package com.akrippner.mon.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Raw palette
// All hex values live here and nowhere else. Nothing outside this file should
// reference a raw Color(0xFF…) literal — use MonTheme.colors instead.
// ─────────────────────────────────────────────────────────────────────────────

internal object Palette {

  // Ink — warm near-blacks
  val Ink50 = Color(0xFF2B2723) // primary text (light theme)
  val Ink800 = Color(0xFF2C1810) // dark card gradient start
  val BlueInk = Color(0xFF1C1C2E)

  // Vermilion — light theme (original saturated red)
  val Vermilion90 = Color(0xFFFDE3E3)
  val Vermilion80 = Color(0xFFD4AFAF)
  val Vermilion50 = Color(0xFFC0392B)
  val Vermilion40 = Color(0xFFFDE3E3)

  // Vermilion — dark theme (Option B: lightened for ~6.1:1 AA contrast on dark bg)
  val Vermilion400Dark = Color(0xFFD4503F)
  val Vermilion300Dark = Color(0xFFE8624F)

  val Blue80 = Color(0xFF343449)

  // Paper — warm off-whites (light theme backgrounds)
  val Paper90 =
    Color(0xFFFDFAF4) // TODO: change numbering to be in line with  m3. 0 darkest and 100 brightest
  val Paper80 = Color(0xFFF7F2E8)
  val Paper70 = Color(0xFFEDE7DA)
  val Paper65 = Color(0xFFd4cdcd)
  val Paper60 = Color(0XFF9A928A)

  // Ash — warm off-white (dark theme primary text)
  val Ash50 = Color(0xFFF0E8DC)

  // Dark backgrounds — warm brown-blacks, three elevation stops
  val Dark950 = Color(0xFF17130F) // dark card gradient start
  val Dark900 = Color(0xFF1E1A16) // app canvas
  val Dark800 = Color(0xFF252018) // cards, nav bar
  val Dark700 = Color(0xFF2E2820) // inputs, list items

  // Night — lookup bottom sheet
  val Night900 = Color(0xFF1C1C2E) // light theme sheet
  val Night950 = Color(0xFF131018) // dark theme sheet (deeper, sits above dark bg)

  val White = Color(0xFFFFFFFF)
  val Red70 = Color(0xFFBA1A1A)
  val Grey = Color(0xFFCCCCCC)
}

interface BaseComponentColor {
  val background: Color
  val text: Color
}

data class ButtonColors(val background: Color, val text: Color, val icon: Color)

data class FilterPillColors(
  val background: Color,
  val text: Color,
  val icon: Color,
  val border: Color,
)

data class InputColors(
  val background: Color,
  val border: Color,
  val cursor: Color,
  val placeholder: Color,
  val text: Color,
)

data class FeaturedColors(val background: Color, val title: Color, val subtitle: Color)

data class ListItemColors(
  val background: Color,
  val title: Color,
  val subtitle: Color,
  val iconEnd: Color,
)

data class LookupColors(
  val background: Color,
  val title: Color,
  val reading: Color,
  val definition: Color,
  val example: Color,
  val partsOfSpeech: Color,
)

data class VocabEntry(
  val title: Color,
  val reading: Color,
  val note: Color,
  val background: Color,
  val subtitle: Color,
)

data class IconColors(val default: Color, val selected: Color, val selectedBackground: Color)

data class NavigationColors(
  val text: Color,
  val background: Color,
  val default: Color,
  val selected: Color,
  val selectedBackground: Color,
)

data class TokenizedText(val background: Color, val selected: Color)

data class FuriganaToggleColors(val background: Color, val icon: Color, val disabled: Color)

data class MonColors(
  // Backgrounds
  val primary: Color,
  val surfacePrimary: Color, // rename to surface
  val surfaceSecondary: Color, // rename to surfaceTint
  val surfaceElevated: Color, // remove?
  val backgroundSecondary: Color, // rename to tertiary
  val appBackground: Color,
  // Components
  val buttonPrimary: ButtonColors,
  val buttonSecondary: ButtonColors,
  val filterPillDefault: FilterPillColors,
  val filterPillSelected: FilterPillColors,
  val input: InputColors,
  val tag: ButtonColors,
  val featured: FeaturedColors,
  val listItem: ListItemColors,
  val lookup: LookupColors,
  val navigation: NavigationColors,
  val tokenizedText: TokenizedText,
  val vocabEntry: VocabEntry,
  val furiganaToggle: FuriganaToggleColors,
  // Text
  val textPrimary: Color, // rename to onPrimary
  val textSecondary: Color,
  val textTertiary: Color,
  val textDisabled: Color,
  // Accent
  val secondary: Color,
  val accentGradientEnd: Color,
  val accentSubtle: Color,
  val accentMuted: Color,
  /** vermilion90: a light accent background, e.g. a success notice. */
  val accentTint: Color,
  /** Error messages. */
  val error: Color,
  /** paper90: surfaces raised above the screen, such as a dialog. */
  val surfaceRaised: Color,
  // Borders & dividers
  val borderDefault: Color,
  val borderFocus: Color,
  val divider: Color,
  val accentBarActive: Color,
  val accentBarInactive: Color,
  // Featured card dark gradient
  val cardDarkStart: Color,
  val cardDarkEnd: Color, // rename to surface
  // Lookup bottom sheet
  val sheetBackground: Color, // rename to surfaceVariant
  val sheetSurface: Color, // remove?
  val sheetTextPrimary: Color, // rename to onSurfaceVariant
  val sheetTextSecondary: Color,
  val sheetTextTertiary: Color,
  val sheetDivider: Color,
  val sheetInputBg: Color,
  val sheetInputBorder: Color,
  val isDark: Boolean,
)

