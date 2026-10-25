package com.akrippner.mon.core.model.data

interface MyDictionary {
  fun tokenize(text: String): List<MonToken>
}
