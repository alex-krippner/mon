package com.example.mon.core.data.model

import com.example.mon.core.model.data.MyDictionary

interface DictionaryRepository {
    fun getDictionary(): MyDictionary
}