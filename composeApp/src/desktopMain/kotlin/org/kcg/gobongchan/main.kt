package org.kcg.gobongchan

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ggobong.composeapp.generated.resources.Res

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "GGobong",
    ) {

        Greeting().greet()
        App()
    }
}