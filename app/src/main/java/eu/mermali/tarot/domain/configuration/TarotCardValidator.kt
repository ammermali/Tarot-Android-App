package eu.mermali.tarot.domain.configuration
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.TarotAbility
import eu.mermali.tarot.domain.model.TarotCard

data class TarotCardValidationResult(val isValid: Boolean, val errors: List<String> = emptyList())

class TarotCardValidator {
    fun validate(playerCount: Int, cards: List<TarotCard>): TarotCardValidationResult {
        val errors = mutableListOf<String>()
        if (playerCount <= 0) { errors.add("Player count must be positive.") }
        if (cards.size != playerCount) { errors.add("Selected card count must match player count.") }
        val duplicatedIds = cards.groupBy { it.id }.filterValues { it.size > 1 }.keys
        if (duplicatedIds.isNotEmpty()) { errors.add("Card ids must be unique: ${duplicatedIds.joinToString()}.") }
        if (cards.none { it.direction == CardDirection.STRAIGHT }) { errors.add("At least one STRAIGHT card is required.") }
        if (cards.none { it.direction == CardDirection.REVERSED }) { errors.add("At least one REVERSED card is required.") }
        cards.forEach { card ->
            if (card.hasAbility(TarotAbility.FinalEliminator) && card.direction != CardDirection.REVERSED) {
                errors.add("${card.displayName} can attempt final elimination only when REVERSED.")
            }
            if (card.hasAbility(TarotAbility.SeesReversed) && card.direction != CardDirection.STRAIGHT) {
                errors.add("${card.displayName} can see REVERSED cards only when STRAIGHT.")
            }
            if (card.hasAbility(TarotAbility.SeesOracles) && card.direction != CardDirection.STRAIGHT) {
                errors.add("${card.displayName} can see oracle-like cards only when STRAIGHT.")
            }
            if (card.hasAbility(TarotAbility.AppearsAsOracle) && card.direction != CardDirection.REVERSED) {
                errors.add("${card.displayName} can appear ambiguous only when REVERSED.")
            }
            if (card.hasAbility(TarotAbility.HiddenFromSight) && card.direction != CardDirection.REVERSED) {
                errors.add("${card.displayName} can be hidden from sight only when REVERSED.")
            }
            if (card.hasAbility(TarotAbility.IsolatedReversed) && card.direction != CardDirection.REVERSED) {
                errors.add("${card.displayName} can be isolated only when REVERSED.")
            }
        }
        val finalTargets = cards.count { it.isFinalEliminationTarget }
        if (finalTargets > 1) { errors.add("Only one final elimination target is supported.") }
        val hasFinalTarget = cards.any { it.isFinalEliminationTarget }
        val hasFinalEliminator = cards.any { it.canAttemptFinalElimination }
        if (hasFinalTarget && !hasFinalEliminator) { errors.add("A final elimination target requires a REVERSED final eliminator.") }
        return TarotCardValidationResult(isValid = errors.isEmpty(), errors = errors)
    }
}
