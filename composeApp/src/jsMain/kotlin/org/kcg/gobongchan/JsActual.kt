package org.kcg.gobongchan

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import ggobong.composeapp.generated.resources.Res
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.window
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Date

actual fun getTime() : String{
    val now = Date(Date.now())
    val hours = now.getHours() % 12
    val minutes = "${now.getMinutes()}".padStart(2,'0')
    val second = "${now.getSeconds()}".padStart(2,'0')
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

actual fun addPWA() {
    window.addEventListener("DOMContentLoaded", {
        console.log("start LaunchedEffect")
        registerServiceWorker{ success -> console.log("start LaunchedEffect: $success") }
        ignoreBackKey()
    })
}

@Composable
actual fun WebImage(url: String) {
    AsyncImage(
        model = url,
        contentScale = ContentScale.FillWidth,
        contentDescription = "maps!",
        modifier = Modifier.fillMaxSize()
    )
}

