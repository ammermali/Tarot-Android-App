package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.game.gamestate.GameState

class ExportGameLog {
    operator fun invoke(state: GameState): String = state.gameLog.joinToString(separator = "\n")
}
