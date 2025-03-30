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
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import org.jetbrains.compose.resources.painterResource
import org.w3c.dom.events.Event
import org.w3c.notifications.NotificationOptions
import org.w3c.workers.ServiceWorkerRegistration
import kotlin.js.Promise


fun registerServiceWorker(callback: (Boolean) -> Unit){
    // Service Worker 등록
    console.log("IN : registerServiceWorker()")

    // 서비스 워커가 지원되지 않으면 즉시 false 반환
    if (!window.navigator.serviceWorker.asDynamic() as Boolean) {
        console.log("[Error] Service Worker is not supported in this browser.")
        callback(false)
        return
    }

//    window.addEventListener("load", {
    window.navigator.serviceWorker.register("service-worker.js")
        .then(
            { registration -> {
                console.log("[regi ok] Service Worker: $registration.scope")
                callback(true)
                }
            },
            { error -> {
                console.log("[regi fail] Service Worker : $error")
                callback(false)
                }
            }
        ).then {
            console.log("[regi then] Service Worker : $it")
        }
//    })
    // 혹시 등록이 지연되거나 실패할 경우 대비하여 일정 시간이 지나도 `callback(false)` 호출
    window.setTimeout({
        console.log("[timeout] Service Worker registration timed out.")
        callback(false)
    }, 5000) // 5초 후에도 `then`이 실행되지 않으면 실패 처리
}

fun requestPushNotificationPermission(title: String, message: String, leftIcon:String, rightIcon:String) {
    console.log("IN : requestPushNotificationPermission()")
    Notification.requestPermission { permission ->
        if (permission == "granted") {
            console.log("[Permission ok]")
            showNotification(title, message, leftIcon,rightIcon)
        } else {
            console.log("[Permission denied] : $permission")
        }
    }
}

private fun showNotification(title: String, message: String, leftIcon:String, rightIcon:String) {
    console.log("IN : showNotification()")

    window.navigator.serviceWorker.ready.then({ registration ->
        console.log("[ready- ok] showNotification")
        val notificationOptions = NotificationOptions().apply {
            body = message
            icon = leftIcon
            badge = rightIcon
        }
        registration.showNotification(title, notificationOptions)
        console.log("[push- ok] showNotification")
    }).catch({ error ->
        console.log("[ready- fail] : $error")
    })
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

@JsName("Notification")
external class Notification(
    title: String,
    options: dynamic
) {
    companion object {
        fun requestPermission(callback: (String) -> Unit): Unit
    }
}

external class Options {
    var body: String?
    var icon: String?
    var badge: String?
}

@JsName("registration")
external  class registration(){
    companion object{
        fun showNotification(s:String, options:dynamic)
    }
}