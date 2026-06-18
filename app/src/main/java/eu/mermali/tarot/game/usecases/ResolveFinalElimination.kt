package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.gamerules.FinalEliminationRule
import eu.mermali.tarot.game.gamestate.GameState

class ResolveFinalElimination(private val finalEliminationRule: FinalEliminationRule = FinalEliminationRule()) {
    operator fun invoke(state: GameState, eliminatorPlayerId: Int, targetPlayerId: Int): GameState {
        require(state.phase == GamePhase.FINAL_ELIMINATION) { "Final elimination is not active." }
        val eliminator = state.players.firstOrNull { it.id == eliminatorPlayerId } ?: throw IllegalArgumentException("Unknown eliminator player id: $eliminatorPlayerId")
        val target = state.players.firstOrNull { it.id == targetPlayerId } ?: throw IllegalArgumentException("Unknown target player id: $targetPlayerId")
        val updatedState = finalEliminationRule.resolve(state, eliminatorPlayerId, targetPlayerId)
        return updatedState.withLog("${eliminator.name} chose ${target.name} for final elimination. Winner: ${updatedState.winner?.side} (${updatedState.winner?.reason}).")
    }
}
