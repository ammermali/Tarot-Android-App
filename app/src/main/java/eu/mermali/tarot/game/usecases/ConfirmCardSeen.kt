package eu.mermali.tarot.game.usecases
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.game.gamestate.GameState

class ConfirmCardSeen {
    operator fun invoke(state: GameState): GameState {
        require(state.phase == GamePhase.CARD_REVEAL) { "Card reveal is not active." }
        val player = state.currentRevealPlayer ?: throw IllegalStateException("There is no player to confirm.")
        val nextRevealPosition = state.currentRevealPosition + 1
        val nextPhase = if (nextRevealPosition >= state.players.size) { GamePhase.TEAM_PROPOSAL }
        else { GamePhase.CARD_REVEAL }
        val nextPosition = if (nextPhase == GamePhase.TEAM_PROPOSAL) { state.currentRevealPosition }
        else { nextRevealPosition }
        val reader = state.currentReader?.name ?: "-"
        val message = if (nextPhase == GamePhase.TEAM_PROPOSAL) {
            "${player.name} confirmed the card. The game starts. Reader: $reader."
        } else {
            "${player.name} confirmed the card."
        }
        return state.copy(currentRevealPosition = nextPosition, phase = nextPhase).withLog(message)
    }
}
