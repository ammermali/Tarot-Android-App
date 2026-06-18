package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.TeamVote
import eu.mermali.tarot.domain.gamerules.TeamVotingRule
import eu.mermali.tarot.game.gamestate.GameState

class SubmitTeamVote(private val teamVotingRule: TeamVotingRule = TeamVotingRule()) {
    operator fun invoke(state: GameState, playerId: Int, vote: TeamVote): GameState {
        require(state.phase == GamePhase.TEAM_VOTING) { "Team vote is not active." }
        val player = state.players.firstOrNull { it.id == playerId } ?: throw IllegalArgumentException("Unknown player id: $playerId")
        return teamVotingRule.submitVote(state, playerId, vote).withLog("${player.name} voted $vote on the team.")
    }
}
