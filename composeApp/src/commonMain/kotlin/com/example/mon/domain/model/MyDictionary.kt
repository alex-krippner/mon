package com.example.mon.domain.model

interface MyDictionary {
    fun tokenize(text: String): List<MyToken>
}
