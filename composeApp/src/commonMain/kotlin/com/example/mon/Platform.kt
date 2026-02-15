package com.example.mon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform