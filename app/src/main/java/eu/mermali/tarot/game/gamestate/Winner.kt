package eu.mermali.tarot.game.gamestate
import eu.mermali.tarot.domain.model.CardDirection

enum class VictoryReason {THREE_STRAIGHT_MISSIONS, THREE_REVERSED_MISSIONS, FIVE_REJECTED_TEAMS, CORRECT_FINAL_ELIMINATION, FAILED_FINAL_ELIMINATION, HERMIT_STRAIGHT_TOKEN_WIN, HERMIT_REVERSED_TOKEN_WIN}

data class Winner(val side: CardDirection, val reason: VictoryReason, val winningPlayerId: Int? = null)
