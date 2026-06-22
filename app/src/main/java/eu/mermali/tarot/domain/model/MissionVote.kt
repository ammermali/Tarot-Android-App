package eu.mermali.tarot.domain.model

enum class MissionVote { STRAIGHT, REVERSED, MAGIC, HERMIT_STRAIGHT, HERMIT_REVERSED }

val MissionVote.isHermitVote: Boolean get() = (this == MissionVote.HERMIT_REVERSED || this == MissionVote.HERMIT_STRAIGHT)

fun MissionVote.baseVote(): MissionVote =
    when (this) {
        MissionVote.HERMIT_STRAIGHT -> MissionVote.STRAIGHT
        MissionVote.HERMIT_REVERSED -> MissionVote.REVERSED
        else -> this
    }

fun MissionVote.emitToken(): MissionToken? =
    when(this) {
        MissionVote.HERMIT_STRAIGHT -> MissionToken.STRAIGHT_HERMIT
        MissionVote.HERMIT_REVERSED -> MissionToken.REVERSED_HERMIT
        else -> null
    }