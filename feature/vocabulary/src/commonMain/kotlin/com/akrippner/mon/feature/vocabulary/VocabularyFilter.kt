package com.akrippner.mon.feature.vocabulary

import com.akrippner.mon.core.model.data.JlptLevel
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.VocabularyGroup

/** How many of a reading's newest words the tab shows before the "+ N more" row. */
internal const val TAB_PREVIEW_COUNT = 2

/**
 * The sections to list for [groups].
 *
 * Without a search or a level, a section shows its [previewCount] newest words and how many more
 * there are, so a long reading does not crowd the others out (the tab; the list of one reading
 * passes [previewCount] = null and shows all). With a search or a level, every match is shown and
 * readings without a match are left out.
 */
internal fun buildSections(
  groups: List<VocabularyGroup>,
  query: String,
  levels: Set<JlptLevel>,
  previewCount: Int?,
): List<VocabularySection> {
  val filtering = query.isNotBlank() || levels.isNotEmpty()
  return groups.mapNotNull { group ->
    if (filtering) {
      val matches = group.words.filter { it.matches(query, levels) }
      if (matches.isEmpty()) null
      else VocabularySection(group.readingId, group.title, matches.size, matches, moreCount = 0)
    } else {
      val shown = if (previewCount == null) group.words else group.words.take(previewCount)
      VocabularySection(
        group.readingId,
        group.title,
        wordCount = group.words.size,
        words = shown,
        moreCount = group.words.size - shown.size,
      )
    }
  }
}

/**
 * Whether the word has one of [levels] (any, if empty) and its word, kana, gloss or note contains
 * every space-separated part of [query], ignoring case.
 */
internal fun SavedWord.matches(query: String, levels: Set<JlptLevel>): Boolean {
  if (levels.isNotEmpty() && jlpt !in levels) return false
  val parts = query.trim().split(' ').filter { it.isNotEmpty() }
  if (parts.isEmpty()) return true
  val haystack = listOfNotNull(dictionaryForm, surface, kana, gloss, note).joinToString("\n")
  return parts.all { haystack.contains(it, ignoreCase = true) }
}
