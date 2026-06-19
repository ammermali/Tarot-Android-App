package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotAbility
import eu.mermali.tarot.game.gamestate.GameState
import eu.mermali.tarot.game.gamestate.VictoryReason
import eu.mermali.tarot.game.gamestate.Winner

class FinalEliminationRule {
    fun canAttemptFinalElimination(player: Player): Boolean =
        player.card?.canAttemptFinalElimination == true || player.card?.hasAbility(TarotAbility.FinalEliminator) == true
    fun resolve(state: GameState, eliminatorPlayerId: Int, targetPlayerId: Int): GameState {
        val eliminator = state.players.firstOrNull { it.id == eliminatorPlayerId }
            ?: throw IllegalArgumentException("Unknown eliminator player id: $eliminatorPlayerId")
        val target = state.players.firstOrNull { it.id == targetPlayerId }
            ?: throw IllegalArgumentException("Unknown target player id: $targetPlayerId")
        val isActiveOverride = state.activeFinalEliminatorPlayerId == eliminatorPlayerId
        require(canAttemptFinalElimination(eliminator)) { "This player cannot attempt final elimination." }
        val correctTarget = target.card?.isFinalEliminationTarget == true ||
            target.card?.hasAbility(TarotAbility.SeesReversed) == true
        val winner = if (correctTarget) { Winner(side = CardDirection.REVERSED, reason = VictoryReason.CORRECT_FINAL_ELIMINATION) }
        else { Winner(side = CardDirection.STRAIGHT, reason = VictoryReason.FAILED_FINAL_ELIMINATION) }
        return state.copy(winner = winner, finalEliminationTargetPlayerId = targetPlayerId, phase = GamePhase.GAME_OVER)
    }
}
