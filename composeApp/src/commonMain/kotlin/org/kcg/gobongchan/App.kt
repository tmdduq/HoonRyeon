package org.kcg.gobongchan

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ggobong.composeapp.generated.resources.Res
import ggobong.composeapp.generated.resources.chat
import ggobong.composeapp.generated.resources.police
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun App() {
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
                    //toPhone = { phoneNumber.value = it }
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
                ) // end
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







@Composable
fun ChatScreen(popupData : MainData, onBack:() -> Unit, ) {
    val title = remember { mutableStateOf("") }

    LaunchedEffect(popupData) {
        chats.clear()
        title.value = chatsInit(popupData.map)
    }
    MaterialTheme{
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Color.Black.copy(0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    //.background(Color(0xfff2f2f2)))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xfff2f2f2),  // 상단 색상
                                Color(238, 249, 252),  // 하단
                            )
                        )
                    ))
            {
                //TopBar
                TopBarSection(title.value)
                //Chat List
                ChatSection(Modifier.weight(1f))

            }
            Row(modifier = Modifier.padding(16.dp)
                .height(70.dp).align(Alignment.BottomEnd).clickable{onBack()},
                verticalAlignment = Alignment.CenterVertically)
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "close Chat"
                )
                Text("돌아가기", fontFamily = GmarketFont())
            }
        }
    }

}

@Composable
fun ChatItemView(
    subtitle : String,
    detail: String,
    isOut: Boolean,
    linkData: ChatLinkData?=null,
    xyData: Pair<Double,Double>?=null){
    val layoutDirection =
        if(isOut) LocalLayoutDirection provides LayoutDirection.Rtl
        else LocalLayoutDirection provides LayoutDirection.Ltr
    CompositionLocalProvider(layoutDirection) {
        Column(Modifier.fillMaxWidth().wrapContentHeight().widthIn(min=100.dp)) {

            Box(
                modifier = Modifier.padding(start=3.dp, end=3.dp)
            ) {
                Row(
                    modifier = Modifier
                        //.fillMaxWidth().height(IntrinsicSize.Min)
                ) {

                    //subTitle Box
                    Card(
                        modifier = Modifier
                            .padding(bottom = 12.dp, start = 3.dp, end = 3.dp) // 외부 여백
                            .wrapContentWidth().fillMaxHeight().widthIn(max = 110.dp),
                        elevation = 10.dp, backgroundColor = Color.Transparent,
                        shape = RoundedCornerShape(topEndPercent = 20, bottomEndPercent = 20)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.background(
                                    color = Color(11, 64, 148),
                                    shape = RoundedCornerShape(topEndPercent = 20, bottomEndPercent = 20)
                                ).padding(8.dp)
                        ) {
                            Text(
                                subtitle,
                                textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                                color = Color.White, fontFamily = GmarketFont(),
                                style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                            )
                        } // end subTitle Box
                    }
                    // Detail Box
                    Box(
                        modifier = Modifier
                            .padding(top=12.dp).padding(start=3.dp, end=3.dp).fillMaxHeight() .wrapContentWidth()
                            .background(Color.White, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                            .border( width = 1.dp,color = Color.Black, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center

                    ) {
                        Column(Modifier.width(IntrinsicSize.Max)) {
                            Text(
                                detail,
                                textAlign = TextAlign.Center, fontFamily = GmarketFont(),
                                style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                            )
                            linkData?.let {
                                FileContentView(linkData)
                            }
                            xyData?.let{
                                MapContentView(detail, xyData)
                            }
                        }
                    } // end Detail Box
                }

            } // end Box

        }
    }
}

@Composable
fun MapContentView(detail:String, xyData: Pair<Double, Double>){
    val lon = xyData.first
    val lat = xyData.second
    Box(
        modifier = Modifier.padding(top = 8.dp)
            .background(Color.White).wrapContentHeight().wrapContentWidth()
            .padding(2.dp).clickable {
                openUrl("https://map.kakao.com/link/map/$detail,$lat,$lon")
            }
    ) {
        WebImage("https://osy.kr/compose/proxy/static-map?lon=$lon&lat=$lat&w=600&h=400")
    }

}

@Composable
fun FileContentView(linkData: ChatLinkData){
    Row(
        Modifier.clickable {openLinkData(linkData.link) }
            .padding(top=3.dp).fillMaxWidth()
    ) {
        //File Icon Image
        Image(
            modifier = Modifier
                .padding(end = 8.dp).align(Alignment.CenterVertically),
            painter = painterResource(linkData.thumbnail), contentDescription = "thumbnail"
        )
        //Vertical Border Line
        Box(Modifier.fillMaxHeight().width(1.dp).background(Color.LightGray))

        Column(
            modifier = Modifier.padding(start = 8.dp, end =8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        )
        {
            Text(linkData.title, fontFamily = GmarketFont(), fontSize = 15.sp,
                modifier = Modifier.bottomBorder(Color.LightGray, 1.dp).fillMaxWidth())
            Text("· ${linkData.detail}", fontFamily = GmarketFont(), fontSize = 12.sp,
                style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr))
        }

    }
}

@Composable
fun ChatSection(
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(chats.size) {
        if(chats.size>0) listState.animateScrollToItem(chats.size - 1)
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState
    ) {
        itemsIndexed(chats) { index, chat ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit){
                delay(index*100L)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInHorizontally(initialOffsetX ={ if(chat.isOutgoing) it/2 else -it/2 } )
            ){
                ChatItemView(
                    chat.subtitle,
                    chat.detail,
                    chat.isOutgoing,
                    chat.linkData,
                    chat.xyData
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}



@Composable
fun TopBarSection(
    titleName: String,
) {
    var visible by remember { mutableStateOf(false) }
    val offsetX by animateDpAsState(
        targetValue = if (visible) 0.dp else 300.dp,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.4f,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 10.dp),
        backgroundColor = Color.White,//(151,153,156),
        elevation = 1.dp
    ) {
        Row(Modifier.background(
            Brush.verticalGradient(
                colors = listOf(Color(238, 249, 252), Color(0xfff2f2f2)))
        )) {
            Box(
                modifier = Modifier.fillMaxWidth(0.7f).offset(x = offsetX)
                    .graphicsLayer { alpha = alpha;scaleX = scale;scaleY = scale },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.chat),
                    contentScale = ContentScale.Fit,
                    contentDescription = "speech bubble"
                )
                Text("${attachJosa(titleName,"을를")} 알려줄게!",
                    modifier = Modifier.padding(top=15.dp, start=20.dp, end=30.dp, bottom = 50.dp),
                    fontFamily = GmarketFont(), fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Ellipsis,)
            }
            Image(
                modifier = Modifier.offset(x=offsetX),
                bitmap = imageResource(Res.drawable.police),
                contentDescription = "police"
            )

        }
    }
}