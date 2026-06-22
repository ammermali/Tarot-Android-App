package eu.mermali.tarot.domain.gamerules

import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.Mission
import eu.mermali.tarot.domain.model.MissionResult
import eu.mermali.tarot.domain.model.MissionVote
import eu.mermali.tarot.domain.model.MissionVoteCast
import eu.mermali.tarot.domain.model.baseVote
import eu.mermali.tarot.domain.model.emitToken
import eu.mermali.tarot.game.gamestate.GameState
import kotlin.random.Random

class MissionResultRule {
    fun resolve(mission: Mission, votes: List<MissionVoteCast>): Mission {
        require(votes.size == mission.requiredPlayerCount) { "Mission ${mission.index} requires ${mission.requiredPlayerCount} mission votes." }
        val rawVotes = votes.map { it.vote }
        val baseVotes = rawVotes.map { it.baseVote() }
        val reversedVotes = baseVotes.count { it == MissionVote.REVERSED }
        val straightVotes = baseVotes.count { it == MissionVote.STRAIGHT }
        val magicVotes = baseVotes.count { it == MissionVote.MAGIC }
        val tokens = rawVotes.mapNotNull { it.emitToken() }.toSet()
        val baseresult = if (reversedVotes >= mission.reversedVotesRequired) { MissionResult.REVERSED } else { MissionResult.STRAIGHT }
        val result = if (magicVotes % 2 == 1) { baseresult.invert() } else { baseresult }
        return mission.copy(result = result, straightVoteCount = straightVotes, reversedVoteCount = reversedVotes, tokens = tokens)
    }

    fun resolveCurrentMission(state: GameState): GameState {
        val mission = state.currentMission ?: throw IllegalStateException("There is no current mission.")
        val resolvedMission = resolve(mission, state.missionVotes).withReadingArt()
        return state.copy(
            missions = state.missions.map { if (it.index == resolvedMission.index) resolvedMission else it },
            missionVotes = emptyList(), phase = GamePhase.MISSION_RESULT
        )
    }

    private fun Mission.withReadingArt(random: Random = Random.Default): Mission {
        val artPool = when (result) {
            MissionResult.STRAIGHT -> StraightReadingArtKeys
            MissionResult.REVERSED -> ReversedReadingArtKeys
            MissionResult.PENDING -> return this
        }

        return copy(artKey = artPool.random(random))
    }

    private fun MissionResult.invert() : MissionResult{
        return when(this){
            MissionResult.STRAIGHT -> MissionResult.REVERSED
            MissionResult.REVERSED -> MissionResult.STRAIGHT
            MissionResult.PENDING -> MissionResult.PENDING
        }
    }

    private companion object {
        val StraightReadingArtKeys = readingArtKeys("straight")
        val ReversedReadingArtKeys = readingArtKeys("reversed")

        fun readingArtKeys(direction: String): List<String> {
            val suits = listOf("cups", "pentacles", "swords", "wands")
            return suits.flatMap { suit ->
                (1..10).map { number -> "reading_${direction}_${suit}${number.toString().padStart(2, '0')}" }
            }
        }
    }
}
