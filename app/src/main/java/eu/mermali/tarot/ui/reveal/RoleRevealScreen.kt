package eu.mermali.tarot.ui.reveal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.mermali.tarot.domain.gamerules.InitialVisibility
import eu.mermali.tarot.domain.gamerules.TarotVisibilityRule
import eu.mermali.tarot.domain.gamerules.VisibilityReason
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotCard
import eu.mermali.tarot.game.gamestate.GameState
import eu.mermali.tarot.ui.cards.TarotCardArt
import eu.mermali.tarot.ui.cards.TarotCardImageResolver

private data class RoleRevealUiState(val currentPlayerIndex: Int = 0, val isRoleVisible: Boolean = false)

@Composable
fun RoleRevealScreen(gameState: GameState, onComplete: () -> Unit, onBack: () -> Unit) {
    val players = remember(gameState.players) { gameState.players.sortedBy { it.position } }
    val visibilityRule = remember { TarotVisibilityRule() }
    val visibleInformationByPlayer = remember(players) { visibilityRule.visibilityFor(players).groupBy { it.viewerPlayerId } }
    var revealState by remember(gameState) { mutableStateOf(RoleRevealUiState()) }
    val currentPlayer = players.getOrNull(revealState.currentPlayerIndex)

    Scaffold(topBar = { RoleRevealTopBar(onBack = onBack) }) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                currentPlayer == null -> EmptyRevealContent(onDone = onComplete)
                revealState.isRoleVisible -> RoleContent(
                    player = currentPlayer,
                    players = players,
                    visibleInformation = visibleInformationByPlayer[currentPlayer.id].orEmpty(),
                    skinId = gameState.cardSkinId,
                    isLastPlayer = revealState.currentPlayerIndex == players.lastIndex,
                    onNext = {
                        if (revealState.currentPlayerIndex == players.lastIndex) { onComplete() }
                        else { revealState = RoleRevealUiState(currentPlayerIndex = revealState.currentPlayerIndex + 1) }
                    }
                )
                else -> PassPhoneContent(player = currentPlayer, onReveal = { revealState = revealState.copy(isRoleVisible = true) })
            }
        }
    }
}

@Composable
private fun EmptyRevealContent(onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            text = "No players available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

@Composable
private fun RoleRevealTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 6.dp)) { Text("Back") }
            Text(text = "Roles", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PassPhoneContent(player: Player, onReveal: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            text = "Pass the phone to ${player.displayName()}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onReveal, modifier = Modifier.fillMaxWidth()) { Text("Reveal") }
    }
}

@Composable
private fun RoleContent(player: Player, players: List<Player>, visibleInformation: List<InitialVisibility>, skinId: String, isLastPlayer: Boolean, onNext: () -> Unit) {
    val card = player.card

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = player.displayName(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        if (card == null) { Text("No role assigned", color = MaterialTheme.colorScheme.error) }
        else {
            RoleCard(card = card, seed = player.id, skinId = skinId)
            Spacer(Modifier.height(16.dp))
            VisibilityCard(visibleInformation = visibleInformation, players = players)
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text(if (isLastPlayer) "Finish" else "Pass to the next player") }
    }
}

@Composable
private fun RoleCard(card: TarotCard, skinId: String) {
    RoleCard(card = card, seed = card.id.hashCode(), skinId)
}

@Composable
private fun RoleCard(card: TarotCard, seed: Int, skinId: String) {
    val context = LocalContext.current
    val imageAssetPath = remember(context, card, seed, skinId) { TarotCardImageResolver.roleCardAssetPath(card, seed, skinId = skinId) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TarotCardArt(assetPath = imageAssetPath, contentDescription = card.displayName, modifier = Modifier.fillMaxWidth(0.62f))
            Spacer(Modifier.height(16.dp))
            RoleName(card = card)
        }
    }
}

@Composable
private fun RoleName(card: TarotCard) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = card.direction.roleColor(), fontWeight = FontWeight.Bold)) { append(card.direction.name) }
            append(" ")
            append(card.roleNameWithoutDirection())
        },
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun VisibilityCard(visibleInformation: List<InitialVisibility>, players: List<Player>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "What you see",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))

            val visibleGroups = visibleInformation.filter { it.visiblePlayerIds.isNotEmpty() }
            if (visibleGroups.isEmpty()) {
                Text(text = "You see no one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }

            visibleGroups.forEachIndexed { index, information ->
                Text(
                    text = information.reason.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                information.visiblePlayerIds.mapNotNull { visiblePlayerId -> players.firstOrNull { it.id == visiblePlayerId } }.forEach { visiblePlayer -> VisiblePlayerRow(player = visiblePlayer) }
                if (index < visibleGroups.lastIndex) { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun VisiblePlayerRow(player: Player) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = player.displayName(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun Player.displayName(): String { return name.ifBlank { "Player ${position + 1}" } }

private fun TarotCard.roleNameWithoutDirection(): String {
    return displayName.removePrefix("Straight").removePrefix("Reversed").trim().ifBlank { displayName }
}

private fun VisibilityReason.label(): String {
    return when (this) {
        VisibilityReason.REVERSED_NETWORK -> "Reversed players"
        VisibilityReason.ARCANE_SIGHT -> "Reversed players"
        VisibilityReason.ORACLE_SIGHT -> "Oracle players"
        VisibilityReason.FINAL_ELIMINATOR_SIGHT -> "Final eliminator"
    }
}

private fun CardDirection.roleColor(): Color {
    return when (this) {
        CardDirection.STRAIGHT -> Color(0xFF1565C0)
        CardDirection.REVERSED -> Color(0xFFC62828)
    }
}
