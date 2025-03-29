package org.kcg.gobongchan

class JvmPlatform: Platform {
    override val name: String = "Web with Kotlin/Desktop"
}

actual fun getPlatform() : Platform = JvmPlatform()

