package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.MissionVote
import eu.mermali.tarot.domain.gamerules.MissionVotingRule
import eu.mermali.tarot.game.gamestate.GameState

class SubmitMissionVote(private val missionVotingRule: MissionVotingRule = MissionVotingRule()) {
    operator fun invoke(state: GameState, playerId: Int, vote: MissionVote): GameState {
        require(state.phase == GamePhase.MISSION_VOTING) { "Mission vote is not active." }
        val player = state.players.firstOrNull { it.id == playerId } ?: throw IllegalArgumentException("Unknown player id: $playerId")
        val updatedState = missionVotingRule.submitVote(state, playerId, vote)
        val mission = updatedState.currentMission ?: throw IllegalStateException("There is no current mission.")
        return updatedState.withLog("${player.name} submitted a reading vote: $vote (${updatedState.missionVotes.size}/${mission.requiredPlayerCount}).")
    }
}
