package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ggobong.composeapp.generated.resources.*
import io.ktor.client.*

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.window
import org.jetbrains.compose.resources.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise


@Composable
fun GmarketFont() = FontFamily(
    Font(resource = Res.font.GmarketSansTTFLight, weight = FontWeight.Light),
    Font(resource = Res.font.GmarketSansTTFMedium, weight = FontWeight.Medium),
    Font(resource = Res.font.GmarketSansTTFBold, weight = FontWeight.Bold)
)
val KCGDarkBlue = 0xFF0A154B
val KCGBlue = 0xFF0B4094
val KCGRed = 0xFFE00030
val KCGYellow = 0xFFFECD17




//연번,부서대분류,부서중분류,훈련분류,분야,훈련명,시간1,시간2
data class MainData(
    val map: Map<String, Any> = mapOf(
        "연번" to 0,
        "부서대분류" to "",
        "부서중분류" to "",
        "훈련분류" to "",
        "분야" to "",
        "훈련명" to "",
        "훈련횟수" to "",
        "훈련시간" to "",
        "첨부제목" to "",
        "첨부내용" to "",
        "파일명 또는 링크" to "",
    )
)

val commonMap = mutableMapOf<String, String?>(
    "rstCode" to "1",
    "rstTitle" to null,
    "rstMessage" to "대충 이런 느낌?",
    "errorMessage" to null,
    "fileName" to null
)

val csvName = "files/Gobong.csv"





