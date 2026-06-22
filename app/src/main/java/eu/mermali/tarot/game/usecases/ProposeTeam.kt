package eu.mermali.tarot.game.usecases
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.gamerules.TeamProposalRule
import eu.mermali.tarot.game.gamestate.GameState

class ProposeTeam(private val teamProposalRule: TeamProposalRule = TeamProposalRule()) {
    operator fun invoke(state: GameState, playerIds: List<Int>, watchTokenPlayerId: Int? = null): GameState {
        require(state.phase == GamePhase.TEAM_PROPOSAL) { "Team can be proposed only during team proposal." }
        val updatedState = teamProposalRule.proposeTeam(state, playerIds, watchTokenPlayerId)
        val teamNames = updatedState.proposedTeam.joinToString { it.name }
        val readerName = state.currentReader?.name ?: "-"
        val watchMessage = watchTokenPlayerId ?.let {id -> updatedState.proposedTeam.firstOrNull { it.id == id}} ?.let {player -> "Watch token: ${player.name}."} .orEmpty()
        return updatedState.withLog("$readerName proposed a team: $teamNames.$watchMessage")
    }
}
