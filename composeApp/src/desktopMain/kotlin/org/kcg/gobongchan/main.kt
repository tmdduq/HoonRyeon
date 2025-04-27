package org.kcg.gobongchan

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(
        width = 450.dp, height = 800.dp,
    )
        Window(
        onCloseRequest = ::exitApplication, state = windowState,
        title = "부안, 교육훈련 지원 플랫폼",
    ) {
        //Greeting().greet()
        App(Pair(windowState.size.width, windowState.size.height-50.dp))
    }
}
