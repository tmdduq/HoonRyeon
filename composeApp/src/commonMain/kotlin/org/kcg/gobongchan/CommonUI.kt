package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ggobong.composeapp.generated.resources.GmarketSansTTFBold
import ggobong.composeapp.generated.resources.GmarketSansTTFLight
import ggobong.composeapp.generated.resources.GmarketSansTTFMedium
import ggobong.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font
import kotlin.random.Random


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
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (s in nameList) {
                val screenWidth = LocalWindowInfo.current.containerSize.width / LocalDensity.current.density
                //val borderWidth = if(selectedCategoryName.isNullOrEmpty() || s!=selectedCategoryName) 0.dp else 5.dp
                val scale by animateFloatAsState(
                    targetValue = when {
                        s==selectedCategoryName -> 1.4f
                        selectedCategoryName.isNullOrEmpty() -> 1f
                        else -> 0.99f
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "scaleAnimation"
                )
                AnimatedVisibility(scale>=1) {
                    mainWindowBox(
                        modifier = Modifier.height((70*scale).dp).wrapContentWidth().widthIn(min = (screenWidth * 0.3f * scale).dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale),
                        text = s, textFont = GmarketFont(),
                        backgroundColor = if(scale!=1.4f) color else 0xffE00030,
                        onClick = { onClick(s) }
                    ) //mainBox
                }
            } // for
        } // flowRow
    } // ani
}

@Composable
fun makeDialog(rstMap : MutableMap<String, String?>, onClose:()-> Unit){
    val title = rstMap["rstTitle"]?:""
    val message = rstMap["rstMessage"]?:""
    val footer = if(rstMap["rstCode"]=="1") "화면구성은 이런식으로 처리하고...., " else rstMap["errorMessage"]?:""
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
        chats.add(Chat("참고자료","첨부파일을 확인하세요.", Random.nextBoolean(), linkData))
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

fun Modifier.bottomBorder(color: Color, thickness: Dp) = this.then(
    Modifier.drawBehind {
        val stroke = thickness.toPx()
        val y = size.height - stroke / 2
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = stroke
        )
    }
)