// ─────────────────────────────────────────────────────────────────────────────
// Light theme instance
// ─────────────────────────────────────────────────────────────────────────────

internal val lightMonColors =
  MonColors(
    primary = Palette.Paper70, // make 50
    surfacePrimary = Palette.Paper80, // make 100
    surfaceSecondary = Palette.Paper80, // make 200 rename to surfaceTint
    surfaceElevated = Palette.Paper80,
    backgroundSecondary = Palette.Ink50,
    textPrimary = Palette.Ink50,
    textSecondary = Palette.Ink50.copy(alpha = 0.45f),
    textTertiary = Palette.Ink50.copy(alpha = 0.30f),
    textDisabled = Palette.Ink50.copy(alpha = 0.22f),
    appBackground = Palette.Paper80,
    buttonPrimary =
      ButtonColors(
        background = Palette.Vermilion50,
        text = Palette.Paper90,
        icon = Palette.Paper90,
      ),
    buttonSecondary =
      ButtonColors(background = Palette.Blue80, text = Palette.Paper90, icon = Palette.Paper90),
    filterPillDefault =
      FilterPillColors(
        background = Palette.Paper70,
        text = Palette.Paper60,
        icon = Palette.Paper60,
        border = Color.Unspecified,
      ),
    filterPillSelected =
      FilterPillColors(
        background = Palette.Vermilion90,
        text = Palette.Vermilion50,
        icon = Palette.Vermilion50,
        border = Palette.Vermilion80,
      ),
    input =
      InputColors(
        background = Palette.White,
        border = Palette.Grey,
        cursor = Palette.Vermilion50,
        placeholder = Palette.Paper65,
        text = Palette.Paper60,
      ),
    tag =
      ButtonColors(background = Palette.Paper80, text = Palette.Paper60, icon = Color.Unspecified),
    featured =
      FeaturedColors(
        background = Palette.Vermilion50,
        title = Palette.White,
        subtitle = Palette.Vermilion80,
      ),
    listItem =
      ListItemColors(
        background = Palette.Paper70,
        title = Palette.Ink50,
        subtitle = Palette.Paper60,
        iconEnd = Palette.Ink50,
      ),
    lookup =
      LookupColors(
        background = Palette.Blue80,
        title = Palette.Paper90,
        reading = Palette.Paper65,
        definition = Palette.Paper90,
        example = Palette.Paper65,
        partsOfSpeech = Palette.Paper60,
      ),
    navigation =
      NavigationColors(
        text = Palette.Ink50,
        background = Palette.Paper90,
        default = Palette.Paper60,
        selected = Palette.Vermilion50,
        selectedBackground = Palette.Vermilion90,
      ),
    tokenizedText = TokenizedText(background = Palette.Vermilion90, selected = Palette.Vermilion50),
    vocabEntry =
      VocabEntry(
        title = Palette.Ink50,
        background = Palette.Paper70,
        reading = Palette.Vermilion50.copy(alpha = 0.85f),
        note = Palette.Vermilion50,
        subtitle = Palette.Paper60,
      ),
    furiganaToggle =
      FuriganaToggleColors(
        background = Palette.Vermilion50,
        icon = Palette.Paper90,
        disabled = Palette.Paper60,
      ),
    secondary = Palette.Vermilion50,
    accentGradientEnd = Palette.Vermilion40,
    accentSubtle = Palette.Vermilion50.copy(alpha = 0.10f),
    accentMuted = Palette.Vermilion50.copy(alpha = 0.55f),
    accentTint = Palette.Vermilion90,
    error = Palette.Red70,
    surfaceRaised = Palette.Paper90,
    borderDefault = Palette.Ink50.copy(alpha = 0.08f),
    borderFocus = Palette.Vermilion50,
    divider = Palette.Ink50.copy(alpha = 0.07f),
    accentBarActive = Palette.Vermilion50.copy(alpha = 0.90f),
    accentBarInactive = Palette.Ink50.copy(alpha = 0.16f),
    cardDarkStart = Palette.Ink800,
    cardDarkEnd = Palette.Ink50,
    sheetBackground = Palette.Night900, // use BlueInk
    sheetSurface = Palette.White.copy(alpha = 0.10f),
    sheetTextPrimary = Palette.White,
    sheetTextSecondary = Palette.White.copy(alpha = 0.45f),
    sheetTextTertiary = Palette.White.copy(alpha = 0.30f),
    sheetDivider = Palette.White.copy(alpha = 0.08f),
    sheetInputBg = Palette.White.copy(alpha = 0.08f),
    sheetInputBorder = Palette.White.copy(alpha = 0.12f),
    isDark = false,
  )
