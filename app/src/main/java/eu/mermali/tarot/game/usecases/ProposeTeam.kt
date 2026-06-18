package eu.mermali.tarot.game.usecases
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.gamerules.TeamProposalRule
import eu.mermali.tarot.game.gamestate.GameState

class ProposeTeam(private val teamProposalRule: TeamProposalRule = TeamProposalRule()) {
    operator fun invoke(state: GameState, playerIds: List<Int>): GameState {
        require(state.phase == GamePhase.TEAM_PROPOSAL) { "Team can be proposed only during team proposal." }
        val updatedState = teamProposalRule.proposeTeam(state, playerIds)
        val teamNames = updatedState.proposedTeam.joinToString { it.name }
        val readerName = state.currentReader?.name ?: "-"
        return updatedState.withLog("$readerName proposed a team: $teamNames.")
    }
}
