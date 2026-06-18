package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.game.gamestate.GameState

class CreateGame {
    operator fun invoke(): GameState = GameState(phase = GamePhase.PLAYER_SETUP).withLog("Game created.")
}
