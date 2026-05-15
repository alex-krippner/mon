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
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
  val state by viewModel.state.collectAsState()

  LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    items(state.tokens) { token ->
      Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "Surface: ${token.text}")
        Text(text = "Parts of Speech: ${token.partOfSpeech.joinToString()}")
        Text(text = "Reading: ${token.readingForm}")
        Text(text = "Dict Form: ${token.dictionaryForm}")
      }
    }
  }
}
