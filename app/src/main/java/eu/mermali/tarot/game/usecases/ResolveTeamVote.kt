package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.gamerules.RejectionRule
import eu.mermali.tarot.domain.gamerules.TeamVotingRule
import eu.mermali.tarot.game.gamestate.GameState

class ResolveTeamVote(
    private val teamVotingRule: TeamVotingRule = TeamVotingRule(),
    private val rejectionRule: RejectionRule = RejectionRule()
) {
    operator fun invoke(state: GameState): GameState {
        require(state.phase == GamePhase.TEAM_VOTE_RESULT) { "Team vote result is not ready." }
        require(state.teamVotes.size == state.players.size) { "Every player must vote before resolving the team vote." }
        val outcome = teamVotingRule.resolve(state.teamVotes.values)
        val updatedState = rejectionRule.applyTeamVoteOutcome(state, outcome)
        val message = if (outcome.approved) {
            "Team approved (${outcome.approveCount} approve, ${outcome.rejectCount} reject). Reading starts."
        } else {
            "Team rejected (${outcome.approveCount} approve, ${outcome.rejectCount} reject). Rejections: ${updatedState.consecutiveRejectedTeams}."
        }
        val winnerMessage = updatedState.winner?.let { " Winner: ${it.side} (${it.reason})." } ?: ""
        return updatedState.withLog(message + winnerMessage)
    }
}
