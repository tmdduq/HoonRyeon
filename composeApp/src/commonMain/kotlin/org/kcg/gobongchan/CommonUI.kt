package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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


@Composable
fun GmarketFont() = FontFamily(
    Font(resource = Res.font.GmarketSansTTFLight, weight = FontWeight.Light),
    Font(resource = Res.font.GmarketSansTTFMedium, weight = FontWeight.Medium),
    Font(resource = Res.font.GmarketSansTTFBold, weight = FontWeight.Bold)
)

@Composable
fun mainWindowBox(text:String, backgroundColor:Color, onClick:()->Unit, modifier: Modifier = Modifier, textFont: FontFamily){
    val itemModifier = Modifier
        .padding(4.dp)
        .clip(RoundedCornerShape(8.dp))
        .then(modifier)
    return Box(
        contentAlignment = Alignment.Center,
        modifier = itemModifier.clickable(
            onClick =onClick,
        ).background(backgroundColor)
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
) {
    Column(modifier.fillMaxWidth()) {
        //footer 경계선
        Divider(
            color= Color(0,0,0,128),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Spacer(modifier = Modifier.weight(1f))


            KakaoShareScreen()

//            Button(
//                modifier = Modifier.height(40.dp).width(100.dp),
//                onClick = onCall,
//                colors = ButtonDefaults.buttonColors(Color(10,21,75)),
//                enabled = true,
//            ) {
//                Text(text = "전화", color = Color.White, fontWeight = FontWeight.Bold)
//            }

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
        val screenWidth = LocalWindowInfo.current.containerSize.width / LocalDensity.current.density
        BoxWithConstraints {
            val maxWidth = constraints.maxWidth.dp
            FlowRow(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {


                for (s in nameList.sorted().sortedBy { it.length }) {
                    val contentsWidth = when{
                        s.replace(" ","").length <= 5 -> (screenWidth * 0.3f)
                        s.replace(" ","").length >= 10 -> (screenWidth * 0.9f)
                        else -> (screenWidth * 0.45f)
                    }
                    //val borderWidth = if(selectedCategoryName.isNullOrEmpty() || s!=selectedCategoryName) 0.dp else 5.dp
                    val scale by animateFloatAsState(
                        targetValue = when {
                            s == selectedCategoryName -> 1.4f
                            selectedCategoryName.isNullOrEmpty() -> 1f
                            else -> 0.99f
                        },
                        animationSpec = tween(durationMillis = 300),
                    )
                    val backgroundColor by animateColorAsState(
                        targetValue = when {
                            s == selectedCategoryName -> Color(0xffE00030)
                            selectedCategoryName.isNullOrEmpty() -> Color(color)
                            else -> Color(color)
                        },
                        animationSpec = tween(durationMillis = 300),
                    )
                    if(scale>=1) {
                        AnimatedVisibility(scale >= 1) {
                            mainWindowBox(
                                modifier = Modifier.heightIn(min = (70 * scale).dp)
                                    .widthIn(min = (contentsWidth * scale).dp)
                                    .graphicsLayer(scaleX = scale, scaleY = scale),
                                text = s, textFont = GmarketFont(),
                                backgroundColor = backgroundColor,
                                onClick = { onClick(s) }
                            ) //mainBox
                        }
                    }
                } // for
            } // flowRow
        }
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