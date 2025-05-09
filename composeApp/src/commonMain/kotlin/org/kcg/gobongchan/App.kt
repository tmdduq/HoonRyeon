package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ggobong.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import kotlin.collections.set


@Composable
fun App(windowSize :  Pair<Dp, Dp>) {
    val footerHeight = 70.dp
    val isVertical = when{
        windowSize.first == windowSize.second -> windowWidth() <= windowHeight()
        else -> windowSize.first <= windowSize.second
    }
    val mainDataList by loadCSV()

    val selectedCategoryMap = mutableMapOf<Int,String>()
    val categoryVerticalDepth = remember { mutableStateOf( 0 ) }
    val showPopup = remember { mutableStateOf(false) }
    var popupData = MainData()
    val showMain = remember { mutableStateOf(true) }
    val searchString = remember { mutableStateOf("") }

    registerBackHandler {
        println("onBack")
        searchString.value =""
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
                    modifier =
                        if(isVertical) Modifier.padding(top = 50.dp, bottom = 10.dp)
                        else Modifier.padding(top = 10.dp, bottom = 10.dp).height(50.dp),
                    painter = painterResource(Res.drawable.app_title_text),
                    contentScale = ContentScale.Fit,
                    contentDescription = "titleText",
                )


                Spacer(modifier = Modifier.padding(5.dp))
                val mainDataList : List<MainData> = mainDataList

                // 검색결과 표출
                AnimatedVisibility(searchString.value.isNotEmpty()){
                    val searchMap = getLastName(mainDataList, searchString.value)
                    drawMenu(
                        nameList = searchMap.keys.toSet(),
                        onClick = {
                            popupData = searchMap[it]!!
                            showPopup.value = true
                          },
                        color = KCGBlue,
                        selectedCategoryName = "",
                        visible = true
                    )
                }

                if(searchString.value.isEmpty()) {
                    for (i in 0 until LabelSize) {
                        var depDataList = mainDataList
                        if (i > 0)
                            for (j in 0 until i)
                                depDataList = depDataList.filter { it.map[cNameList[j + 1]] == selectedCategoryMap[j] }

                        val d = depDataList.map { it.map[cNameList[i + 1]] ?: "" }.filter { it != "" }.toSet()

                        drawMenu(
                            nameList = d,
                            visible = isBetween(categoryVerticalDepth.value, i, i + 1),
                            selectedCategoryName = selectedCategoryMap[i],
                            color = listOf(KCGDarkBlue, KCGBlue, 0xffff9040L, KCGYellow)[i % 4],
                            onClick = { s ->
                                if (i < LabelSize - 1) {
                                    //console.log("depth:${categoryVerticalDepth.value},\tbeforeNode(map[i]):${selectedCategoryMap[i]},\tclickNode=$s")
                                    val predNextNode = depDataList.filter { it.map[cNameList[i + 1]] == s }
                                        .map { it.map[cNameList[i + 2]] ?: "" }.filter { it != "" }.toSet()
                                    when {
                                        predNextNode.isEmpty() && i != 0 -> {
                                            println("last node")
                                            popupData =
                                                depDataList.filter { it.map[cNameList[i + 1]] == s }.firstOrNull()
                                                    ?: MainData()
                                            showPopup.value = true

                                            categoryVerticalDepth.value = i
                                            selectedCategoryMap[i] = if (selectedCategoryMap[i] != s) s else ""
                                        }

                                        !selectedCategoryMap[i].isNullOrEmpty() && selectedCategoryMap[i] != s -> {
                                            println("eq depth / diff node")
                                            categoryVerticalDepth.value = i
                                            categoryVerticalDepth.value = i + 1
                                        }

                                        categoryVerticalDepth.value != i -> {
                                            println("eq node")
                                            categoryVerticalDepth.value = i
                                        }

                                        else -> {
                                            println("diff depth / diff node")
                                            categoryVerticalDepth.value = i + 1
                                        }
                                    }
                                    selectedCategoryMap[i] = if (selectedCategoryMap[i] != s) s else ""
                                    pushHistoryState("${categoryVerticalDepth.value}")
                                } else {
                                    println("last depth")
                                    depDataList =
                                        depDataList.filter { it.map[cNameList[i]] == selectedCategoryMap[i - 1] }
                                    popupData = depDataList.firstOrNull { it.map[cNameList[i + 1]] == s } ?: MainData()
                                    showPopup.value = true
                                }
                            })
                    }
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
                    onCall = {},
                ) // end
            }
            //다운로드 버튼
            Box(
                modifier = Modifier.fillMaxWidth(1f).height(40.dp),
                contentAlignment = Alignment.CenterEnd) {
                InstallButton()
            }
            //메인화면
            AnimatedVisibility(categoryVerticalDepth.value<1 && searchString.value.isEmpty(),
                enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                exit= fadeOut(animationSpec = tween(durationMillis = 1000))){
                if(isVertical)
                    MainItemView(
                        {
                            pushHistoryState("1")
                            categoryVerticalDepth.value = 1
                            selectedCategoryMap[0] = it
                            showMain.value = false
                            println("showMain ${showMain.value}")
                        },
                        onValueChange = {searchString.value = it}
                    )
                else
                    MainItemViewHori({
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
fun KoreanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default
) {
    val regex = Regex("^[가-힣ㆍᆞᆢㄱ-ㅎㅏ-ㅣ]*$")

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || regex.matches(newValue)) {
                onValueChange(newValue)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray)
            .padding(8.dp),
        textStyle = textStyle,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}
@Composable
fun SearchBox(onValueChange:(s:String) -> Unit){
    val focusManager = LocalFocusManager.current
    var txt by remember { mutableStateOf("") }
    var load by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().height(70.dp),
        verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(load) {
            Box(Modifier.weight(0.7f).fillMaxWidth(0.7f), contentAlignment = Alignment.Center) {
                TextField(    //KoreanTextField(
                    value = txt,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp).background(Color.White).fillMaxWidth()
                        .align(Alignment.Center)
                        .onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown && it.key == Key.Enter) {
                                if(txt.length<2) return@onPreviewKeyEvent true
                                pushHistoryState("1")
                                onValueChange(txt)
                                focusManager.clearFocus()
                                return@onPreviewKeyEvent true
                            }
                            false
                        },
                    textStyle = TextStyle(
                        color = Color(KCGDarkBlue),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = GmarketFont()
                    ),
                    onValueChange = {
                        if(it.length<10) txt = it
                    },
                )
                if (txt.isEmpty()) {
                    onValueChange(txt)
                    Text(
                        text = "취합할 과정의 키워드를 입력하세요. ",
                        color = Color.DarkGray.copy(alpha = 0.7f),
                        fontFamily = GmarketFont(),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                }
            }
        }
        Box(
            modifier = Modifier.weight(0.3f).padding(10.dp)
                .fillMaxHeight().align(Alignment.CenterVertically)
                .border(width = 1.dp, color = Color.White)
                .clickable {
                    load = true
                    if(txt.length<2) return@clickable
                    pushHistoryState("1")
                    focusManager.clearFocus()
                    onValueChange(txt)},
            contentAlignment =  Alignment.Center,
            ){
            Text(
                text=if(load) "검색" else "찾는 내용을 검색해보세요.", color = Color.White,
                textAlign = TextAlign.Center, fontSize = 30.sp,
                overflow = TextOverflow.Ellipsis, maxLines = 1,
                fontFamily = GmarketFont()
            )
        }

    }
}

