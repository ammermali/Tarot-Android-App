package eu.mermali.tarot.ui.cards
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.MissionResult
import eu.mermali.tarot.domain.model.TarotCard

object TarotCardImageResolver {
    fun artKeyAssetPath(artKey: String, skinId: String = DefaultSkinId): String? {
        return when {
            artKey.startsWith("basic_") -> artKey.cardAssetPath(CardKind.BASIC, skinId)
            artKey.startsWith("reading_") -> artKey.cardAssetPath(CardKind.READING, skinId)
            else -> null
        }
    }

    fun cardBackAssetPath(skinId: String = DefaultSkinId): String {return "$skinRoot/$skinId/card_backs.png"}

    fun roleCardAssetPath(card: TarotCard, seed: Int = card.id.hashCode()): String? {
        card.artKey?.let { artKey -> artKeyAssetPath(artKey)?.let { return it } }
    // TODO check
        return when {
            card.id.startsWith(StraightFaceCardPrefix) -> StraightFaceCardPreview
            card.id.startsWith(ReversedFaceCardPrefix) -> ReversedFaceCardPreview
            card.id == HermitCardId -> "$skinRoot/$DefaultSkinId/rolecards/reversed/hermit.png"
            card.id.startsWith("straight_") -> card.id.roleAssetPath(CardDirection.STRAIGHT)
            card.id.startsWith("reversed_") -> card.id.roleAssetPath(CardDirection.REVERSED)
            else -> null
        }
    }

    fun readingCardAssetPath(result: MissionResult, seed: Int, skinId: String = DefaultSkinId): String? {
        val artKey = when (result) {
            MissionResult.STRAIGHT -> ReadingStraightCards.pickBySeed(seed)
            MissionResult.REVERSED -> ReadingReversedCards.pickBySeed(seed)
            MissionResult.PENDING -> null
        }
        return artKey?.let { artKeyAssetPath(it, skinId) }
    }

    private fun List<String>.pickBySeed(seed: Int): String {
        val index = seed.floorMod(size)
        return this[index]
    }

    private fun Int.floorMod(divisor: Int): Int {
        return ((this % divisor) + divisor) % divisor
    }

    private fun readingCardKeys(direction: CardDirection): List<String> {
        val suits = listOf("cups", "pentacles", "swords", "wands")
        return suits.flatMap { suit ->
            (1..10).map { number ->
                val numberLabel = number.toString().padStart(2, '0')
                "reading_${direction.name.lowercase()}_${suit}$numberLabel"
            }
        }
    }

    private fun String.cardAssetPath(kind: CardKind, skinId: String): String? {
        val pattern = when (kind) {
            CardKind.BASIC -> Regex("^basic_(straight|reversed)_(.+)$")
            CardKind.READING -> Regex("^reading_(straight|reversed)_(.+)$")
        }
        val match = pattern.matchEntire(this) ?: return null
        val direction = match.groupValues[1]
        val cardName = match.groupValues[2]
        return "$skinRoot/$skinId/${kind.folderName}/$direction/$cardName.png"
    }

    private fun String.roleAssetPath(direction: CardDirection, skinId: String = DefaultSkinId): String {
        val directionName = direction.name.lowercase()
        val roleName = removePrefix("${directionName}_")
        return "$skinRoot/$skinId/rolecards/$directionName/$roleName.png"
    }

    private enum class CardKind(val folderName: String) {
        BASIC("basiccards"),
        READING("readingcards")
    }

    private const val skinRoot = "tarot_skins"
    private const val DefaultSkinId = "default"
    private const val StraightFaceCardPrefix = "straight_face_card"
    private const val ReversedFaceCardPrefix = "reversed_face_card"
    private const val StraightFaceCardPreview = "$skinRoot/$DefaultSkinId/basiccards/straight/cups11.png"
    private const val ReversedFaceCardPreview = "$skinRoot/$DefaultSkinId/basiccards/reversed/cups11.png"
    private const val HermitCardId = "the_hermit"

    private val ReadingStraightCards = readingCardKeys(CardDirection.STRAIGHT)
    private val ReadingReversedCards = readingCardKeys(CardDirection.REVERSED)
}
