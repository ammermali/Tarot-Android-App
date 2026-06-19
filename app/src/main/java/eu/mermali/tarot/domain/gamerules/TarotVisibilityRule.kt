package eu.mermali.tarot.domain.gamerules
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotAbility

enum class VisibilityReason { REVERSED_NETWORK, ARCANE_SIGHT, ORACLE_SIGHT, FINAL_ELIMINATOR_SIGHT }

data class InitialVisibility(val viewerPlayerId: Int, val visiblePlayerIds: List<Int>, val reason: VisibilityReason)

class TarotVisibilityRule {
    fun visibilityFor(players: List<Player>): List<InitialVisibility> =
        players.flatMap{ viewer ->
            listOfNotNull(
                reversedNetworkVisibility(viewer, players),
                arcaneSightVisibility(viewer, players),
                oracleSightVisibility(viewer, players),
                finalEliminatorVisibility(viewer, players)
            ) }

    fun visiblePlayerIdsFor(viewer: Player, players: List<Player>): List<Int> =
        visibilityFor(players).filter { it.viewerPlayerId == viewer.id }.flatMap { it.visiblePlayerIds }.distinct()

    private fun reversedNetworkVisibility( viewer: Player, players: List<Player>): InitialVisibility? {
        val viewerCard = viewer.card ?: return null
        if (viewerCard.direction != CardDirection.REVERSED || !viewerCard.visibleToReversedPeers) { return null }
        val visiblePlayerIds = players.filter { player ->
            val card = player.card
            player.id != viewer.id && card != null && card.direction == CardDirection.REVERSED && card.visibleToReversedPeers
        }.map { it.id }
        return InitialVisibility(viewerPlayerId = viewer.id, visiblePlayerIds = visiblePlayerIds, reason = VisibilityReason.REVERSED_NETWORK)
    }

    private fun arcaneSightVisibility(viewer: Player, players: List<Player>): InitialVisibility? {
        val viewerCard = viewer.card ?: return null
        if (!viewerCard.hasAbility(TarotAbility.SeesReversed)) { return null }
        val visiblePlayerIds = players.filter { player ->
            val card = player.card
            player.id != viewer.id && card != null && (card.direction == CardDirection.REVERSED || card.hasAbility(TarotAbility.AppearsReversed)) && card.visibleToArcaneSight
        }.map { it.id }
        return InitialVisibility(viewerPlayerId = viewer.id, visiblePlayerIds = visiblePlayerIds, reason = VisibilityReason.ARCANE_SIGHT)
    }

    private fun oracleSightVisibility(viewer: Player, players: List<Player>): InitialVisibility? {
        val viewerCard = viewer.card ?: return null
        if (!viewerCard.hasAbility(TarotAbility.SeesOracles)) { return null}
        val visiblePlayerIds = players.filter { player ->
            player.id != viewer.id && player.card?.appearsInOracleVision == true
        }.map { it.id }
        return InitialVisibility(viewerPlayerId = viewer.id, visiblePlayerIds = visiblePlayerIds, reason = VisibilityReason.ORACLE_SIGHT)
    }

    private fun finalEliminatorVisibility(viewer: Player, players: List<Player>): InitialVisibility? {
        val viewerCard = viewer.card ?: return null
        if(!viewerCard.hasAbility(TarotAbility.SeesFinalEliminator)){ return null }
        val visiblePlayerIds = players.filter { player ->
            val card = player.card
            player.id != viewer.id && card != null && card.hasAbility(TarotAbility.FinalEliminator) }.map {it.id}
        return InitialVisibility(viewerPlayerId = viewer.id, visiblePlayerIds = visiblePlayerIds, reason = VisibilityReason.FINAL_ELIMINATOR_SIGHT)
    }
}
