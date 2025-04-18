package org.kcg.gobongchan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ggobong.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.*

const val KCGDarkBlue = 0xFF0A154B
const val KCGBlue = 0xFF0B4094
const val KCGRed = 0xFFE00030
const val KCGYellow = 0xFFFECD17

const val datapath = "composeResources/ggobong.composeapp.generated.resources"
const val csvName = "files/Gobong.csv"
const val username = "Mr.고 선생님"
const val phoneNumber = "063-000-0000"
const val isOnline = false

val LabelSize = 7
val cNameList = mutableListOf<String>()

data class MainData(
    val map: Map<String, String> = mutableMapOf()
)

data class Chat(
    val message: String,
    val time: String,
    val isOutgoing: Boolean,
    val linkData :ChatLinkData? = null,
    val xyData: Pair<Double, Double>?=  null
)
data class ChatLinkData(
    val thumbnail: DrawableResource,
    val title: String,
    val detail: String,
    val link :String
)
val message = mutableStateOf("")

val commonMap = mutableMapOf(
    "rstCode" to "1",
    "rstTitle" to null,
    "rstMessage" to "대충 이런 느낌?",
    "errorMessage" to null,
    "fileName" to null
)

fun extNameIcon(fName:String) = when {
    fName.endsWith("gif") -> Res.drawable.gif_64dp
    fName.endsWith("png") -> Res.drawable.png_64dp
    fName.endsWith("jpg") -> Res.drawable.image_64dp
    fName.endsWith("bmp") -> Res.drawable.image_64dp
    fName.endsWith("mp4") -> Res.drawable.moive_64dp
    fName.endsWith("wmv") -> Res.drawable.moive_64dp
    fName.endsWith("avi") -> Res.drawable.moive_64dp
    fName.endsWith("zip") -> Res.drawable.zip_64dp
    fName.endsWith("pdf") -> Res.drawable.pdf_64fp
    fName.endsWith("hwp") -> Res.drawable.docs_64dp
    fName.endsWith("hwpx") -> Res.drawable.docs_64dp
    fName.endsWith("xls") -> Res.drawable.docs_64dp
    else -> Res.drawable.link_64dp
}

fun isBetween(n : Int, start: Int, end:Int) = n in start..end

// convert : (376153, 1073087) -> (126.4408,37.3611)
fun TM5179toWGS84(x: Double, y: Double): Pair<Double, Double> {
    val RE = 6378137.0
    val GRID = 5.0
    val SLAT1 = 30.0
    val SLAT2 = 60.0
    val OLON = 126.0
    val OLAT = 38.0
    val XO = 200000.0
    val YO = 500000.0

    val DEGRAD = PI / 180.0
    val RADDEG = 180.0 / PI

    val re = RE / GRID
    val slat1 = SLAT1 * DEGRAD
    val slat2 = SLAT2 * DEGRAD
    val olon = OLON * DEGRAD
    val olat = OLAT * DEGRAD

    val sn = tan(PI * 0.25 + slat2 * 0.5) / tan(PI * 0.25 + slat1 * 0.5)
    val snLog = ln(cos(slat1) / cos(slat2)) / ln(sn)
    val sf = tan(PI * 0.25 + slat1 * 0.5)
    val sfPow = sf.pow(snLog) * cos(slat1) / snLog
    val ro = re * sfPow / tan(PI * 0.25 + olat * 0.5).pow(snLog)

    val xn = x - XO
    val yn = ro - (y - YO)
    val ra = sqrt(xn * xn + yn * yn)
    val theta = atan2(xn, yn)

    val alat = 2.0 * atan((re * sfPow / ra).pow(1.0 / snLog)) - PI * 0.5
    val alon = theta / snLog + olon

    val lat = alat * RADDEG
    val lon = alon * RADDEG

    return lon to lat
}
// convert : (126.4408,37.3611) -> (376153, 1073087)
fun WGS84toTM5179(lon: Double, lat: Double): Pair<Double, Double> {
    val RE = 6378137.0
    val GRID = 5.0
    val SLAT1 = 30.0
    val SLAT2 = 60.0
    val OLON = 126.0
    val OLAT = 38.0
    val XO = 200000.0
    val YO = 500000.0

    val DEGRAD = PI / 180.0

    val re = RE / GRID
    val slat1 = SLAT1 * DEGRAD
    val slat2 = SLAT2 * DEGRAD
    val olon = OLON * DEGRAD
    val olat = OLAT * DEGRAD

    val sn = tan(PI * 0.25 + slat2 * 0.5) / tan(PI * 0.25 + slat1 * 0.5)
    val snLog = ln(cos(slat1) / cos(slat2)) / ln(sn)
    val sf = tan(PI * 0.25 + slat1 * 0.5)
    val sfPow = sf.pow(snLog) * cos(slat1) / snLog
    val ro = re * sfPow / tan(PI * 0.25 + olat * 0.5).pow(snLog)

    val ra = re * sfPow / tan(PI * 0.25 + lat * DEGRAD * 0.5).pow(snLog)
    val theta = lon * DEGRAD - olon
    val thetaSn = if (theta > PI) theta - 2.0 * PI else if (theta < -PI) theta + 2.0 * PI else theta
    val th = thetaSn * snLog

    val x = ra * sin(th) + XO
    val y = ro - ra * cos(th) + YO

    return x to y
}


expect fun getTime() : String
expect fun println(s:String)
expect fun runCall(phoneNumber:String)
expect fun openLinkData(link:String)
expect fun openUrl(url:String)
expect fun addPWA()
@Composable
expect fun InstallButton()
@Composable
expect fun loadCSV(): State<List<MainData>>
@Composable
expect fun WebImage(url:String)

