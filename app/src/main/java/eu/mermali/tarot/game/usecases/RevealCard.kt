package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.gamerules.InitialVisibility
import eu.mermali.tarot.domain.gamerules.TarotVisibilityRule
import eu.mermali.tarot.game.gamestate.GameState

data class CardReveal(
    val player: Player,
    val visibleInformation: List<InitialVisibility>
)

class RevealCard(private val visibilityRule: TarotVisibilityRule = TarotVisibilityRule()) {
    operator fun invoke(state: GameState): CardReveal {
        require(state.phase == GamePhase.CARD_REVEAL) { "Cards can be revealed only during card reveal." }
        val player = state.currentRevealPlayer ?: throw IllegalStateException("There is no player to reveal.")
        val information = visibilityRule.visibilityFor(state.players).filter { it.viewerPlayerId == player.id }
        return CardReveal(player = player, visibleInformation = information)
    }
}
