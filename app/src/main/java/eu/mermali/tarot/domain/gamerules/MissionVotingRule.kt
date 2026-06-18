package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.MissionVote
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.game.gamestate.GameState

class MissionVotingRule {
    fun canCastMissionVote(player: Player, vote: MissionVote): Boolean {
        val card = player.card ?: return false
        return when (card.direction) {
            CardDirection.STRAIGHT -> vote == MissionVote.STRAIGHT
            CardDirection.REVERSED -> true
        }
    }

    fun submitVote(state: GameState, playerId: Int, vote: MissionVote): GameState {
        val player = state.players.firstOrNull { it.id == playerId } ?: throw IllegalArgumentException("Unknown player id: $playerId")
        val mission = state.currentMission ?: throw IllegalStateException("There is no current mission.")
        val proposedTeam = if (state.proposedTeam.isNotEmpty()) { state.proposedTeam }
        else { mission.proposedTeam }
        require(proposedTeam.any { it.id == playerId }) { "Only proposed team members can cast mission votes." }
        require(canCastMissionVote(player, vote)) { "STRAIGHT cards can only cast STRAIGHT mission votes." }
        val updatedVotes = state.missionVotes + vote
        require(updatedVotes.size <= mission.requiredPlayerCount) { "Mission ${mission.index} already has all required votes." }
        return state.copy(
            missionVotes = updatedVotes,
            phase = if (updatedVotes.size == mission.requiredPlayerCount) { GamePhase.MISSION_RESULT }
            else { GamePhase.MISSION_VOTING }
        )
    }
}
