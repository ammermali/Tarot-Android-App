package eu.mermali.tarot.ui.cards

data class TarotSkin(val id: String, val displayName: String)

object TarotSkinCatalog {
    const val DefaultSkinId = "default"

    val availableSkins: List<TarotSkin> = listOf(
        TarotSkin(id = "default", displayName="Default"),
        TarotSkin(id="cats", displayName="Cats")
    )
    fun displayNameFor(id: String): String{
        return availableSkins.firstOrNull {it.id == id}?.displayName ?: id
    }
}