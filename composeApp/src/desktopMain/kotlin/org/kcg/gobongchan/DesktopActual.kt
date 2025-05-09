package org.kcg.gobongchan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.rememberWindowState
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import ggobong.composeapp.generated.resources.Res
import ggobong.composeapp.generated.resources.app_footer_text
import ggobong.composeapp.generated.resources.police
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource
import java.awt.Desktop
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual fun getTime() : String{
    val currentDateTime = LocalDateTime.now()  // 현재 날짜와 시간
    val formatter = DateTimeFormatter.ofPattern("HH:mm A")
    val formattedDateTime = currentDateTime.format(formatter)
    return formattedDateTime
}

@Composable
actual fun InstallButton(){
    println("InstallButton")
}
actual fun println(s:String) {
    kotlin.io.println(s)
}

actual fun runCall(phoneNumber:String) {
    println("runCall :$phoneNumber")
}

actual fun openLinkData(link: String) {
    val url = when {
        link.startsWith("http") -> {
            val lastCommaIndex = link.lastIndexOf("/")
            val base = link.substring(0, lastCommaIndex)
            val endPoint = link.substring(lastCommaIndex + 1)
            base +"/"+URLEncoder.encode(endPoint, Charsets.UTF_8.name()).replace("+", "%20")
        }
        else -> "http://osy.kr/compose/$datapath/files/" +
            URLEncoder.encode(link, Charsets.UTF_8.name()).replace("+", "%20")
    }

    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(URI(url))
    }
}
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun WebImage(url:String){
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url){
        try{
            val bitmap = withContext(Dispatchers.IO){
                val stream: InputStream = URL(url).openStream()
                stream.readAllBytes().decodeToImageBitmap()
            }
            image = bitmap
        }catch(e:Exception){e.printStackTrace()}
    }
    image?.let {
        Image(bitmap = it, contentDescription = "map!!")
    }
}

actual fun openUrl(url: String) {
    openLinkData(url)
}



@Composable
actual fun loadCSV(): State<List<MainData>> {
    return produceState(initialValue = emptyList()) {
        value = loadCSVFromFile("src/commonMain/composeResources/$csvName")
    }
}

suspend fun loadCSVFromFile(path: String): List<MainData> = withContext(Dispatchers.IO) {
    try {
        val csvFile = File(path)
        val reader: CsvReader = csvReader {
            delimiter = ','
            quoteChar = '"'
            skipEmptyLine = true
            charset = "UTF-8"
            skipMissMatchedRow = true
        }

        val rows: List<Map<String, String>> = reader.readAllWithHeader(csvFile)
        val headers = rows.firstOrNull()?.keys ?: emptySet()
        cNameList.addAll(headers.toList())
        rows.map { rowMap ->
            val completeMap = headers.associateWith { key ->
                rowMap[key]?.takeIf { it.isNotBlank() } ?: ""
            }
            MainData(map = completeMap)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

actual fun addPWA() {
    println("addPWA")
}

@Composable
actual fun windowWidth() = rememberWindowState().size.width
@Composable
actual fun windowHeight() = rememberWindowState().size.height


actual fun registerBackHandler(onBack: () -> Unit) {
    // 데스크톱은 브라우저 뒤로가기 개념이 없으므로 아무 작업도 안 함
}
actual fun pushHistoryState(s:String) {
    // 아무 것도 안 함
}


@Composable
actual fun KakaoShareScreen() {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.clickable {}
    ) {
        Row(modifier = Modifier.weight(0.7f),){
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
        Text(
            "여기를 눌러 주변 동료에게 알려주세요!",
            fontFamily = GmarketFont(),
            modifier = Modifier.weight(0.3f),
            color = Color(KCGBlue), lineHeight = 13.sp, fontSize = 13.sp,
        )

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

@Composable
actual fun mapView(){
    Column(Modifier.fillMaxSize().background(Color.Cyan)) {  }
}