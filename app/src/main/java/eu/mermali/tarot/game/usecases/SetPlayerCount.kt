package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.configuration.PlayerCountConfig
import eu.mermali.tarot.domain.configuration.PlayerCountRules
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.game.gamestate.GameState

class SetPlayerCount(private val playerCountConfig: PlayerCountConfig = PlayerCountConfig()) {
    fun rulesFor(playerCount: Int): PlayerCountRules? = playerCountConfig.forPlayerCount(playerCount)

    operator fun invoke(state: GameState, playerCount: Int): GameState {
        val rules = playerCountConfig.forPlayerCount(playerCount)
            ?: throw IllegalArgumentException("Unsupported player count: $playerCount")
        val players = (1..playerCount).map { index -> Player(id = index, name = "Player $index", position = index - 1) }
        return state.copy(
            players = players,
            selectedCards = emptyList(),
            missions = emptyList(),
            proposedTeam = emptyList(),
            teamVotes = emptyMap(),
            missionVotes = emptyList(),
            currentMissionIndex = 0,
            consecutiveRejectedTeams = 0,
            currentRevealPosition = 0,
            winner = null,
            finalEliminationTargetPlayerId = null,
            phase = GamePhase.CARD_SETUP
        ).withLog("Player count set to $playerCount (${rules.straightCards} STRAIGHT, ${rules.reversedCards} REVERSED).")
    }
}
