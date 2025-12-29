package com.humblecoders.aromex_android_windows

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform