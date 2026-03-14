package com.akrippner.mon.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.YomuTheme
import com.akrippner.mon.core.data.model.DictionaryRepository
import com.akrippner.mon.core.model.data.MyDictionary
import com.akrippner.mon.core.model.data.MyToken
import com.akrippner.mon.core.domain.usecase.TokenizeTextUseCase

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(state.tokens) { token ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(text = "Surface: ${token.surface}")
                Text(text = "Parts of Speech: ${token.partOfSpeech.joinToString()}")
                Text(text = "Normalized: ${token.normalizedForm}")
                Text(text = "Reading: ${token.reading}")
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    YomuTheme {
        val fakeDictionary = object : MyDictionary {
            override fun tokenize(text: String): List<MyToken> {
                return listOf(
                    MyToken("すもも", listOf("名詞"), "李", "スモモ"),
                    MyToken("も", listOf("助詞"), "も", "モ"),
                    MyToken("もも", listOf("名詞"), "桃", "モモ")
                )
            }
        }
        val fakeRepository = object : DictionaryRepository {
            override fun getDictionary(): MyDictionary = fakeDictionary
        }
        val fakeUseCase = TokenizeTextUseCase(fakeRepository)
        val fakeViewModel = HomeViewModel(fakeUseCase)

        HomeScreen(viewModel = fakeViewModel)
    }
}