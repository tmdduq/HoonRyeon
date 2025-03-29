package org.kcg.gobongchan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ggobong.composeapp.generated.resources.*
import ggobong.composeapp.generated.resources.Res
import kotlinx.browser.window
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.w3c.dom.Window
import kotlin.js.Date

data class Chat(
    val message: String,
    val time: String,
    val isOutgoing: Boolean,
    val linkData :ChatLinkData? = null
)

data class ChatLinkData(
    val thumbnail: DrawableResource,
    val title: String,
    val detail: String,
    val link :String
)

val message = mutableStateOf("")

val chats = mutableStateListOf<Chat>(
    Chat("교육에 대해 상세히 알려줄게!", getTime(), false),
)

fun chatsInit(data: Map<String, Any>){
    chats.add(Chat("<${data["훈련명"]}>에 대해 알려주세요!",getTime(), true))

    data.filter { it.key !in listOf("연번", "부서대분류", "부서중분류", "분야", "훈련분류", "첨부제목", "첨부내용", "파일명 또는 링크")  }
        .forEach { (k,v) ->
            if(!v.toString().isNullOrEmpty())
                chats.add(Chat("$k : $v",getTime(), false))
    }
    if(data["첨부제목"] != null){
        val fName = data["파일명 또는 링크"] as String
        val linkData = ChatLinkData(extNameIcon(fName),
            "${data["첨부제목"]}",
            "${data["첨부내용"]}",
            "${data["파일명 또는 링크"]}")
        chats.add(Chat("첨부파일을 참고하세요.",getTime(), false, linkData))
    }
    val t = ChatLinkData(extNameIcon("sample.pdf"), "샘플입니다", "훈련교범입니다.\n클릭하면 볼 수 있어요.","https://youtube.com")
    chats.add(Chat("훈련교범을 참고하세요.",getTime(), false, t))


}

const val username = "Mr.고 선생님"
const val isOnline = false

@OptIn(InternalResourceApi::class)
@Composable
fun ChatScreen(popupData : MainData, onBack:() -> Unit) {
    LaunchedEffect(popupData) {
        chatsInit(popupData.map)
    }



    Box(
        modifier = Modifier.fillMaxSize()
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
            MessageSection()
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
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
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
                    imageVector = Icons.Default.ArrowBack,
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

            Column {
                Text(text = username, fontWeight = FontWeight.SemiBold, fontFamily = GmarketFont())
                Text(
                    text = if (isOnline) "Online" else "문의: 063-928-2318",
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
    LaunchedEffect(chats.size) {
        listState.animateScrollToItem(chats.size - 1)
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        state = listState
    ) {
        items(chats) { chat ->
            if(chat.time.length>1)
                MessageItem(
                    chat.message,
                    chat.time,
                    chat.isOutgoing,
                    chat.linkData
                )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun getTime() : String{
    val now = Date(Date.now())
    var hours = now.getHours()
    val minutes = "${now.getMinutes()}".padStart(2,'0')
    val second = "${now.getSeconds()}".padStart(2,'0')
    val ampm = if(hours>=12) "PM" else "AM"
    hours = hours % 12
    val hour = if(hours==0) "12" else "$hours".padStart(2,'0')
    return "$hour:$minutes $ampm"
}

@Composable
fun MessageSection() {
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
                    }
                )

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
        )
    }
}

@Composable
fun MessageItem(
    messageText: String,
    time: String,
    isOut: Boolean,
    linkData: ChatLinkData?=null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOut) Alignment.End else Alignment.Start
    ) {
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

        if(linkData!=null)
            Box(
                modifier = Modifier.padding(top=8.dp)
                    .background(Color.White)
                    .wrapContentHeight()
                    .heightIn(max = 100.dp)
                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable { window.open(url=linkData.link, target = "_blank") }
            ){
                Row{
                    Image(
                        modifier = Modifier
                            .weight(0.25f)
                            .padding(end = 16.dp).align(Alignment.CenterVertically),
                        painter = painterResource(linkData.thumbnail) , contentDescription = "thumbnail"
                    )
                    Box(
                        Modifier.fillMaxHeight()
                            .width(1.dp)
                            .background(Color.LightGray))
                    Column(
                        modifier = Modifier.weight(0.75f).padding(start = 16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center)
                    {
                        Text(linkData.title, fontFamily = GmarketFont(), fontSize = 15.sp,)
                        Divider(thickness = 1.dp)
                        Text(linkData.detail, fontFamily = GmarketFont(), fontSize = 12.sp,)
                    }

                }

            }
        Text(
            text = time,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}



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