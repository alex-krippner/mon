package com.example.mon.core.domain.usecase

import com.example.mon.core.data.model.DictionaryRepository
import com.example.mon.core.model.data.MyToken

class TokenizeTextUseCase(private val dictionaryRepository: DictionaryRepository) {
    fun execute(text: String): List<MyToken> {
        val dictionary = dictionaryRepository.getDictionary()
        return dictionary.tokenize(text)
    }
}
