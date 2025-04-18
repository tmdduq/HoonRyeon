package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ggobong.composeapp.generated.resources.*
import ggobong.composeapp.generated.resources.Res
import ggobong.composeapp.generated.resources.pallate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    MaterialTheme{
        Box(
            modifier = Modifier.fillMaxSize().padding(40.dp)
                .background(Color.Black.copy(0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(Color(0xfff2f2f2)))
            {
                TopBarSection(
                    "장비수사요우미우 운용법 교육",
                    "ㅁ이ㅏㄴㅁㄹ ㅁㅇ라ㅣㅁ너라ㅣ",
                    painterResource(Res.drawable.pallate))
                Appdata("기간", "ㅁㄴㄹㄴㅇ월까지", true)
                Appdata("세부내용", " ㄴ참아추ㅏㄴㅇ닐ㄴ리ㅏㅁㄴㄹ", false)
                Appdata("관련법령", "ㄴ리ㅏㄴ머리ㅏㄴㅁㅇ러ㅏㅇㄴ 해야 한다.!!!!", true)
                Appdata("동영상 보기", "누르면 동영상이 나와요.",false, true)
                Appdata("지도보기", "누르면 동영상이 나와요.",true, true)
            }

        }
    }
}

@Composable
fun Appdata(
    subtitle : String,
    detail : String,
    isOut:Boolean,
    link : Boolean = false){
    val layoutDirection =
        if(isOut) LocalLayoutDirection provides LayoutDirection.Rtl
        else LocalLayoutDirection provides LayoutDirection.Ltr
    CompositionLocalProvider(layoutDirection) {
        Column(Modifier.fillMaxWidth().wrapContentHeight().widthIn(min=100.dp)) {
            Box(
                modifier = Modifier.padding(8.dp)
                    .fillMaxWidth(0.9f)
                    .background(Color(0xffffffff), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
            ) {
                Row{
                    if(link) {
                        Icon(
                            painter = painterResource(Res.drawable.moive_64dp),
                            modifier = Modifier.padding(25.dp).size(50.dp),
                            contentDescription = "thumbnail"
                        )
                    }
                    Column {
                        Text(
                            "$subtitle",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.Bold
                        )
                        Row{
                            Text(
                                detail,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Left
                            )
                        }

                    }
                }
            }

        }
    }

}

@Composable
fun TopBarSection(
    username: String,
    describeText:String,
    profile: Painter,
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        backgroundColor = Color(151,153,156),
        elevation = 1.dp
    ) {

    }
}