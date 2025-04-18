package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource


@Composable
fun GmarketFont() = FontFamily(
    Font(resource = Res.font.GmarketSansTTFLight, weight = FontWeight.Light),
    Font(resource = Res.font.GmarketSansTTFMedium, weight = FontWeight.Medium),
    Font(resource = Res.font.GmarketSansTTFBold, weight = FontWeight.Bold)
)

@Composable
fun mainWindowBox(text:String, backgroundColor:Long, onClick:()->Unit, modifier: Modifier = Modifier, textFont: FontFamily){
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
fun Footer(
    modifier: Modifier = Modifier,
    onCall: () -> Unit,
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
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(Color(10,21,75)),
                enabled = true,
            ) {
                Text(text = "전화", color = Color.White, fontWeight = FontWeight.Bold)
            }

        }
    }
}

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
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xff9a4bf1)
                )
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




















val chats = mutableStateListOf<Chat>(
    Chat("교육에 대해 상세히 알려줄게!", getTime(), false),
)

fun chatsInit(data: Map<String, String>){

    for(s in cNameList.subList(0, LabelSize+1).reversed())
        if(!data[s].isNullOrEmpty()){
            chats.add(Chat("<${data[s]}>에 대해 알려주세요!",getTime(), true))
            break
        }

    data.filter { it.key !in cNameList.subList(0, LabelSize+1)  }
        .filter { it.key !in listOf("첨부제목", "첨부내용", "파일명 또는 링크", "위경도")  }
        .forEach { (k,v) ->
            if(v.isNotEmpty())
                chats.add(Chat("$k : $v",getTime(), false))
        }
    if("${data["첨부제목"]}".isNotEmpty()){
        val fName = data["파일명 또는 링크"] as String
        val linkData = ChatLinkData(extNameIcon(fName),
            "${data["첨부제목"]}",
            "${data["첨부내용"]}",
            "${data["파일명 또는 링크"]}")
        chats.add(Chat("첨부파일을 참고하세요.",getTime(), false, linkData))
    }
    if("${data["위경도"]}".isNotEmpty()){
        val yxString = data["위경도"] as String
        val yxSplit = yxString.split(",")
        val latSplit = yxSplit[0].trim().split("-")
        val lonSplit = yxSplit[1].trim().split("-")
        val lat = latSplit[0].toDouble() + (latSplit[1].toDouble()/60) + (latSplit[2].toDouble()/3600)
        val lon = lonSplit[0].toDouble() + (lonSplit[1].toDouble()/60) + (lonSplit[2].toDouble()/3600)
        println("lat $lat, lon $lon")
        chats.add(Chat("여기에요!",getTime(), false,null, xyData = Pair(lon, lat) ))
    }
}

@Composable
fun ChatScreen(popupData : MainData, onBack:() -> Unit, toPhone: (number: String) -> Unit) {
    LaunchedEffect(popupData) {
        chatsInit(popupData.map)
    }

    Box(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
            .padding(40.dp)
            .background(Color.Black.copy(0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(Color(237,253,200)).clickable {onBack() },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopBarSection(
                username = username,
                profile = painterResource(Res.drawable.gobongchan),
                isOnline = isOnline,
                onBack = onBack,
            )
            ChatSection(Modifier.weight(1f))
            MessageSection(
                toPhone = {number-> toPhone(number) }
            )
        }
    }
}

@Composable
fun TopBarSection(
    username: String,
    profile: Painter,
    isOnline: Boolean,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        backgroundColor = Color(0xFFFAFAFA),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ){
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = profile,
                contentDescription = null,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(Modifier.clickable {  runCall(phoneNumber) })
            {
                Text(text = username, fontWeight = FontWeight.SemiBold, fontFamily = GmarketFont())
                Text(
                    text = if (isOnline) "Online" else "문의: $phoneNumber",
                    fontSize = 12.sp, fontFamily = GmarketFont()
                )
            }
        }
    }
}

