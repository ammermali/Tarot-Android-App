package eu.mermali.tarot.domain.model

data class Player(
    val id: Int,
    val name: String,
    val position: Int,
    val card: TarotCard? = null
)
