package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.NoticeTone
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

/**
 * A short message about the screen's state: a [title] line and an optional [body], with an optional
 * inline link at the end. See [NoticeTone] for how each tone looks. Errors are announced politely
 * to screen readers as they appear.
 */
@Composable
fun MonNotice(
  title: String,
  tone: NoticeTone,
  modifier: Modifier = Modifier,
  body: String? = null,
  linkLabel: String? = null,
  onLinkClick: () -> Unit = {},
) {
  val colors = MonTheme.colors
  val icon =
    when (tone) {
      NoticeTone.Success -> Icon.Core.CheckCircle
      NoticeTone.Info -> Icon.Core.Info
      NoticeTone.Error -> Icon.Core.ErrorCircle
    }
  val iconColor =
    when (tone) {
      NoticeTone.Success -> colors.secondary
      NoticeTone.Info -> colors.textPrimary
      NoticeTone.Error -> colors.error
    }
  val card =
    when (tone) {
      NoticeTone.Success -> Modifier.clip(CARD_SHAPE).background(colors.accentTint).padding(12.dp)
      NoticeTone.Info -> Modifier.clip(CARD_SHAPE).background(colors.primary).padding(12.dp)
      NoticeTone.Error -> Modifier
    }

  Row(
    modifier = modifier.then(card).semantics { liveRegion = LiveRegionMode.Polite },
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.Top,
  ) {
    MonIcon(id = icon, primaryColor = iconColor, requiredSize = 18.dp)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = title,
        style = MonTheme.typography.labelMedium,
        color = if (tone == NoticeTone.Error) colors.error else colors.textPrimary,
      )
      if (body != null || linkLabel != null) {
        Text(
          text =
            buildAnnotatedString {
              if (body != null) append(body)
              if (linkLabel != null) {
                if (body != null) append(" ")
                withLink(
                  LinkAnnotation.Clickable(
                    tag = "notice-link",
                    styles =
                      TextLinkStyles(
                        SpanStyle(
                          fontWeight = FontWeight.Medium,
                          textDecoration = TextDecoration.Underline,
                        )
                      ),
                    linkInteractionListener = { onLinkClick() },
                  )
                ) {
                  withStyle(SpanStyle(color = colors.textPrimary)) { append(linkLabel) }
                }
              }
            },
          style = MonTheme.typography.bodySmall,
          color = colors.textPrimary,
        )
      }
    }
  }
}

private val CARD_SHAPE = RoundedCornerShape(12.dp)

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun MonNoticePreview() {
  MonTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      MonNotice(
        title = "JMdict (English) is ready.",
        body = "Tap a word in any text to see its meaning.",
        linkLabel = "Back to Library",
        tone = NoticeTone.Success,
      )
      MonNotice(
        title = "Could not check for dictionaries. Check your connection.",
        body = "The dictionaries you have still work.",
        tone = NoticeTone.Info,
      )
      MonNotice(title = "The download failed. Check your connection.", tone = NoticeTone.Error)
    }
  }
}
