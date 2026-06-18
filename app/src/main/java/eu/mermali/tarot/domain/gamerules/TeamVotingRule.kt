package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.TeamVote
import eu.mermali.tarot.game.gamestate.GameState

data class TeamVoteSummary(val approveCount: Int, val rejectCount: Int, val approved: Boolean)

class TeamVotingRule {
    fun submitVote(state: GameState, playerId: Int, vote: TeamVote): GameState {
        require(state.players.any { it.id == playerId }) {"Unknown player id: $playerId"}
        val updatedVotes = state.teamVotes + (playerId to vote)
        val nextPhase = if (updatedVotes.size == state.players.size) { GamePhase.TEAM_VOTE_RESULT }
        else { GamePhase.TEAM_VOTING }
        return state.copy(teamVotes = updatedVotes, phase = nextPhase)
    }

    fun resolve(votes: Collection<TeamVote>): TeamVoteSummary {
        val approveCount = votes.count { it == TeamVote.APPROVE }
        val rejectCount = votes.count { it == TeamVote.REJECT }
        return TeamVoteSummary(approveCount = approveCount, rejectCount = rejectCount, approved = approveCount > rejectCount)
    }
}
