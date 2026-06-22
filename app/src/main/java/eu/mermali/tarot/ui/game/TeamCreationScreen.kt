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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.game.gamestate.GameState

@Composable
fun TeamCreationScreen(gameState: GameState, onBack: () -> Unit, onConfirmTeam: (List<Int>, Int?) -> Unit) {
    val players = remember(gameState.players) { gameState.players.sortedBy { it.position } }
    val mission = gameState.currentMission
    val requiredPlayers = mission?.requiredPlayerCount ?: 0
    var selectedPlayerIds by remember(gameState.currentMissionIndex, gameState.proposedTeam) { mutableStateOf(gameState.proposedTeam.map { it.id }.toSet()) }
    val selectedCount = selectedPlayerIds.size
    val canConfirm = requiredPlayers > 0 && selectedCount == requiredPlayers
    val watchTokenAllowed = mission != null && (gameState.players.size !in 5..6 || mission.index >= 3)
    var selectedWatchTokenPlayerId by remember(gameState.currentMissionIndex, gameState.proposedTeam) { mutableStateOf(gameState.currentMission?.watchTokenPlayerId)}

    Scaffold(topBar = { TeamCreationTopBar(onBack = onBack) })
    { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Create team", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(text = "Reader: ${gameState.currentReader?.displayName() ?: "-"}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Reading ${mission?.index ?: "-"} requires $requiredPlayers players", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Players", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "$selectedCount/$requiredPlayers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(12.dp))

                    players.forEach { player ->
                        val selected = player.id in selectedPlayerIds
                        PlayerSelectionRow(
                            player = player,
                            selected = selected,
                            canAdd = selected || selectedCount < requiredPlayers,
                            watchTokenAllowed = watchTokenAllowed,
                            watchSelected = selectedWatchTokenPlayerId == player.id,
                            onToggleWatch = {
                                selectedWatchTokenPlayerId = if(selectedWatchTokenPlayerId == player.id) null else player.id
                            },
                            onToggle = {
                                selectedPlayerIds =
                                    if(selected) {
                                        if (selectedWatchTokenPlayerId == player.id) {
                                            selectedWatchTokenPlayerId = null
                                        }
                                        selectedPlayerIds - player.id
                                    } else {
                                        selectedPlayerIds + player.id
                                    }
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onConfirmTeam(selectedPlayerIds.toList(), selectedWatchTokenPlayerId)
                          },
                enabled = canConfirm,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Confirm team") }
        }
    }
}

@Composable
private fun TeamCreationTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 6.dp)) { Text("Back") }
            Text(text = "Team", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PlayerSelectionRow(player: Player, selected: Boolean, canAdd: Boolean, watchTokenAllowed: Boolean, watchSelected: Boolean, onToggleWatch: () -> Unit, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) { MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) } else { MaterialTheme.colorScheme.surface },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) { MaterialTheme.colorScheme.primary } else { MaterialTheme.colorScheme.outlineVariant }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = player.displayName(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (selected && watchTokenAllowed) { OutlinedButton(onClick = onToggleWatch) { Text(if (watchSelected) "Watching" else "Watch") } }
            if (selected) { OutlinedButton(onClick = onToggle) {Text("Remove")} }
            else { Button(onClick = onToggle, enabled = canAdd) { Text("Add") } }
        }
    }
}

private fun Player.displayName(): String { return name.ifBlank { "Player ${position + 1}" } }