@OptIn(ExperimentalResourceApi::class)
@Composable
fun JsApp() {
    registerServiceWorker()
    ignoreBackKey()

    val rstMap = remember{ mutableStateOf(commonMap.toMutableMap()) }

    val mainDataList by produceState(emptyList()){
        value = HttpClient().use { client ->
            val s = client.get(Res.getUri(csvName)).bodyAsText()

            suspendCoroutine { continuation ->
                val config = js("{}") // JavaScript 객체 생성
                config.header = true
                config.dynamicTyping = true
                config.delimiter = ","
                config.complete = { result: ParseResult<dynamic> ->
                    //println("파싱된 데이터 구조: ${JSON.stringify(result.data)}")
                    val parsedList = result.data
                        .filter { it["연번"]!=null }
                        .map {
                        MainData(
                            mapOf(
                                "연번" to (it["연번"]?.toString()?.toIntOrNull() ?: -1),
                                "부서대분류" to (it["부서대분류"]?.toString() ?: ""),
                                "부서중분류" to (it["부서중분류"]?.toString() ?: ""),
                                "훈련분류" to (it["훈련분류"]?.toString() ?: ""),
                                "분야" to (it["분야"]?.toString() ?: ""),
                                "훈련명" to (it["훈련명"]?.toString() ?: ""),
                                "훈련횟수" to (it["시간1"]?.toString() ?: ""),
                                "훈련시간" to (it["시간2"]?.toString() ?: "")
                            )
                        )
                    }
                    continuation.resume(parsedList)
                }
                Papa.parse(s, config)
            }
        }
    }

    val selectedCategoryMap = mutableMapOf<Int,String>()
    val categoryVerticalDepth = remember { mutableStateOf( 0 ) }
    val showPopup = remember { mutableStateOf(false) }
    var popupData = MainData()

    MaterialTheme( ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier.padding(top = 50.dp, bottom = 10.dp),
                    text = "내게 필요한 교육.",
                    fontFamily = GmarketFont(),
                    fontSize = 40.sp
                )
                Text(
                    modifier = Modifier.padding(start = 40.dp, bottom = 10.dp),
                    text = "꼭 맞게 찾아드려요!",
                    fontFamily = GmarketFont(),
                    fontSize = 40.sp
                )

                Spacer(modifier = Modifier.padding(5.dp))

                val dep0dataList = mainDataList.map { it.map["부서대분류"] }.toSet() as Set<String>
                drawMenu(
                    nameList=dep0dataList,
                    visible = isBetween(categoryVerticalDepth.value,0,1),
                    selectedCategoryName = selectedCategoryMap[0],
                    color = KCGDarkBlue,
                    onClick = { s ->
                        if(selectedCategoryMap[0]!=s){
                            categoryVerticalDepth.value = 0
                            categoryVerticalDepth.value = 1
                        }
                        else if(categoryVerticalDepth.value != 0) categoryVerticalDepth.value = 0
                        else categoryVerticalDepth.value = 1
                        selectedCategoryMap[0] = s
                    })

                val dep1dataList = mainDataList
                    .filter{ it.map["부서대분류"]== selectedCategoryMap[0] }
                    .map{ it.map["부서중분류"] }.toSet() as Set<String>
                drawMenu(
                    nameList=dep1dataList,
                    visible = isBetween(categoryVerticalDepth.value,1,2),
                    selectedCategoryName = selectedCategoryMap[1],
                    color = KCGBlue,
                    onClick = { s ->
                        if(selectedCategoryMap[1]!=s){
                            categoryVerticalDepth.value = 1
                            categoryVerticalDepth.value = 2
                        }
                        else if(categoryVerticalDepth.value != 1) categoryVerticalDepth.value = 1
                        else categoryVerticalDepth.value = 2
                        selectedCategoryMap[1] = if( selectedCategoryMap[1] != s) s else ""
                    })

                val dep2dataList = mainDataList
                    .filter{ it.map["부서대분류"]== selectedCategoryMap[0] }
                    .filter{ it.map["부서중분류"]== selectedCategoryMap[1] }
                    .map{ it.map["훈련분류"] }.toSet() as Set<String>
                drawMenu(
                    nameList=dep2dataList,
                    visible = isBetween(categoryVerticalDepth.value,2,3),
                    selectedCategoryName = selectedCategoryMap[2],
                    onClick = { s ->
                        if(selectedCategoryMap[2]!=s){
                            categoryVerticalDepth.value = 2
                            categoryVerticalDepth.value = 3
                        }
                        else if(categoryVerticalDepth.value != 2) categoryVerticalDepth.value = 2
                        else categoryVerticalDepth.value = 3
                        selectedCategoryMap[2] = if( selectedCategoryMap[2] != s) s else ""
                    })

                val dep3dataList = mainDataList
                    .asSequence()
                    .filter{ it.map["부서대분류"]== selectedCategoryMap[0] }
                    .filter{ it.map["부서중분류"]== selectedCategoryMap[1] }
                    .filter{ it.map["훈련분류"]== selectedCategoryMap[2] }
                    .map{ it.map["분야"] }.toSet() as Set<String>
                drawMenu(
                    nameList=dep3dataList,
                    visible = isBetween(categoryVerticalDepth.value,3,4),
                    selectedCategoryName = selectedCategoryMap[3],
                    color = KCGDarkBlue,
                    onClick = { s ->
                        if(selectedCategoryMap[3]!=s){
                            categoryVerticalDepth.value = 3
                            categoryVerticalDepth.value = 4
                        }
                        else if(categoryVerticalDepth.value != 3) categoryVerticalDepth.value = 3
                        else categoryVerticalDepth.value = 4
                        selectedCategoryMap[3] = if( selectedCategoryMap[3] != s) s else ""
                    })

                val dep4dataList = mainDataList
                    .asSequence()
                    .filter{ it.map["부서대분류"]== selectedCategoryMap[0] }
                    .filter{ it.map["부서중분류"]== selectedCategoryMap[1] }
                    .filter{ it.map["훈련분류"]== selectedCategoryMap[2] }
                    .filter{ it.map["분야"]== selectedCategoryMap[3] }
                    .map{ it.map["훈련명"] }.toSet() as Set<String>
                drawMenu(
                    nameList=dep4dataList,
                    visible = isBetween(categoryVerticalDepth.value,4,5),
                    selectedCategoryName = selectedCategoryMap[4],
                    color = KCGBlue,
                    onClick = { s ->
                        popupData = mainDataList
                            .asSequence()
                            .filter{ it.map["부서대분류"]== selectedCategoryMap[0] }
                            .filter{ it.map["부서중분류"]== selectedCategoryMap[1] }
                            .filter{ it.map["훈련분류"]== selectedCategoryMap[2] }
                            .filter{ it.map["분야"]== selectedCategoryMap[3] }
                            .filter{ it.map["훈련명"]== s }
                            .firstOrNull() ?: throw IllegalArgumentException("조건에 맞는 데이터가 없습니다.")
                        showPopup.value = true
                    })
            }//columns

            //하단바 보이기숨기기
            if(false)
                AnimatedVisibility(
                    visible = true,
                    modifier = Modifier
                        .background(Color(221, 235, 247) )
                        .wrapContentHeight()
                        .fillMaxWidth().align(Alignment.BottomCenter),
                ) {
                    //하단바
                    Footer(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(238, 249, 252),  // 하단
                                        Color(255, 255, 255),  // 상단 색상
                                    )
                                )
                            )
                            //Color(221, 235, 247) )//.background(Color.White)
                            .align(Alignment.BottomCenter),
                        save = {
                            rstMap.value = commonMap.toMutableMap().apply { this["rstTitle"] = "이렇게이렇게..." }
                        }
                    ) // end CalendarBottom
                }

            //다운로드 버튼
            Box(
                modifier = Modifier.fillMaxWidth(1f).height(40.dp),
                contentAlignment = Alignment.CenterEnd) {
                InstallButton()
            }

            AnimatedVisibility(showPopup.value, enter = EnterTransition.None, exit= fadeOut()){
                ChatScreen(popupData, {showPopup.value=false} )
            }


            if(!rstMap.value["rstTitle"].isNullOrBlank()) {
                makeDialog(rstMap.value, { rstMap.value = commonMap.toMutableMap() })
            }


        } // box
    }
}

