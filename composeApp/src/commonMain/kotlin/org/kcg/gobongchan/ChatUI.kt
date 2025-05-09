package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ggobong.composeapp.generated.resources.*
import kotlinx.coroutines.delay
import okio.ByteString.Companion.encodeUtf8
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.skia.Image
import kotlin.random.Random

val chats = mutableStateListOf<Chat>(
    //Chat("교육에 대해 상세히 알려줄게!", getTime(), false),
)

fun chatsInit(data: Map<String, String>): String{
    // value.takeIf{Boolean}이란: True- return value // False- return null
    val title = cNameList.subList(0, LabelSize + 1).asReversed()
        .firstNotNullOfOrNull { s ->
            data[s]?.takeIf { it.isNotEmpty() }
        } ?: ""

    data.filter { it.key !in cNameList.subList(0, LabelSize+1)  }
        .filter { it.key !in listOf("첨부제목", "첨부내용", "파일명 또는 링크", "위경도")  }
        .forEach { (k,v) ->
            if(v.isNotEmpty())
                chats.add(Chat(k, v, Random.nextBoolean()))
        }

    if("${data["첨부제목"]}".isNotEmpty()){
        val fName = data["파일명 또는 링크"] as String
        val linkData = ChatLinkData(extNameIcon(fName),
            "${data["첨부제목"]}",
            "${data["첨부내용"]}",
            "${data["파일명 또는 링크"]}")
        chats.add(Chat("참고자료","첨부파일을 확인해보세요.", Random.nextBoolean(), linkData))
    }
    if("${data["위경도"]}".isNotEmpty()){
        val yxString = data["위경도"] as String
        val yxSplit = yxString.split(",")
        val latSplit = yxSplit[0].trim().split("-")
        val lonSplit = yxSplit[1].trim().split("-")
        val lat = latSplit[0].toDouble() + (latSplit[1].toDouble()/60) + (latSplit[2].toDouble()/3600)
        val lon = lonSplit[0].toDouble() + (lonSplit[1].toDouble()/60) + (lonSplit[2].toDouble()/3600)
        println("lat $lat, lon $lon")
        chats.add(Chat("지도위치","$title 여기에요!", Random.nextBoolean(),null, xyData = Pair(lon, lat) ))
    }

    return title
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
    val imageNumber by remember { mutableStateOf(Random.nextInt(7) ) }

    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 10.dp),
        backgroundColor = Color.White,//(151,153,156),
        elevation = 1.dp
    ) {
        Row(
            Modifier.background(
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
                    lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontSize=14.sp, lineHeight = 16.sp)
                )

            }

            Image(
                modifier = Modifier.offset(x=offsetX),
                bitmap = when(imageNumber) {
                    0 -> imageResource(Res.drawable.police0)
                    1 -> imageResource(Res.drawable.police1)
                    2 -> imageResource(Res.drawable.police2)
                    3 -> imageResource(Res.drawable.police3)
                    4 -> imageResource(Res.drawable.police4)
                    5 -> imageResource(Res.drawable.police5)
                    else -> imageResource(Res.drawable.police6)
                },
                contentDescription = "police"
            )

        }
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
                    imageVector = vectorResource(Res.drawable.arrow_back),
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
                openUrl("https://map.kakao.com/link/map/${detail.encodeUtf8().utf8()},$lat,$lon")
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


