package com.akrippner.mon.core.data.model

import com.akrippner.mon.core.model.data.MyDictionary

interface DictionaryRepository {
  fun getDictionary(): MyDictionary
}
