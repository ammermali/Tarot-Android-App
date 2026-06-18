package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.gamerules.MissionResultRule
import eu.mermali.tarot.domain.gamerules.VictoryRule
import eu.mermali.tarot.game.gamestate.GameState

class ResolveMission(
    private val missionResultRule: MissionResultRule = MissionResultRule(),
    private val victoryRule: VictoryRule = VictoryRule()
) {
    operator fun invoke(state: GameState): GameState {
        require(state.phase == GamePhase.MISSION_RESULT) { "Reading result is not ready." }
        val resolvedState = missionResultRule.resolveCurrentMission(state)
        val resolvedMission = resolvedState.currentMission ?: throw IllegalStateException("There is no current mission.")
        val updatedState = victoryRule.applyAfterMission(resolvedState)
        val baseMessage = "Reading ${resolvedMission.index} resolved as ${resolvedMission.result} (${resolvedMission.reversedVoteCount} REVERSED votes)."
        val nextMessage = when (updatedState.phase) {
            GamePhase.FINAL_ELIMINATION -> " Final elimination starts."
            GamePhase.GAME_OVER -> " Winner: ${updatedState.winner?.side} (${updatedState.winner?.reason})."
            else -> " Next Reader: ${updatedState.currentReader?.name ?: "-"}."
        }
        return updatedState.withLog(baseMessage + nextMessage)
    }
}
