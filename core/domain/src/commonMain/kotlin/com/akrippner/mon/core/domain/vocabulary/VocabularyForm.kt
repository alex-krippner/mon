package com.akrippner.mon.core.domain.vocabulary

import com.akrippner.mon.core.model.data.MonToken

/**
 * The form a saved word is identified by: the token's dictionary form, or its surface if the
 * tokenizer gave none. A reading has at most one saved word per form, and the Reader highlights
 * every token with a saved form.
 */
fun MonToken.vocabularyForm(): String = dictionaryForm.ifBlank { surface }
