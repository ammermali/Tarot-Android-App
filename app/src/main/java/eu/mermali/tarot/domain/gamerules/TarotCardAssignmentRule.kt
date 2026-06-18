package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotCard
import kotlin.random.Random

class TarotCardAssignmentRule {
    fun assign(players: List<Player>, cards: List<TarotCard>, random: Random = Random.Default): List<Player> {
        require(players.size == cards.size) { "Player count must match selected card count." }
        val shuffledCards = cards.shuffled(random).map { card -> card.withAssignedFaceArt(random) }
        return players.sortedBy { it.position }.zip(shuffledCards).map { (player, card) -> player.copy(card = card) }
    }

    private fun TarotCard.withAssignedFaceArt(random: Random): TarotCard {
        val artPool = when {
            id.startsWith(StraightFaceCardIdPrefix) -> StraightFaceArtKeys
            id.startsWith(ReversedFaceCardIdPrefix) -> ReversedFaceArtKeys
            else -> return this
        }

        return copy(artKey = artPool.random(random))
    }

    private companion object {
        const val StraightFaceCardIdPrefix = "straight_face_card"
        const val ReversedFaceCardIdPrefix = "reversed_face_card"

        val StraightFaceArtKeys = faceArtKeys("straight")
        val ReversedFaceArtKeys = faceArtKeys("reversed")

        fun faceArtKeys(direction: String): List<String> {
            val suits = listOf("cups", "pentacles", "swords", "wands")
            return suits.flatMap { suit ->
                (11..14).map { number -> "basic_${direction}_${suit}$number" }
            }
        }
    }
}
