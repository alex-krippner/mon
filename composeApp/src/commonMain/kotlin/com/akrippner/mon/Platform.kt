package com.akrippner.mon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform