package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.configuration.PlayerCountConfig
import eu.mermali.tarot.domain.configuration.PlayerCountRules
import eu.mermali.tarot.domain.configuration.TarotCardPreset
import eu.mermali.tarot.domain.configuration.TarotCardValidationResult
import eu.mermali.tarot.domain.configuration.TarotCardValidator
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.TarotCard
import eu.mermali.tarot.game.gamestate.GameState

class SelectCards(
    private val tarotCardPreset: TarotCardPreset = TarotCardPreset(),
    private val playerCountConfig: PlayerCountConfig = PlayerCountConfig(),
    private val tarotCardValidator: TarotCardValidator = TarotCardValidator()
) {
    fun availableCards(): List<TarotCard> = tarotCardPreset.defaultDeck()

    fun rulesFor(state: GameState): PlayerCountRules =
        playerCountConfig.forPlayerCount(state.players.size)
            ?: throw IllegalStateException("Set player count before selecting cards.")

    fun validationFor(state: GameState): TarotCardValidationResult =
        tarotCardValidator.validate(state.players.size, state.selectedCards)

    fun canAdd(state: GameState, card: TarotCard): Boolean {
        val rules = rulesFor(state)
        if (state.selectedCards.any { it.id == card.id }) { return false }
        return when (card.direction) {
            CardDirection.STRAIGHT -> state.selectedCards.count { it.direction == CardDirection.STRAIGHT } < rules.straightCards
            CardDirection.REVERSED -> state.selectedCards.count { it.direction == CardDirection.REVERSED } < rules.reversedCards
        }
    }

    fun add(state: GameState, cardId: String): GameState {
        require(state.phase == GamePhase.CARD_SETUP) { "Cards can be selected only during card setup." }
        val card = availableCards().firstOrNull { it.id == cardId } ?: throw IllegalArgumentException("Unknown card id: $cardId")
        require(canAdd(state, card)) { "Cannot add ${card.displayName} to this setup deck." }
        return state.copy(selectedCards = state.selectedCards + card).withLog("${card.displayName} added to setup deck.")
    }

    fun remove(state: GameState, cardId: String): GameState {
        require(state.phase == GamePhase.CARD_SETUP) { "Cards can be removed only during card setup." }
        val card = state.selectedCards.firstOrNull { it.id == cardId } ?: throw IllegalArgumentException("Card is not selected: $cardId")
        return state.copy(selectedCards = state.selectedCards.filterNot { it.id == cardId }).withLog("${card.displayName} removed from setup deck.")
    }

    fun fillDefault(state: GameState): GameState {
        val cards = tarotCardPreset.defaultSelection(state.players.size)
        return state.copy(selectedCards = cards).withLog("Default setup deck selected.")
    }
}
