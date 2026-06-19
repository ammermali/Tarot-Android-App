package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.TarotAbility
import eu.mermali.tarot.game.gamestate.GameState
import eu.mermali.tarot.game.gamestate.VictoryReason
import eu.mermali.tarot.game.gamestate.Winner

class VictoryRule {
    fun evaluate(state: GameState): Winner? = when {
        state.reversedScore >= MISSIONS_TO_WIN -> Winner(side = CardDirection.REVERSED, reason = VictoryReason.THREE_REVERSED_MISSIONS)
        state.consecutiveRejectedTeams >= RejectionRule.MAX_CONSECUTIVE_REJECTED_TEAMS -> Winner(side = CardDirection.REVERSED, reason = VictoryReason.FIVE_REJECTED_TEAMS)
        state.straightScore >= MISSIONS_TO_WIN && !canRunFinalElimination(state) -> Winner(side = CardDirection.STRAIGHT, reason = VictoryReason.THREE_STRAIGHT_MISSIONS)
        else -> null
    }

    fun applyAfterMission(state: GameState): GameState {
        val winner = evaluate(state)
        if (winner != null) {return state.copy(winner = winner, phase = GamePhase.GAME_OVER) }
        if (state.straightScore >= MISSIONS_TO_WIN && canRunFinalElimination(state)) {
            return if (hasStraightDevil(state) && hasReversedDeath(state)){
                state.copy(phase = GamePhase.DEVIL_GUESS)
            } else {
                state.copy(phase = GamePhase.FINAL_ELIMINATION)
            }
        }
        return state.copy(
            currentMissionIndex = state.currentMissionIndex + 1,
            currentReaderPosition = state.currentReaderPosition + 1,
            proposedTeam = emptyList(),
            teamVotes = emptyMap(),
            missionVotes = emptyList(),
            phase = GamePhase.TEAM_PROPOSAL
        )
    }

    private fun hasFinalEliminationTarget(state: GameState): Boolean =
        state.players.any { player -> player.card?.isFinalEliminationTarget == true || player.card?.hasAbility(TarotAbility.SeesReversed) == true }

    private fun hasFinalEliminator(state: GameState): Boolean =
        state.players.any { player -> player.card?.canAttemptFinalElimination == true || player.card?.hasAbility(TarotAbility.FinalEliminator) == true }

    private fun canRunFinalElimination(state: GameState): Boolean =
        hasFinalEliminationTarget(state) && hasFinalEliminator(state)

    private fun hasStraightDevil(state: GameState): Boolean = state.players.any { it.card?.id == "straight_devil" }
    private fun hasReversedDeath(state: GameState): Boolean = state.players.any { it.card?.id == "reversed_death" }

    companion object {
        const val MISSIONS_TO_WIN = 3
    }
}
