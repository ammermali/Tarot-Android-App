package eu.mermali.tarot.domain.configuration
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.TarotAbility
import eu.mermali.tarot.domain.model.TarotCard

class TarotCardPreset {
    fun defaultDeck(): List<TarotCard> = listOf(
        TarotCard(
            id = "straight_high_priestess",
            displayName = "Straight High Priestess",
            direction = CardDirection.STRAIGHT,
            abilities = setOf(TarotAbility.SeesReversed)
        ),
        TarotCard(
            id = "straight_hierophant",
            displayName = "Straight Hierophant",
            direction = CardDirection.STRAIGHT,
            abilities = setOf(TarotAbility.SeesOracles)
        ),
        *faceCards(
            idPrefix = "straight_face_card",
            displayName = "Straight Face Card",
            direction = CardDirection.STRAIGHT,
            count = 6
        ).toTypedArray(),
        TarotCard(
            id = "straight_magician",
            displayName = "Straight Magician",
            direction = CardDirection.STRAIGHT,
            abilities = setOf(TarotAbility.CanCastMagic)
        ),
        TarotCard(
            id = "straight_devil",
            displayName = "Straight Devil",
            direction = CardDirection.STRAIGHT,
            abilities = setOf(TarotAbility.AppearsReversed, TarotAbility.SeesFinalEliminator)
        ),
        TarotCard(
            id = "reversed_death",
            displayName = "Reversed Death",
            direction = CardDirection.REVERSED,
            abilities = setOf(TarotAbility.FinalEliminator)
        ),
        TarotCard(
            id = "reversed_high_priestess",
            displayName = "Reversed High Priestess",
            direction = CardDirection.REVERSED,
            abilities = setOf(TarotAbility.AppearsAsOracle)
        ),
        *faceCards(
            idPrefix = "reversed_face_card",
            displayName = "Reversed Face Card",
            direction = CardDirection.REVERSED,
            count = 4
        ).toTypedArray(),
        TarotCard(
            id = "reversed_tower",
            displayName = "Reversed Tower",
            direction = CardDirection.REVERSED,
            abilities = setOf(TarotAbility.HiddenFromSight)
        ),
        TarotCard(
            id = "reversed_devil",
            displayName = "Reversed Devil",
            direction = CardDirection.REVERSED,
            abilities = setOf(TarotAbility.IsolatedReversed)
        ),
        TarotCard(
            id = "reversed_magician",
            displayName = "Reversed Magician",
            direction = CardDirection.REVERSED,
            abilities = setOf(TarotAbility.CanCastMagic)
        )
    )

    fun defaultSelection(playerCount: Int): List<TarotCard> {
        val config = PlayerCountConfig().forPlayerCount(playerCount)
            ?: throw IllegalArgumentException("Unsupported player count: $playerCount")
        val straightCards = defaultDeck()
            .filter { it.direction == CardDirection.STRAIGHT }
            .take(config.straightCards)
        val reversedCards = defaultDeck()
            .filter { it.direction == CardDirection.REVERSED }
            .take(config.reversedCards)
        return straightCards + reversedCards
    }

    private fun faceCards(
        idPrefix: String,
        displayName: String,
        direction: CardDirection,
        count: Int
    ): List<TarotCard> {
        return (1..count).map { index ->
            TarotCard(
                id = "${idPrefix}_$index",
                displayName = displayName,
                direction = direction
            )
        }
    }
}
