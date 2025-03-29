package org.kcg.gobongchan

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform