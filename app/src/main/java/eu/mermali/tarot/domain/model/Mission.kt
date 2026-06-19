package eu.mermali.tarot.domain.model

data class Mission(
    val index: Int,
    val requiredPlayerCount: Int,
    val reversedVotesRequired: Int,
    val proposedTeam: List<Player> = emptyList(),
    val result: MissionResult = MissionResult.PENDING,
    val straightVoteCount: Int = 0,
    val reversedVoteCount: Int = 0,
    val magicVoteCount: Int = 0,
    val artKey: String? = null
) {
    val isResolved: Boolean get() = result != MissionResult.PENDING
}