@Composable
fun MainItemView(onClick: (s:String) -> Unit, onValueChange:(s:String)->Unit){

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF0D1B2A)).padding(5.dp),
        verticalArrangement = Arrangement.Center
    ) {

        SearchBox(
            onValueChange = {onValueChange(it)}
        )

        Row(modifier = Modifier.fillMaxWidth().weight(1f),//.width(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
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
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
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
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
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
fun MainItemViewHori(onClick: (s:String) -> Unit){
    Row(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF0D1B2A)).padding(5.dp),
        horizontalArrangement = Arrangement.Center
    ) {

        Column(Modifier.fillMaxHeight().padding(start=10.dp, end=5.dp), verticalArrangement = Arrangement.Center){
            val text = "무엇을 찾나요?"
            text.reversed().forEach { char ->
                Text(
                    modifier = Modifier.padding(bottom = 3.dp).rotate(-90f),
                    text = "$char", color = Color.White,
                    fontFamily = GmarketFont(), fontSize = 30.sp,
                )
            }
        }

        Row(Modifier.fillMaxWidth(), Arrangement.Center,Alignment.CenterVertically,)
        {
            Column(modifier = Modifier.fillMaxHeight().weight(1f),
                verticalArrangement = Arrangement.Center) {
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
            Column(modifier = Modifier.fillMaxHeight().weight(1f),
                verticalArrangement = Arrangement.Center) {
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
            Column(modifier = Modifier.fillMaxHeight().weight(1f),
                verticalArrangement = Arrangement.Center) {
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
}

@Composable
fun MainMenuBox(modifier: Modifier, color: Long, painter: Painter, text:String, onClick:()->Unit){
    val isClick = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if(isClick.value) 2f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "scaleAnimation"
    )
    Box(modifier = modifier.padding(5.dp)/*.aspectRatio(1f)*/.fillMaxSize()
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



