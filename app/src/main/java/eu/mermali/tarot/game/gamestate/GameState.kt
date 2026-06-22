package eu.mermali.tarot.game.gamestate
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.Mission
import eu.mermali.tarot.domain.model.MissionResult
import eu.mermali.tarot.domain.model.MissionVoteCast
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotCard
import eu.mermali.tarot.domain.model.TeamVote

data class GameState(
    val players: List<Player> = emptyList(),
    val selectedCards: List<TarotCard> = emptyList(),
    val phase: GamePhase = GamePhase.PLAYER_SETUP,
    val currentReaderPosition: Int = 0,
    val currentMissionIndex: Int = 0,
    val consecutiveRejectedTeams: Int = 0,
    val missions: List<Mission> = emptyList(),
    val proposedTeam: List<Player> = emptyList(),
    val teamVotes: Map<Int, TeamVote> = emptyMap(),
    val missionVotes: List<MissionVoteCast> = emptyList(),
    val devilGuessTargetPlayerId: Int? = null,
    val activeFinalEliminatorPlayerId: Int? = null,
    val winner: Winner? = null,
    val finalEliminationTargetPlayerId: Int? = null,
    val currentRevealPosition: Int = 0,
    val cardSkinId: String = "default",
    val gameLog: List<String> = emptyList()
) {
    val currentMission: Mission?
        get() = missions.getOrNull(currentMissionIndex)

    val currentReader: Player?
        get() = if (players.isEmpty()) { null }
        else { players.sortedBy { it.position }[currentReaderPosition % players.size] }

    val currentRevealPlayer: Player?
        get() = players.sortedBy { it.position }.getOrNull(currentRevealPosition)

    val straightScore: Int
        get() = missions.count { it.result == MissionResult.STRAIGHT }

    val reversedScore: Int
        get() = missions.count { it.result == MissionResult.REVERSED }
}