fun isBetween(n : Int, start: Int, end:Int) = n in start..end


@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun drawMenu(
    nameList:Set<String>,
    visible:Boolean,
    selectedCategoryName : String?,
    color: Long=0xffff9040,
    onClick:(s:String)->Unit){
    AnimatedVisibility(visible) {

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (s in nameList) {
                val screenWidth = LocalWindowInfo.current.containerSize.width / LocalDensity.current.density
                val borderWidth = if(selectedCategoryName.isNullOrEmpty() || s!=selectedCategoryName) 0.dp else 5.dp
                mainWindowBox(
                    modifier = Modifier.height(70.dp).wrapContentWidth().widthIn(min = (screenWidth*0.3f).dp)
                        .border(color = Color(KCGYellow), width = borderWidth ),
                    text = s, textFont = GmarketFont(),
                    backgroundColor = color,
                    onClick = {onClick(s)}
                ) //mainBox
            } // for
        } // flowRow
    } // ani
}

@Preview
@Composable
fun popUpView(popupData:MainData, onClose: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(40.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth().fillMaxHeight()
                        .background(Color.White)
                        .padding(top=30.dp, bottom = 30.dp)
                ) {
                    Icon(
                        modifier = Modifier.height(50.dp).aspectRatio(1f).clip(CircleShape)
                            .clickable(onClick=onClose).padding(6.dp).align(Alignment.End),
                        imageVector = Icons.Default.Close,
                        contentDescription = "close"
                    )
                    Text(
                        modifier = Modifier.padding(top = 50.dp, bottom = 10.dp),
                        text = popupData.map["훈련명"].toString(),
                        fontFamily = GmarketFont(),
                        fontSize = 30.sp
                    )

                    Text("여기에는 상세 정보를 표시할 수 있습니다.")
                    Button(onClick = onClose) {
                        Text("닫기")
                    }
                }
            }

    }
}

@Composable
fun mainWindowBox(text:String, backgroundColor:Long, onClick:()->Unit, modifier: Modifier=Modifier, textFont:FontFamily){
    val itemModifier = Modifier
        .padding(4.dp)
        .clip(RoundedCornerShape(8.dp))
        .then(modifier)
    return Box(
        contentAlignment = Alignment.Center,
        modifier = itemModifier.clickable(
            onClick =onClick,
        ).background(Color(backgroundColor))
    ){
        Text(
            modifier = Modifier.wrapContentSize(Alignment.Center).padding(start = 15.dp, end = 15.dp),
            text=text, fontWeight = FontWeight.Bold,
            fontFamily = textFont,
            fontSize = 20.sp, color = Color.White,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    save: () -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        //footer 경계선
        Divider(
            color= Color(0,0,0,192),
            thickness = 2.dp,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //footer Text
            Text(
                text = "선택이 완료되었어요. \n필요한 교육을 확인해볼까요?",
                fontWeight = FontWeight.Bold, fontFamily = GmarketFont()
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.height(40.dp).width(100.dp),
                onClick = save,
                colors = ButtonDefaults.buttonColors(Color(10,21,75)),
                enabled = true,
            ) {
                Text(text = "찾기", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun makeDialog(rstMap : MutableMap<String, String?>, onClose:()-> Unit){
    val title = rstMap["rstTitle"]?:""
    val message = rstMap["rstMessage"]?:""
    val footer = if(rstMap["rstCode"]=="1") "화면구성은 이런식으로 처리하고.. 엑셀파일이 잘될지 모르겠네요.., " else rstMap["errorMessage"]?:""
    if(title.isEmpty()) onClose()
    Dialog(
        onDismissRequest = {  },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
            Column(modifier = Modifier.padding(16.dp).defaultMinSize(minWidth = 300.dp)) {

                Text(text = title,
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xff9a4bf1))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = message, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = footer, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.padding(horizontal = 5.dp),
                        onClick = onClose)
                    { Text("확인") }
                }
            }
        }
    }
}


