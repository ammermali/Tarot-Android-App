package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.configuration.MissionTable
import eu.mermali.tarot.domain.configuration.TarotCardValidator
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.gamerules.TarotCardAssignmentRule
import eu.mermali.tarot.game.gamestate.GameState
import kotlin.random.Random

class StartGame(
    private val assignmentRule: TarotCardAssignmentRule = TarotCardAssignmentRule(),
    private val missionTable: MissionTable = MissionTable(),
    private val tarotCardValidator: TarotCardValidator = TarotCardValidator()
) {
    operator fun invoke(state: GameState, random: Random = Random.Default): GameState {
        require(state.phase == GamePhase.CARD_SETUP) { "Game can start only after card setup." }
        val validation = tarotCardValidator.validate(state.players.size, state.selectedCards)
        require(validation.isValid) { validation.errors.joinToString() }
        val assignedPlayers = assignmentRule.assign(state.players, state.selectedCards, random)
        val readerPosition = random.nextInt(assignedPlayers.size)
        val reader = assignedPlayers.sortedBy { it.position }[readerPosition]
        return state.copy(
            players = assignedPlayers,
            missions = missionTable.missionsFor(state.players.size),
            currentReaderPosition = readerPosition,
            currentMissionIndex = 0,
            currentRevealPosition = 0,
            consecutiveRejectedTeams = 0,
            proposedTeam = emptyList(),
            teamVotes = emptyMap(),
            missionVotes = emptyList(),
            winner = null,
            finalEliminationTargetPlayerId = null,
            phase = GamePhase.CARD_REVEAL
        )
            .withLog("Cards assigned. First Reader: ${reader.name}.")
            .withLog("Role distribution: ${assignedPlayers.roleDistributionLabel()}.")
            .withLog("Initial alliances: STRAIGHT - ${assignedPlayers.allianceLabel(CardDirection.STRAIGHT)}; REVERSED - ${assignedPlayers.allianceLabel(CardDirection.REVERSED)}.")
    }
}

private fun List<eu.mermali.tarot.domain.model.Player>.roleDistributionLabel(): String {
    return sortedBy { it.position }.joinToString { player -> "${player.name}=${player.card?.displayName ?: "Unknown"}" }
}

private fun List<eu.mermali.tarot.domain.model.Player>.allianceLabel(direction: CardDirection): String {
    return filter { it.card?.direction == direction }.sortedBy { it.position }.joinToString { it.name }.ifBlank { "-" }
}
