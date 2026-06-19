package eu.mermali.tarot.domain.model

sealed class TarotAbility {
    object SeesReversed : TarotAbility()
    object SeesOracles : TarotAbility()
    object FinalEliminator : TarotAbility()
    object AppearsAsOracle : TarotAbility()
    object HiddenFromSight : TarotAbility()
    object IsolatedReversed : TarotAbility()
    object CanCastMagic : TarotAbility()
    object AppearsReversed : TarotAbility()
    object SeesFinalEliminator : TarotAbility()
}
