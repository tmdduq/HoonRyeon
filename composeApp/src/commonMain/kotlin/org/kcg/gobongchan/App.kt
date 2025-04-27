package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ggobong.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import kotlin.collections.set


@Composable
fun App() {
    val footerHeight = 70.dp
    val rstMap = remember{ mutableStateOf(commonMap.toMutableMap()) }
    val mainDataList by loadCSV()
    val selectedCategoryMap = mutableMapOf<Int,String>()
    val categoryVerticalDepth = remember { mutableStateOf( 0 ) }
    val showPopup = remember { mutableStateOf(false) }
    var popupData = MainData()
    val showMain = remember { mutableStateOf(true) }
    registerBackHandler {
        println("onBack")
        if(showPopup.value) showPopup.value = false
        else {
            if(categoryVerticalDepth.value>0)
                categoryVerticalDepth.value--
            selectedCategoryMap[categoryVerticalDepth.value] = ""
        }
    }
    LaunchedEffect(Unit){
        addPWA()
    }

    MaterialTheme{
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = footerHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    modifier = Modifier.padding(top = 50.dp, bottom = 10.dp),
                    painter = painterResource(Res.drawable.app_title_text),
                    contentDescription = "titleText",
                )

//                Text(
//                    modifier = Modifier.padding(start = 40.dp, bottom = 10.dp),
//                    text = "꼭 맞게 찾아드려요!",
//                    fontFamily = GmarketFont(),
//                    fontSize = 40.sp
//                )

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
                                pushHistoryState("${categoryVerticalDepth.value}")
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
            AnimatedVisibility(showPopup.value, enter = fadeIn(), exit= fadeOut()){
                ChatScreen(
                    popupData = popupData,
                    onBack = {
                        showPopup.value = false
                    },
                )
            }

            //하단바 보이기숨기기
            AnimatedVisibility(
                visible = !showPopup.value,
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            ) {
                //하단바
                Footer(
                    modifier = Modifier.height(footerHeight).fillMaxWidth(),
                    onCall = {
                        rstMap.value = commonMap.toMutableMap().apply { this["rstTitle"] = "이렇게이렇게..." }
                    },
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

            AnimatedVisibility(categoryVerticalDepth.value<1,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                exit= fadeOut(animationSpec = tween(durationMillis = 1000))){
                MainItemView({
                    pushHistoryState("1")
                    categoryVerticalDepth.value = 1
                    selectedCategoryMap[0] = it
                    showMain.value = false
                    println("showMain ${showMain.value}")
                })
            }


        } // box
    }
}



@Composable
fun MainItemView(onClick: (s:String) -> Unit){

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF0D1B2A)).padding(5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(top = 50.dp, bottom = 10.dp),
            text = "무엇을 찾나요?", color = Color.White,
            fontFamily = GmarketFont(),
            fontSize = 40.sp
        )
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            MainMenuBox(
                Modifier.weight(1f),
                0xFF079ad9,
                painterResource(Res.drawable.menu1),
                "교육",
                { onClick("교육") }
            )
            MainMenuBox(
                Modifier.weight(1f),
                0xFF4CAF50,
                painterResource(Res.drawable.menu2),
                "수사",
                { onClick("수사") }
            )
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            MainMenuBox(
                Modifier.weight(1f),
                0xFFFFC107,
                painterResource(Res.drawable.menu3),
                "장비숙지",
                { onClick("장비숙지") }
            )
            MainMenuBox(
                Modifier.weight(1f),
                0xFF6200EE,
                painterResource(Res.drawable.menu4),
                "지형지물",
                { onClick("지형지물") }
            )
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            MainMenuBox(
                Modifier.weight(1f),
                0xFFFF4081,
                painterResource(Res.drawable.menu5),
                "나라배움터",
                { onClick("나라배움터") }
            )
            MainMenuBox(
                Modifier.weight(1f),
                0xFF00B1D2,
                painterResource(Res.drawable.menu6),
                "직무역량평가",
                { onClick("직무역량평가") }
            )
        }
    }

}

@Composable
fun MainMenuBox(modifier: Modifier, color: Long, painter: Painter, text:String, onClick:()->Unit){
    val isClick = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if(isClick.value) 2f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "scaleAnimation"
    )
    Box(modifier = modifier.padding(5.dp).aspectRatio(1f)
        .border(width = 5.dp, color = Color(color), shape = CutCornerShape(10.dp))
        .clickable {
            isClick.value = true
            onClick() },
        contentAlignment = Alignment.Center){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painter,
                contentDescription = "menu",
                colorFilter = ColorFilter.tint(Color(color)),
                modifier = Modifier.fillMaxSize(0.4f * scale)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                modifier = Modifier.padding(),
                text = text, color = Color(color),
                fontFamily = GmarketFont(),
                fontSize = 25.sp
            )
        }
    }
}



