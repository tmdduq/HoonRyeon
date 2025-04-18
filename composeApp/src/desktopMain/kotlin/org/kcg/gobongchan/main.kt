package org.kcg.gobongchan

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 450.dp, height = 800.dp
        ),
        title = "GGobong",
    ) {

        Greeting().greet()
        App()
    }
}