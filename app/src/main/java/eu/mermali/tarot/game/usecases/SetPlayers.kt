package eu.mermali.tarot.game.usecases

import eu.mermali.tarot.game.gamestate.GameState

class SetPlayers {
    operator fun invoke(state: GameState, names: List<String>): GameState {
        require(names.size == state.players.size) { "Player names must match player count." }
        val players = state.players.sortedBy { it.position }.mapIndexed { index, player ->
            val name = names[index].trim().ifBlank { "Player ${index + 1}" }
            player.copy(name = name)
        }
        return state.copy(players = players).withLog("Player names updated.")
    }

    fun rename(state: GameState, playerId: Int, name: String): GameState {
        require(state.players.any { it.id == playerId }) { "Unknown player id: $playerId" }
        val players = state.players.map { player ->
            if (player.id == playerId) { player.copy(name = name) }
            else { player }
        }
        return state.copy(players = players)
    }
}
