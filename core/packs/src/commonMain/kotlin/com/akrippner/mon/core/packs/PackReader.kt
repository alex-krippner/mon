package com.akrippner.mon.core.packs

/**
 * What every installed pack opens into. It only says which pack it was opened from; what a reader
 * can answer is a capability interface that the reader implements next to it, such as
 * `MonDictionary` for word lookups. Capabilities do not extend this interface, so something that is
 * not a pack (an online service, say) can implement them too.
 */
interface PackReader {
  val pack: CatalogPack
}

/**
 * The reader was closed because its pack is being updated or removed, so it can answer nothing. Any
 * call on any reader can throw it. This is not "nothing found": fetch the readers again and repeat
 * the call.
 */
class PackClosedException : IllegalStateException("The pack reader is closed")
