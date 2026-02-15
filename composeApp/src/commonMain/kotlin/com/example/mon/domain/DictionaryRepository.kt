package com.example.mon.domain

import com.example.mon.domain.model.MyDictionary

interface DictionaryRepository {
    fun getDictionary(): MyDictionary
}
