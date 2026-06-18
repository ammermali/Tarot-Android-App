package eu.mermali.tarot.game.history

import android.content.Context
import eu.mermali.tarot.game.gamestate.GameState
import org.json.JSONArray
import org.json.JSONObject

data class GameHistoryEntry(val id: String, val title: String, val createdAtMillis: Long, val events: List<GameHistoryEvent>)

data class GameHistoryEvent(val order: Int, val category: String, val message: String)

class GameHistoryRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    fun load(): List<GameHistoryEntry> {
        val rawHistory = preferences.getString(HistoryKey, null) ?: return emptyList()
        return runCatching {
            val entries = JSONArray(rawHistory)
            List(entries.length()) { index -> entries.getJSONObject(index).toHistoryEntry() }}.getOrDefault(emptyList())
    }

    fun saveCompletedGame(state: GameState) {
        if (state.gameLog.isEmpty()) { return }

        val entry = state.toHistoryEntry(System.currentTimeMillis())
        val existingEntries = load()
        if (existingEntries.any { it.id == entry.id }) { return }

        save((listOf(entry) + existingEntries).take(MaxSavedGames))
    }

    private fun save(entries: List<GameHistoryEntry>) {
        val payload = JSONArray().apply { entries.forEach { entry -> put(entry.toJson()) } }
        preferences.edit().putString(HistoryKey, payload.toString()).apply()
    }

    private fun GameState.toHistoryEntry(createdAtMillis: Long): GameHistoryEntry {
        val eventMessages = if (winner == null || gameLog.any { it.contains("Winner:") }) { gameLog } else { gameLog + "Winner: ${winner.side} (${winner.reason})." }
        val source = players.joinToString { it.name } + "|" + selectedCards.joinToString { it.id } + "|" + eventMessages.joinToString("|")

        return GameHistoryEntry(
            id = source.hashCode().toString(),
            title = "${winner?.side?.name ?: "Completed game"} - ${players.size} players",
            createdAtMillis = createdAtMillis,
            events = eventMessages.mapIndexed { index, message -> GameHistoryEvent( order = index + 1, category = message.toHistoryCategory(), message = message) }
        )
    }

    private fun JSONObject.toHistoryEntry(): GameHistoryEntry {
        val rawEvents = optJSONArray("events") ?: JSONArray()
        return GameHistoryEntry(
            id = optString("id"),
            title = optString("title"),
            createdAtMillis = optLong("createdAtMillis"),
            events = List(rawEvents.length()) { index -> rawEvents.getJSONObject(index).toHistoryEvent() }.sortedBy { it.order }
        )
    }

    private fun JSONObject.toHistoryEvent(): GameHistoryEvent {
        return GameHistoryEvent(
            order = optInt("order"),
            category = optString("category", "Game"),
            message = optString("message")
        )
    }

    private fun GameHistoryEntry.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("title", title)
            .put("createdAtMillis", createdAtMillis)
            .put("events", JSONArray().apply { events.sortedBy { it.order }.forEach { event -> put(event.toJson()) } } )
    }

    private fun GameHistoryEvent.toJson(): JSONObject {
        return JSONObject()
            .put("order", order)
            .put("category", category)
            .put("message", message)
    }

    private fun String.toHistoryCategory(): String {
        return when {
            contains("Role distribution") || contains("Initial alliances") || contains("Cards assigned") -> "Setup"
            contains("proposed a team") -> "Team"
            contains("voted") || contains("Team approved") || contains("Team rejected") -> "Team voting"
            contains("reading vote", ignoreCase = true) || contains("Reading") -> "Reading"
            contains("final elimination", ignoreCase = true) || contains("chose") -> "Post-game"
            contains("Winner:") -> "Result"
            else -> "Game"
        }
    }

    private companion object {
        const val PreferencesName = "tarot_game_history"
        const val HistoryKey = "completed_games"
        const val MaxSavedGames = 50
    }
}
