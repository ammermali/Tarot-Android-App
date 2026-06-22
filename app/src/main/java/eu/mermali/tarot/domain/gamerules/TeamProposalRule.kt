package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.Mission
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.game.gamestate.GameState

class TeamProposalRule {
    fun proposeTeam(state: GameState, playerIds: List<Int>, watchTokenPlayerId: Int?): GameState {
        val mission = state.currentMission ?: throw IllegalStateException("There is no current mission.")
        val team = playerIds.map { playerId -> state.players.firstOrNull { it.id == playerId } ?: throw IllegalArgumentException("Unknown player id: $playerId")}
        validateTeam(mission, team)
        if(watchTokenPlayerId != null){
            require(watchTokenPlayerId in playerIds){ "Watch token can only be assigned to a player on the proposed team."}
            if(state.players.size in 5..6){
                require(state.currentMissionIndex >= 2){ "With 5-6 players, watch token can only be assigned from the third mission onward."}
            }
        }
        val updatedMissions = state.missions.map { existingMission ->
            if (existingMission.index == mission.index) { existingMission.copy(proposedTeam = team, watchTokenPlayerId = watchTokenPlayerId) }
            else { existingMission }
        }
        return state.copy(proposedTeam = team, teamVotes = emptyMap(), missions = updatedMissions, phase = GamePhase.TEAM_VOTING)
    }

    fun validateTeam(mission: Mission, team: List<Player>) {
        require(team.size == mission.requiredPlayerCount) { "Mission ${mission.index} requires ${mission.requiredPlayerCount} players." }
        require(team.map { it.id }.distinct().size == team.size) { "A proposed team cannot contain the same player more than once." }
    }
}
