package com.example.mon.core.model.data

interface MyDictionary {
    fun tokenize(text: String): List<MyToken>
}
