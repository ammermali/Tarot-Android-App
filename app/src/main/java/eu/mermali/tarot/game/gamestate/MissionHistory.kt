package eu.mermali.tarot.game.gamestate

import eu.mermali.tarot.domain.model.Mission
import eu.mermali.tarot.domain.model.MissionResult

data class MissionHistory(val missions: List<Mission> = emptyList()) {
    val straightScore: Int get() = missions.count { it.result == MissionResult.STRAIGHT }
    val reversedScore: Int get() = missions.count { it.result == MissionResult.REVERSED }
    fun withResolvedMission(mission: Mission): MissionHistory = copy(missions = missions.map { if (it.index == mission.index) mission else it })
}
