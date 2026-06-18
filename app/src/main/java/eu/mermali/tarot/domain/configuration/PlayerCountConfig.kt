package eu.mermali.tarot.domain.configuration

data class PlayerCountRules(val playerCount: Int, val straightCards: Int, val reversedCards: Int)

class PlayerCountConfig {
    private val rulesByPlayerCount = mapOf(
        5 to PlayerCountRules(playerCount = 5, straightCards = 3, reversedCards = 2),
        6 to PlayerCountRules(playerCount = 6, straightCards = 4, reversedCards = 2),
        7 to PlayerCountRules(playerCount = 7, straightCards = 4, reversedCards = 3),
        8 to PlayerCountRules(playerCount = 8, straightCards = 5, reversedCards = 3),
        9 to PlayerCountRules(playerCount = 9, straightCards = 6, reversedCards = 3),
        10 to PlayerCountRules(playerCount = 10, straightCards = 6, reversedCards = 4)
    )

    fun forPlayerCount(playerCount: Int): PlayerCountRules? = rulesByPlayerCount[playerCount]
    fun supportedPlayerCounts(): Set<Int> = rulesByPlayerCount.keys
}
