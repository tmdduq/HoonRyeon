package org.kcg.gobongchan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.events.Event


@OptIn(ExperimentalComposeUiApi::class)
fun main(){
    onWasmReady{
        CanvasBasedWindow{
            //Greeting().greet()
            ResponsiveApp()
        }
    }
}

@Composable
fun ResponsiveApp() {
    val width = remember { mutableStateOf((window.innerWidth)) }
    val height = remember { mutableStateOf((window.innerWidth)) }

    LaunchedEffect(Unit) {
        val listener: (Event) -> Unit = {
            width.value = window.innerWidth
            height.value = window.innerHeight
        }
        window.addEventListener("resize", listener)
    }
    App(Pair(width.value.dp, height.value.dp) )
}