@Composable
fun ChatSection(
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    if(chats.size>30)
        chats.removeRange(0, chats.lastIndex-15)
    LaunchedEffect(chats.size) {
        listState.animateScrollToItem(chats.size - 1)
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        state = listState
    ) {
        items(chats) { chat ->
            MessageItem(
                chat.message,
                chat.time,
                chat.isOutgoing,
                chat.linkData,
                chat.xyData
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}



@Composable
fun MessageSection(toPhone:(number:String)-> Unit) {
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        backgroundColor = Color.White,
        elevation = 10.dp
    ) {
        OutlinedTextField(
            placeholder = {
                Text("Message..")
            },
            value = message.value,
            onValueChange = {
                message.value = it
            },
            shape = RoundedCornerShape(25.dp),

            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.send_24dp),
                    contentDescription = "send",//
                    tint = Color.Unspecified,
                    modifier = Modifier.size(50.dp).aspectRatio(1f).clip(CircleShape)
                        .clickable {
                            chats.add(Chat(message.value, getTime(), true))
                            message.value = ""
                            coroutineScope.launch{
                                delay(1000)
                                chats.add(Chat("전화주세요. 이 메시지는 확인하지 않아요.", getTime(), false))
                                toPhone(phoneNumber)
                            }
                        }
                )

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MessageItem(
    messageText: String,
    time: String,
    isOut: Boolean,
    linkData: ChatLinkData?=null,
    xyData: Pair<Double,Double>?=null
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOut) Alignment.End else Alignment.Start
    ) {
        // Text Box
        Box(
            modifier = Modifier
                .background(
                    if (isOut) MaterialTheme.colors.primary else Color(0xFF616161),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(
                    top = 8.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            Text(
                text = messageText,
                fontFamily = GmarketFont(),
                color = Color.White
            )
        }
        // file Box
        if(linkData!=null) {
            Box(
                modifier = Modifier.padding(top = 8.dp)
                    .background(Color.White)
                    .wrapContentHeight()
                    .heightIn(max = 100.dp)
                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable {openLinkData(linkData.link)}
            ) {
                Row {
                    Image(
                        modifier = Modifier
                            .weight(0.25f)
                            .padding(end = 16.dp).align(Alignment.CenterVertically),
                        painter = painterResource(linkData.thumbnail), contentDescription = "thumbnail"
                    )
                    Box(
                        Modifier.fillMaxHeight()
                            .width(1.dp)
                            .background(Color.LightGray)
                    )
                    Column(
                        modifier = Modifier.weight(0.75f).padding(start = 16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    )
                    {
                        Text(linkData.title, fontFamily = GmarketFont(), fontSize = 15.sp,)
                        Divider(thickness = 1.dp)
                        Text(linkData.detail, fontFamily = GmarketFont(), fontSize = 12.sp,)
                    }

                }

            }
        }
        if(xyData!=null) {
            val lon = xyData.first
            val lat = xyData.second

            Box(
                modifier = Modifier.padding(top = 8.dp)
                    .background(Color.White).wrapContentHeight().wrapContentWidth()
                    .padding(2.dp).align(Alignment.CenterHorizontally).clickable {
                        openUrl("https://map.kakao.com/link/map/$messageText,$lat,$lon")
                    }
            ) {
                WebImage("https://osy.kr/compose/proxy/static-map?lon=$lon&lat=$lat&w=300&h=200")

            }
        }

        // Time View
        Text(
            text = time,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )

    }
}


@Composable
fun JsApp() {
    LaunchedEffect(Unit){
        addPWA()
    }
    val rstMap = remember{ mutableStateOf(commonMap.toMutableMap()) }
    val mainDataList by loadCSV()
    val selectedCategoryMap = mutableMapOf<Int,String>()
    val categoryVerticalDepth = remember { mutableStateOf( 0 ) }
    val showPopup = remember { mutableStateOf(false) }
    var popupData = MainData()
    val phoneNumber = remember { mutableStateOf("") }
    println("size "+mainDataList.size)
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
                                        println("last node")
                                        popupData = depDataList.filter {it.map[cNameList[i+1]]  == s }.firstOrNull() ?: MainData()
                                        showPopup.value = true

                                        categoryVerticalDepth.value = i
                                        selectedCategoryMap[i] = if (selectedCategoryMap[i] != s) s else ""
                                    }
                                    !selectedCategoryMap[i].isNullOrEmpty() &&selectedCategoryMap[i] != s->{
                                        println("eq depth / diff node")
                                        categoryVerticalDepth.value = i
                                        categoryVerticalDepth.value = i + 1
                                    }
                                    categoryVerticalDepth.value != i ->{
                                        println("eq node")
                                        categoryVerticalDepth.value = i
                                    }
                                    else->{
                                        println("diff depth / diff node")
                                        categoryVerticalDepth.value = i + 1
                                    }
                                }
                                selectedCategoryMap[i] = if (selectedCategoryMap[i] != s) s else ""

                            }
                            else{
                                println("last depth")
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
                    onCall = {
                        rstMap.value = commonMap.toMutableMap().apply { this["rstTitle"] = "이렇게이렇게..." }
                        runCall(phoneNumber.value)
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
