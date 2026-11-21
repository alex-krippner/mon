# Publishing packs

How to publish dictionary and tokenizer packs to the `mon-data` repository. This is a release
runbook; the rules that matter when changing code (catalog fields, key normalization, adding a
source) are in `AGENTS.md` under "Dictionary packs". Do not read `*.db`, `jmdict-*.json` or
`system_core.dic` files; they are large.

## Catalog and release rules

The app reads only the latest release's `catalog.json`, so that one file must list every pack of
every source.

- A pack id is `<source id>-<…>`, and a source owns that prefix: its run replaces all of its packs
  in the catalog and keeps everything else. So always convert all of a source's packs in one run
  (for JMdict: all languages); the converter refuses a run that would drop one, unless it gets
  `--allow-drop`. No source id may be `<another id>-…`; the converter checks this.
- Never delete an old `mon-data` release or its files. Pack URLs are absolute, and the current
  catalog points to other sources' archives in earlier releases.
- Every release marked latest must contain `catalog.json`, or the app gets a 404.
- Never remove or rename a `CatalogPack` field, and every pack carries every field with a valid
  value. The app decodes the whole catalog before it filters, so one pack with a missing field
  makes the whole catalog `CatalogError.INVALID` for older apps. A kind without a gloss language
  repeats its source language.
- A new `CatalogPack` field must have a default value. Otherwise the installed `pack.json` files
  stop decoding and the packs vanish, while `sweep()` keeps their folders. The converter reads the
  previous catalog strictly, so an older converter stops instead of dropping the new field.
- New formats, and new fields old apps can ignore, are fine: an app hides packs whose format,
  schema or language it does not know.
- A change that old readers can't read needs a new `schema`. Known limit: older apps then get no
  more updates for that pack, because `publish` lists each id once.

## Publishing a JMdict data update

The licence requires keeping the data current, so do this regularly, at least monthly.

1. From the same release at https://github.com/scriptin/jmdict-simplified/releases, download the
   full single-language files you publish (`jmdict-eng-<version>.json.tgz`,
   `jmdict-ger-<version>.json.tgz`; not the `common` or `all` variants) and unpack them. The
   languages the converter knows are the entries of `GlossLanguage`. Every file must come from the
   same release, because all packs of a run share one `dictDate` and release tag.
2. Run the converter:
   `./gradlew :tools:pack-converter:run --args="jmdict <outDir> <path>/jmdict-eng-<version>.json <path>/jmdict-ger-<version>.json"`
   The first argument is the source. The language is read from each file. It writes one
   `jmdict-<lang>-<dictDate>.db.gz` per file plus a single `catalog.json`, and prints the release
   command. Options go between the source and `<outDir>`:
   - `--repo <owner/repo>` names the repository that will hold the release.
   - By default the converter first downloads the `catalog.json` of that repository's latest
     release and carries the other sources' packs over. If that fails, it stops before converting.
     `--previous-catalog <file>` reads a local file instead.
   - `--no-previous` starts a catalog with only this run's packs. Use it only for a repository's
     very first release: it drops every other source's packs.
   - If the previous catalog has a pack of this source that the run does not build (a forgotten
     language file, for example), the run fails before it writes anything. `--allow-drop`
     publishes anyway and removes that pack from the catalog on purpose.
3. Create the release in `mon-data` with the printed command (`gh release create jmdict-<dictDate>
   … --latest`), which uploads every `.db.gz` and the `catalog.json`. Do this before converting
   another source: the merge reads the *published* catalog, so two conversions without a release in
   between lose the first one's packs.

## Publishing the Sudachi dictionary

Only when a new SudachiDict release is wanted.

1. Download `sudachi-dictionary-<date>-core.zip` from
   https://github.com/WorksApplications/SudachiDict/releases and unpack it.
2. Run
   `./gradlew :tools:pack-converter:run --args="sudachi <outDir> <path>/system_core.dic"`.
   The version is the date in the file's header description (or its build date). The run carries
   the JMdict packs over from the latest release, so the release it prints covers every source;
   create it as for JMdict, before converting another source.
3. The attribution text is `ATTRIBUTION` in `SudachiSource.kt`; check it against the LEGAL file of
   the SudachiDict release.

The format is `sudachi-system`, schema 1. A different Sudachi library version that can't read the
file needs a new `schema`, so that older apps hide the pack.

## Licence

JMdict is © the EDRDG, CC BY-SA 4.0. The app must show a pack's `attribution` text on a separate
screen (`:feature:dictionaries`, done), and `mon-data` carries the licence in its README.
