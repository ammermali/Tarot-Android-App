package eu.mermali.tarot.game.usecases
import eu.mermali.tarot.game.gamestate.GameState

internal fun GameState.withLog(message: String): GameState = copy(gameLog = gameLog + message)
