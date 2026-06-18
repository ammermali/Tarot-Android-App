package eu.mermali.tarot.domain.model

data class TarotCard(
    val id: String,
    val displayName: String,
    val direction: CardDirection,
    val artKey: String? = null,
    val abilities: Set<TarotAbility> = emptySet(),
    val visibleToReversedPeers: Boolean = TarotAbility.IsolatedReversed !in abilities,
    val visibleToArcaneSight: Boolean = TarotAbility.HiddenFromSight !in abilities,
    val appearsInOracleVision: Boolean = TarotAbility.SeesReversed in abilities || TarotAbility.AppearsAsOracle in abilities,
    val isFinalEliminationTarget: Boolean = TarotAbility.SeesReversed in abilities,
    val canAttemptFinalElimination: Boolean = TarotAbility.FinalEliminator in abilities
) {
    val ability: TarotAbility? get() = abilities.firstOrNull()
    fun hasAbility(ability: TarotAbility): Boolean = ability in abilities
}
