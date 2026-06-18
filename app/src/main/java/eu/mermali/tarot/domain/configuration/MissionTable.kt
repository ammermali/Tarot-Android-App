package eu.mermali.tarot.domain.configuration
import eu.mermali.tarot.domain.model.Mission

class MissionTable {
    private val requiredPlayersByCount = mapOf(
        5 to listOf(2, 3, 2, 3, 3),
        6 to listOf(2, 3, 4, 3, 4),
        7 to listOf(2, 3, 3, 4, 4),
        8 to listOf(3, 4, 4, 5, 5),
        9 to listOf(3, 4, 4, 5, 5),
        10 to listOf(3, 4, 4, 5, 5)
    )

    fun missionsFor(playerCount: Int): List<Mission> {
        val requiredPlayers = requiredPlayersByCount[playerCount] ?: throw IllegalArgumentException("Unsupported player count: $playerCount")
        return requiredPlayers.mapIndexed { index, playersRequired -> Mission(index = index + 1, requiredPlayerCount = playersRequired, reversedVotesRequired = reversedVotesRequired(playerCount, index + 1)) }
    }

    private fun reversedVotesRequired(playerCount: Int, missionIndex: Int): Int = if (playerCount >= 7 && missionIndex == 4) 2 else 1
}
