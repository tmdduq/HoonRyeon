package org.kcg.gobongchan

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URL
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
        link.startsWith("http") -> link
        else -> "http://osy.kr/compose/$datapath/files/${link}"
    }
    openUrl(url)
}
@Composable
actual fun WebImage(url:String){
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url){
        try{
            val stream: InputStream = URL(url).openStream()
            image = loadImageBitmap(stream)
        }catch(e:Exception){e.printStackTrace()}
    }
    image?.let {
        Image(bitmap = it, contentDescription = "map!!")
    }
}

actual fun openUrl(url: String) {
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(URI(url))
    }
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