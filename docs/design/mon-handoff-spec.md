# Mon: design handoff spec (Jetpack Compose)

Source of truth: `mon.penpot` (keep it beside this file). Screens are 360×800 dp phone frames. A rebuilt HTML version exists on the Design canvas but is not meant to be ported directly.

## Screens and navigation

| Screen | Purpose | Navigates to |
|---|---|---|
| Library | Featured text card, "ALL TEXTS" list, add FAB | Reader (tap a text), Import (FAB) |
| Import | Add file, paste text area, title field, Import button | Library (close X) |
| Reader | Japanese text with furigana, highlighted vocab, furigana toggle | Look up sheet (tap highlighted word), Library (back) |
| Look up sheet | Bottom sheet over Reader with dictionary entry | Reader (tap scrim) |
| Vocabulary | Search, JLPT filter chips, saved word cards | none |

Bottom nav has two tabs: Library (`local_library` icon) and Vocabulary (`dictionary` icon). The active tab sits in a 36×26 pill, `vermilion90` background, `vermilion50` icon. Inactive icons use `paper60`.

## Color tokens

| Token | Hex | Use |
|---|---|---|
| ink50 | #2B2723 | Primary text, outlines |
| paper90 | #FDFAF4 | Top bar, bottom bar, text on dark |
| paper80 | #F7F2E8 | Screen background |
| paper70 | #EDE7DA | Cards, import text area, inactive chips |
| paper65 | #D4CDCD | Placeholder text, secondary text on dark sheet |
| paper60 | #9A928A | Secondary text, inactive icons |
| vermilion50 | #C0392B | Accent: featured card, FAB, Import button, furigana, active icons |
| vermilion90 | #FDE3E3 | Active tab pill, word highlights, selected chips |
| vermilion80 | #D4AFAF | Chip borders, notes, subtitle on featured card |
| blue80 | #343449 | Look up sheet, "Add file" button |
| red70 | #BA1A1A | Error (defined, unused in screens) |
| white | #FFFFFF | Search and title inputs |

Also used in the screens but not tokenised: reader body text `#1A1714`, input border `#DFDCDC`, scrim `#000000` at 10%.

## Typography

All Roboto Mono unless noted. Japanese reader text and some vocabulary headwords use Inter in the file, falling back to a system CJK font. Consider Noto Sans JP for all Japanese text.

| Style | Size | Weight |
|---|---|---|
| Display Large / Medium / Small | 57 / 45 / 36 | 400 |
| Headline Large / Medium / Small | 32 / 28 / 24 | 400 |
| Title Large / Medium / Small | 22 / 18 / 14 | 700 / 700 / 500 |
| Body Large / Medium / Small / XS | 16 / 14 / 12 / 8 | 300 (Large has 0.1 letter spacing) |
| Label Large / Medium / Small / XS | 14 / 12 / 10 / 8 | 500 (XS is 700) |

Screen titles (Library, Vocabulary) use Headline Medium (28). Section header "ALL TEXTS" and the Import button label use Title Medium (18).

## Shape and spacing

- Screen corner radius token: 12 dp. Cards and list rows use 12, the featured card 16, the Import text area 25, the Import button 16, chips and toggle fully rounded.
- Layout rhythm: 39 dp horizontal screen margin (Import and the search field use 34–36). Top bar 50 dp below a 20 dp status area. Bottom bar 53 dp.
- Library: featured card 282×89, list rows 282×58 with 8 dp gap, FAB 49 dp circle with a 4 dp / 25% black shadow.
- Vocabulary: search field 36 dp high, pill shaped. Chips 36×20, 8 dp gap. Cards 58–68 dp high with 6 dp gap.

## Reader specifics

- Body: 19 sp, line pitch about 44 dp, letter spacing about 4.8 dp.
- Furigana: 8.5 sp, vermilion50, sits above the kanji.
- Highlighted vocab words get a vermilion90 rounded (5 dp) background behind the word.
- Header toggle: 33×19 pill, vermilion50, furigana icon in paper90. A disabled variant exists in the Penpot file.

## Look up sheet specifics

- Sheet fills the lower 465 dp, blue80, with a 60×4 white drag handle. Scrim over the text above.
- Headword Display Small (36) in paper90, reading in Label Medium (12) paper65, JLPT badge 36×20 on paper80 with paper60 text.
- Part-of-speech labels in Label Small (10) paper60, numbered senses in Body Medium (14) paper90.
- Example sentence in Body Small (12) paper65, Japanese then English.

## Icons

Material Symbols, 24 dp, filled weight 400: `local_library`, `dictionary`, `chevron_left`, `close`, `add` (36 dp in the FAB). The custom furigana icon (15 dp) is not from Material, so export it from the Penpot file as SVG and import it as a vector drawable.

Small paths in a 24×24 viewport:

- chevron_left: `M14 18L8 12L14 6L15.4 7.4L10.8 12L15.4 16.6L14 18Z`
- close: `M6.4 19L5 17.6L10.6 12L5 6.4L6.4 5L12 10.6L17.6 5L19 6.4L13.4 12L19 17.6L17.6 19L12 13.4L6.4 19Z`
- add (36 viewport): `M16.5 19.5L7.5 19.5L7.5 16.5L16.5 16.5L16.5 7.5L19.5 7.5L19.5 16.5L28.5 16.5L28.5 19.5L19.5 19.5L19.5 28.5L16.5 28.5L16.5 19.5Z`

## Known inconsistencies in the Penpot file

- The 先生 card in Vocabulary shows "memory; recollection", which looks copied from 記憶. It should probably read "teacher; master; doctor".
- Some secondary text is low contrast (paper60 on paper70, paper65 on blue80, 8 dp notes). Check against 4.5:1 before shipping.
- Font mix: Inter appears in Japanese text and the "›" glyphs while the system is Roboto Mono.
- Vocabulary list gaps and some card heights vary by a few pixels.

## Suggested Compose structure

- `MonTheme` with a custom `ColorScheme`, plus a small `MonColors` object for tokens that don't map to Material roles (paper and vermilion ramps).
- `Typography` mapped from the scale above using Roboto Mono via `FontFamily`.
- Screens: `LibraryScreen`, `ImportScreen`, `ReaderScreen` (with `ModalBottomSheet` for the look up), `VocabularyScreen`, wired with Navigation Compose and a two-item `NavigationBar`.
- Reader furigana: a custom layout or `Text` with inline content, since Compose has no built-in ruby support.
