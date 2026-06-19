package eu.mermali.tarot.ui.game
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotAbility
import eu.mermali.tarot.game.gamestate.GameState

@Composable
fun PostGameEliminationScreen(gameState: GameState, onTargetSelected: (eliminatorPlayerId: Int, targetPlayerId: Int) -> Unit, onMainMenu: () -> Unit) {
    val eliminator = gameState.activeFinalEliminatorPlayerId?.let { id -> gameState.players.firstOrNull{it.id == id}}
        ?: gameState.players.firstOrNull {player ->
            player.card?.id == ReversedDeathCardId || player.card?.canAttemptFinalElimination == true || player.card?.hasAbility(TarotAbility.FinalEliminator) == true
        }
    val targetExists = gameState.players.any { player ->
        player.card?.id == StraightHighPriestessCardId || player.card?.isFinalEliminationTarget == true || player.card?.hasAbility(TarotAbility.SeesReversed) == true
    }
    val eliminatorLabel = if (eliminator?.card?.id == "straight_devil"){
        "STRAIGHT DEVIL"
    } else {
        "REVERSED DEATH"
    }

    if (eliminator == null || !targetExists) {
        WinScreen(
            title = "STRAIGHT WIN",
            titleColor = StraightWinColor,
            players = gameState.players.playersFor(CardDirection.STRAIGHT),
            reason = "Three STRAIGHT readings",
            onMainMenu = onMainMenu
        )
        return
    }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Final elimination",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = eliminatorLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ReversedWinColor
                    )
                    Text(
                        text = eliminator.displayName(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Choose the player to eliminate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            gameState.players.sortedBy { it.position }.forEach { target ->
                OutlinedButton(
                    onClick = { onTargetSelected(eliminator.id, target.id) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = target.displayName(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(gameState: GameState, onMainMenu: () -> Unit) {
    val winnerSide = gameState.winner?.side
    val title = when (winnerSide) {
        CardDirection.REVERSED -> "REVERSED WIN"
        CardDirection.STRAIGHT -> "STRAIGHT WIN"
        null -> "GAME OVER"
    }
    val titleColor = when (winnerSide) {
        CardDirection.REVERSED -> ReversedWinColor
        CardDirection.STRAIGHT -> StraightWinColor
        null -> MaterialTheme.colorScheme.onSurface
    }
    val players = winnerSide?.let { gameState.players.playersFor(it) }.orEmpty()
    val reason = gameState.winner?.reason?.name?.replace('_', ' ')

    WinScreen(
        title = title,
        titleColor = titleColor,
        players = players,
        reason = reason,
        onMainMenu = onMainMenu
    )
}

@Composable
private fun WinScreen(title: String, titleColor: Color, players: List<Player>, reason: String?, onMainMenu: () -> Unit) {
    Scaffold { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(44.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center
            )
            if (!reason.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(28.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Winning players", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (players.isEmpty()) { Text(text = "-", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    else { players.forEach { player -> WinnerPlayerRow(player = player) } }
                }
            }

            Spacer(Modifier.height(28.dp))
            Button(onClick = onMainMenu, modifier = Modifier.fillMaxWidth() ) { Text("Back to main menu") }
        }
    }
}

@Composable
private fun WinnerPlayerRow(player: Player) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = player.displayName(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = player.card?.displayName ?: "-",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun List<Player>.playersFor(direction: CardDirection): List<Player> {
    return filter { player -> player.card?.direction == direction }.sortedBy { it.position }
}

private fun Player.displayName(): String {
    return name.ifBlank { "Player ${position + 1}" }
}

private const val ReversedDeathCardId = "reversed_death"
private const val StraightHighPriestessCardId = "straight_high_priestess"
private val StraightWinColor = Color(0xFF1565C0)
private val ReversedWinColor = Color(0xFFC62828)
