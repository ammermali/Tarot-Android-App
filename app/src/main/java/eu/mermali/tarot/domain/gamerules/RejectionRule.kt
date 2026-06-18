package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.game.gamestate.GameState
import eu.mermali.tarot.game.gamestate.VictoryReason
import eu.mermali.tarot.game.gamestate.Winner

class RejectionRule {
    fun applyTeamVoteOutcome(state: GameState, outcome: TeamVoteSummary): GameState =
        if (outcome.approved) { state.copy(consecutiveRejectedTeams = 0, missionVotes = emptyList(), phase = GamePhase.MISSION_VOTING) }
        else { recordRejectedTeam(state) }

    fun recordRejectedTeam(state: GameState): GameState {
        val nextRejectedTeams = state.consecutiveRejectedTeams + 1
        val winner = if (nextRejectedTeams >= MAX_CONSECUTIVE_REJECTED_TEAMS) { Winner(side = CardDirection.REVERSED, reason = VictoryReason.FIVE_REJECTED_TEAMS) }
        else { null }
        return state.copy(
            consecutiveRejectedTeams = nextRejectedTeams,
            proposedTeam = emptyList(),
            teamVotes = emptyMap(),
            currentReaderPosition = state.currentReaderPosition + 1,
            winner = winner,
            phase = if (winner == null) GamePhase.TEAM_PROPOSAL else GamePhase.GAME_OVER
        )
    }

    companion object {
        const val MAX_CONSECUTIVE_REJECTED_TEAMS = 5
    }
}
