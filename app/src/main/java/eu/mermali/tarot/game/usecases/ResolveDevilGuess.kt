package eu.mermali.tarot.game.usecases
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.game.gamestate.GameState


class ResolveDevilGuess {
    operator fun invoke(state: GameState, deathPlayerId: Int, targetPlayerId: Int): GameState {
        require(state.phase == GamePhase.DEVIL_GUESS) { "Devil guess is not active. "}
        val death = state.players.firstOrNull { it.id == deathPlayerId } ?: throw IllegalArgumentException("Unknown Death player id.")
        require(death.card?.id == "reversed_death"){ "Only Reversed Death can guess the Straight Devil." }
        val target = state.players.firstOrNull { it.id == targetPlayerId } ?: throw IllegalArgumentException("Unknown target player id.")
        val guessedCorrectly = target.card?.id == "straight_devil"
        val updateState = if(guessedCorrectly){
            state.copy(devilGuessTargetPlayerId = targetPlayerId, activeFinalEliminatorPlayerId = targetPlayerId, phase = GamePhase.FINAL_ELIMINATION)
        } else {
            state.copy(devilGuessTargetPlayerId = targetPlayerId, activeFinalEliminatorPlayerId = deathPlayerId, phase = GamePhase.FINAL_ELIMINATION)
        }
        val resultMessage = if(guessedCorrectly){
            "${death.name} found the Straight Devil. The Straight Devil becomes the final eliminator."
        } else {
            "${death.name} failed to find the Straight Devil. Reversed Death remains the final eliminator."
        }
        return updateState.withLog(resultMessage)
    }
}