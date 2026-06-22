package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.MissionVote
import eu.mermali.tarot.domain.model.MissionVoteCast
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotAbility
import eu.mermali.tarot.domain.model.isHermitVote
import eu.mermali.tarot.game.gamestate.GameState

class MissionVotingRule {
    fun canCastMissionVote(player: Player, vote: MissionVote, watchTokenPlayerId: Int?): Boolean {
        val card = player.card ?: return false

        if (player.id == watchTokenPlayerId && vote.isHermitVote){ return false }

        return when {
            card.hasAbility(TarotAbility.HermitStraight) -> vote == MissionVote.STRAIGHT || vote == MissionVote.HERMIT_STRAIGHT
            card.hasAbility(TarotAbility.HermitReversed) -> vote == MissionVote.STRAIGHT || vote == MissionVote.REVERSED || vote == MissionVote.HERMIT_REVERSED
            card.hasAbility(TarotAbility.CanCastMagic) -> vote == MissionVote.STRAIGHT || vote == MissionVote.MAGIC
            card.direction == CardDirection.STRAIGHT ->  vote == MissionVote.STRAIGHT
            card.direction == CardDirection.REVERSED -> vote == MissionVote.STRAIGHT || vote == MissionVote.REVERSED
            else -> false
        }
    }

    fun submitVote(state: GameState, playerId: Int, vote: MissionVote): GameState {
        val player = state.players.firstOrNull { it.id == playerId } ?: throw IllegalArgumentException("Unknown player id: $playerId")
        val mission = state.currentMission ?: throw IllegalStateException("There is no current mission.")
        val proposedTeam = if (state.proposedTeam.isNotEmpty()) { state.proposedTeam }
        else { mission.proposedTeam }
        require(proposedTeam.any { it.id == playerId }) { "Only proposed team members can cast mission votes." }
        require(canCastMissionVote(player, vote, mission.watchTokenPlayerId)) { "STRAIGHT cards can only cast STRAIGHT mission votes." }
        require(player in mission.proposedTeam) { "Player is not on the proposed team."}
        require(state.missionVotes.none { it.playerId == playerId }) { "Player has already cast a vote."}
        val updatedVotes = state.missionVotes + MissionVoteCast(playerId = playerId, vote = vote)
        require(updatedVotes.size <= mission.requiredPlayerCount) { "Mission ${mission.index} already has all required votes." }
        return state.copy(
            missionVotes = updatedVotes,
            phase = if (updatedVotes.size == mission.requiredPlayerCount) { GamePhase.MISSION_RESULT }
            else { GamePhase.MISSION_VOTING }
        )
    }
}
