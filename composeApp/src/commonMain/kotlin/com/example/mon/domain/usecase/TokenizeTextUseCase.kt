package com.example.mon.domain.usecase

import com.example.mon.domain.DictionaryRepository
import com.example.mon.domain.model.MyToken

class TokenizeTextUseCase(private val dictionaryRepository: DictionaryRepository) {
    fun execute(text: String): List<MyToken> {
        val dictionary = dictionaryRepository.getDictionary()
        return dictionary.tokenize(text)
    }
}
