package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
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
import org.w3c.dom.Navigator
import org.w3c.workers.ServiceWorker
import org.w3c.workers.ServiceWorkerRegistration
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun GmarketFont() = FontFamily(
    Font(resource = Res.font.GmarketSansTTFLight, weight = FontWeight.Light),
    Font(resource = Res.font.GmarketSansTTFMedium, weight = FontWeight.Medium),
    Font(resource = Res.font.GmarketSansTTFBold, weight = FontWeight.Bold)
)
const val KCGDarkBlue = 0xFF0A154B
const val KCGBlue = 0xFF0B4094
const val KCGRed = 0xFFE00030
const val KCGYellow = 0xFFFECD17
const val csvName = "files/Gobong.csv"
val LabelSize = 7


val cNameList = mutableListOf<String>()

data class MainData(
    val map: Map<String, String> = mutableMapOf()
)

val commonMap = mutableMapOf(
    "rstCode" to "1",
    "rstTitle" to null,
    "rstMessage" to "대충 이런 느낌?",
    "errorMessage" to null,
    "fileName" to null
)



@Composable
fun JsApp() {
    LaunchedEffect(Unit){
        window.addEventListener("DOMContentLoaded", {
            console.log("start LaunchedEffect")
            registerServiceWorker{ success -> console.log("start LaunchedEffect: $success") }
            ignoreBackKey()
        })
    }
    val rstMap = remember{ mutableStateOf(commonMap.toMutableMap()) }
    val mainDataList by loadCSV()
    val selectedCategoryMap = mutableMapOf<Int,String>()
    val categoryVerticalDepth = remember { mutableStateOf( 0 ) }
    val showPopup = remember { mutableStateOf(false) }
    var popupData = MainData()
    val phoneNumber = remember { mutableStateOf("") }

    MaterialTheme{
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
                val mainDataList : List<MainData> = mainDataList


                for(i in 0 until LabelSize){
                    var depDataList = mainDataList
                    if(i>0)
                        for(j in 0 until i)
                            depDataList = depDataList.filter {it.map[cNameList[j+1]]  == selectedCategoryMap[j] }
                    val d = depDataList.map{it.map[cNameList[i+1]]?:""}.filter{ it!="" }.toSet()


                    drawMenu(
                        nameList= d,
                        visible = isBetween(categoryVerticalDepth.value,i,i+1),
                        selectedCategoryName = selectedCategoryMap[i],
                        color = listOf(KCGDarkBlue, KCGBlue, 0xffff9040L, KCGYellow)[i%4],
                        onClick = { s ->
                            if(i < LabelSize-1) {
                                //console.log("depth:${categoryVerticalDepth.value},\tbeforeNode(map[i]):${selectedCategoryMap[i]},\tclickNode=$s")
                                val predNextNode = depDataList.filter {it.map[cNameList[i+1]]  == s }.map{it.map[cNameList[i+2]]?:""}.filter{ it!="" }.toSet()
                                when{
                                    predNextNode.isEmpty() && i!=0->{
                                        console.log("last node")
                                        popupData = depDataList.filter {it.map[cNameList[i+1]]  == s }.firstOrNull() ?: MainData()
                                        showPopup.value = true

                                        categoryVerticalDepth.value = i
                                        selectedCategoryMap[i] = if (selectedCategoryMap[i] != s) s else ""
                                    }
                                    !selectedCategoryMap[i].isNullOrEmpty() &&selectedCategoryMap[i] != s->{
                                        console.log("eq depth / diff node")
                                        categoryVerticalDepth.value = i
                                        categoryVerticalDepth.value = i + 1
                                    }
                                    categoryVerticalDepth.value != i ->{
                                        console.log("eq node")
                                        categoryVerticalDepth.value = i
                                    }
                                    else->{
                                        console.log("diff depth / diff node")
                                        categoryVerticalDepth.value = i + 1
                                    }
                                }
                                selectedCategoryMap[i] = if (selectedCategoryMap[i] != s) s else ""

                            }
                            else{
                                console.log("last depth")
                                depDataList = depDataList.filter {it.map[cNameList[i]]  == selectedCategoryMap[i-1] }
                                popupData = depDataList.firstOrNull { it.map[cNameList[i + 1]] == s } ?: MainData()
                                showPopup.value = true
                            }
                        })
                }
            }//columns

            //팝업 채팅창
            AnimatedVisibility(showPopup.value, enter = EnterTransition.None, exit= fadeOut()){
                ChatScreen(
                    popupData = popupData,
                    onBack = {
                        showPopup.value = false
                        phoneNumber.value =""
                    },
                    toPhone = { phoneNumber.value = it }
                )
            }

            //하단바 보이기숨기기
            AnimatedVisibility(
                visible = phoneNumber.value.isNotEmpty(),
                modifier = Modifier
                    .background(Color(221, 235, 247) )
                    .wrapContentHeight()
                    .fillMaxWidth().align(Alignment.BottomCenter),
            ) {
                //하단바
                Footer(
                    modifier = Modifier.wrapContentHeight().fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(238, 249, 252),  // 하단
                                    Color(255, 255, 255),  // 상단 색상
                                )
                            )
                        ).align(Alignment.BottomCenter),
                    save = {
                        rstMap.value = commonMap.toMutableMap().apply { this["rstTitle"] = "이렇게이렇게..." }
                    },
                    phoneNumber = phoneNumber.value
                ) // end CalendarBottom
            }

            //다운로드 버튼
            Box(
                modifier = Modifier.fillMaxWidth(1f).height(40.dp),
                contentAlignment = Alignment.CenterEnd) {
                InstallButton()
            }


            if(!rstMap.value["rstTitle"].isNullOrBlank()) {
                makeDialog(rstMap.value) { rstMap.value = commonMap.toMutableMap() }
            }


        } // box
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun loadCSV(): State<List<MainData>> {
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
    phoneNumber:String
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
                text = "더 궁금한게 있으신가요? \n사무실로 전화해보세요!",
                fontWeight = FontWeight.Bold, fontFamily = GmarketFont()
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.height(40.dp).width(100.dp),
                onClick = {
                    window.location.href = "tel:$phoneNumber"
                },
                colors = ButtonDefaults.buttonColors(Color(10,21,75)),
                enabled = true,
            ) {
                Text(text = "전화", color = Color.White, fontWeight = FontWeight.Bold)
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


