package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.data.model.DictionaryRepository
import com.akrippner.mon.core.model.data.MyToken

class TokenizeTextUseCase(private val dictionaryRepository: DictionaryRepository) {
    fun execute(text: String): List<MyToken> {
        val dictionary = dictionaryRepository.getDictionary()
        return dictionary.tokenize(text)
    }
}
