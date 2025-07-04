package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ggobong.composeapp.generated.resources.Res
import ggobong.composeapp.generated.resources.app_footer_text
import ggobong.composeapp.generated.resources.kakaoWebKey
import ggobong.composeapp.generated.resources.police
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLOrSVGScriptElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Date

actual fun getTime() : String{
    val now = Date(Date.now())
    val hours = now.getHours() % 12
    val minutes = "${now.getMinutes()}".padStart(2,'0')
    //val second = "${now.getSeconds()}".padStart(2,'0')
    val ampm = if(hours>=12) "PM" else "AM"
    val hour = if(hours==0) "12" else "$hours".padStart(2,'0')
    return "$hour:$minutes $ampm"
}

actual fun println(s:String) {
    console.log(s)
}

actual fun runCall(phoneNumber:String) {
    window.location.href = phoneNumber
}


actual fun openLinkData(link: String) {
    window.open(
        url = when{
            link.endsWith("pdf") -> "pdfjs/web/viewer.html?file=../../$datapath/files/$link"
            link.startsWith("http") -> link
            else -> "$datapath/files/${link}"
        },
        target = "_blank")
}

actual fun openUrl(url: String) {
    window.open( url = url, target = "_blank")
}

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun loadCSV(): State<List<MainData>> {
    return produceState(emptyList()){
        value = HttpClient().use { client ->
            val s = client.get(Res.getUri(csvName)).bodyAsText()

            suspendCoroutine { continuation ->
                val config = js("{} ") // JavaScript 객체 생성
                config.header = true
                config.dynamicTyping = true
                config.skipEmptyLines = true
                config.delimiter = ","
                config.complete = { result: ParseResult<dynamic> ->
                    //println("파싱된 데이터 구조: ${JSON.stringify(result.data)}")
                    val fields = result.meta.fields ?: emptyArray()
                    cNameList.addAll(fields)
                    console.log("header :"+fields.joinToString(","))

                    val parsedList = result.data
                        .map { csvLineMap->
                            val map = cNameList.associateWith { cName ->csvLineMap[cName]?.toString()?:"" }
                            //map.keys.forEach {k-> console.log("$k : ${map[k]}") }
                            MainData(map=map)
                        }
                    continuation.resume(parsedList)
                }
                Papa.parse(s, config)
            }
        }
    }
}

actual fun getLastName(mainDataList: List<MainData>, keyword:String): MutableMap<String, MainData> {
    val rstMap = mutableMapOf<String,MainData>()
    if(keyword.length<2) return rstMap
    val keywords = keyword.trim().split(" ")

    for( mainData in mainDataList){
        val dataMap = mainData.map
        if( dataMap.values.any{v-> keywords.all{k-> v.contains(k) } } ){
            val lastName = when{
                dataMap[cNameList[7]]!!.isNotEmpty() -> dataMap[cNameList[7]]!!
                dataMap[cNameList[6]]!!.isNotEmpty() -> dataMap[cNameList[6]]!!
                dataMap[cNameList[5]]!!.isNotEmpty() -> dataMap[cNameList[5]]!!
                dataMap[cNameList[4]]!!.isNotEmpty() -> dataMap[cNameList[4]]!!
                dataMap[cNameList[3]]!!.isNotEmpty() -> dataMap[cNameList[3]]!!
                dataMap[cNameList[2]]!!.isNotEmpty() -> dataMap[cNameList[2]]!!
                dataMap[cNameList[1]]!!.isNotEmpty() -> dataMap[cNameList[1]]!!
                else -> "오류"
            }
            rstMap[lastName] = mainData
            println(lastName)
        }
    }
    return rstMap
}



actual fun addPWA() {
    window.addEventListener("DOMContentLoaded", {
        console.log("start LaunchedEffect")
        registerServiceWorker{ success -> console.log("start LaunchedEffect: $success") }
    })
}

@Composable
actual fun windowWidth()= window.innerWidth.dp
@Composable
actual fun windowHeight()= window.innerHeight.dp

@Composable
actual fun WebImage(url: String) {
    AsyncImage(
        model = url,
        contentScale = ContentScale.Fit,
        contentDescription = "maps!",
        modifier = Modifier.fillMaxSize()
    )
}

actual fun registerBackHandler(onBack: () -> Unit) {
    window.onpopstate = {
        window.history.pushState(null, "", "./")
        onBack()
    }
}
actual fun pushHistoryState(s:String) {
    window.history.pushState("depth state $s", "", "#$s")
}

external abstract class HTMLScriptElement : HTMLElement, HTMLOrSVGScriptElement {
    open var src: String
    open var type: String
    open var charset: String
    open var async: Boolean
    open var defer: Boolean
    open var crossOrigin: String?
    open var text: String
    open var event: String
    open var integrity: String

    companion object {
        val ELEMENT_NODE: Short
        val TEXT_NODE: Short
    }
}


@Composable
actual fun KakaoShareScreen() {
    var isSdkLoaded by remember { mutableStateOf(false) }
    val key = stringResource(Res.string.kakaoWebKey)
    LaunchedEffect(key) {
        if(key.isEmpty()) return@LaunchedEffect
        /* <script src="@@@" integrity="@@@" crossOrigin="@@@"> </script> */
        val script = document.createElement("script") as HTMLScriptElement
        script.src = "https://t1.kakaocdn.net/kakao_js_sdk/2.7.4/kakao.min.js"
        script.integrity = "sha384-DKYJZ8NLiK8MN4/C5P2dtSmLQ4KwPaoqAfyA/DfmEc1VDxu4yyC7wy6K1Hs90nka"
        script.crossOrigin = "anonymous"
        script.onload = {
            /* Kakao.init('@@@');  */
            val kakao = js("Kakao")
            kakao.init(key)
            //kakao.init("202c1ec422cbc5a9dfce5f64b5b880ed")
            isSdkLoaded = true
            Unit
        }
        document.head?.appendChild(script)
    }
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.clickable {
            if (isSdkLoaded) {
                val kakao = js("Kakao")
                kakao.Share.sendCustom(js("{ templateId: 120143 }"))
                //kakao.Channel.chat(js("{ channelPublicId: '_nAttxj' }"))
            }
        }
    ) {
        Row(modifier = Modifier.weight(0.7f), verticalAlignment = Alignment.Bottom){
            Image(
                painter = painterResource(Res.drawable.app_footer_text),
                contentDescription = "footerText"
            )
            Image(
                modifier = Modifier.padding(bottom=5.dp),
                painter = painterResource(Res.drawable.police),
                contentDescription = "footerText"
            )
        }
        AnimatedVisibility(isSdkLoaded) {
            Text(
                "여기를 눌러 주변 동료에게 알려주세요!",
                fontFamily = GmarketFont(),
                modifier = Modifier.weight(0.3f),
                color = Color(KCGBlue), lineHeight = 13.sp, fontSize = 13.sp,
            )
        }
    }
}

@Composable
actual fun mapView(){

        Div(attrs = {
//            id("map")
//            style {
//                property("background","#00f")
//            }

        })

}