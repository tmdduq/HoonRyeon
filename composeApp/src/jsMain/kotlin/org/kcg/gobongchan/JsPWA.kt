package org.kcg.gobongchan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ggobong.composeapp.generated.resources.Res
import ggobong.composeapp.generated.resources.kcg_128x128
import kotlinx.browser.window
import org.jetbrains.compose.resources.painterResource
import org.w3c.dom.events.Event
import kotlin.js.Promise


fun registerServiceWorker(){
    // Service Worker 등록
    window.addEventListener("load", {
        window.navigator.serviceWorker.register("/service-worker.js").then(
            { registration -> println("Service Worker 등록 성공: $registration") },
            { error -> println("Service Worker 등록 실패: $error") }
        )
    })
    // + Mobile
//    registerServiceWorkerForMobile()
}


fun ignoreBackKey(){
    window.history.pushState(null, "", window.location.href)
    window.onpopstate = {
        window.history.pushState(null, "", window.location.href)
        window.location.replace(window.location.href)
        println("Back navigation is disabled!")
    }
}

@Composable
fun InstallButton() {
    var deferredPrompt by remember { mutableStateOf<dynamic>(null) }
    val isReady = remember { mutableStateOf(false) }

    // beforeinstallprompt 이벤트 리스너 등록
    DisposableEffect(Unit) {
        val installEventListener: (Event) -> Unit = { event ->
            event.preventDefault() // 기본 동작 방지
            deferredPrompt = event.asDynamic() // deferredPrompt에 이벤트 저장
            isReady.value = true // 버튼을 표시하도록 설정
        }
        // `beforeinstallprompt` 이벤트 리스너 추가
        window.asDynamic().addEventListener("beforeinstallprompt", installEventListener)
// Composable이 사라질 때 이벤트 리스너 제거
        onDispose {
            window.asDynamic().removeEventListener("beforeinstallprompt", installEventListener)
        }
    }

    AnimatedVisibility(isReady.value) {
        Row(Modifier.fillMaxHeight().background(Color(KCGDarkBlue)).clickable {
            isReady.value = false
            deferredPrompt?.prompt() // PWA 설치 프롬프트 표시
            try {
                val userChoice: Promise<Promise<dynamic>> =
                    deferredPrompt?.userChoice.unsafeCast<Promise<Promise<dynamic>>>()
                console.log(userChoice)
                userChoice.then { choiceResult ->
                    val outcome = choiceResult.outcome
                    when (outcome) {
                        "accepted" -> console.log("User accepted the install prompt")
                        "dismissed" -> console.log("User dismissed the install prompt")
                        else -> console.log("Unknown outcome")
                    }
                }
            } catch (e: ClassCastException) {
                console.log("classCast Exception : $e")
            }
        }) {
            Image(
                painter = painterResource(Res.drawable.kcg_128x128),
                contentDescription = "App install",
                modifier = Modifier.size(20.dp).padding(start = 5.dp).align(Alignment.CenterVertically)
            )
            Text("App 설치", fontFamily = GmarketFont(), fontSize = 15.sp, color = Color.White,
                modifier= Modifier.align(Alignment.CenterVertically).padding(end = 5.dp))
        }
    }
}