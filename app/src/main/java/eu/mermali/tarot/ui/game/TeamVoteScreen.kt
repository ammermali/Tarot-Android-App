package eu.mermali.tarot.ui.game
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.mermali.tarot.domain.gamerules.TeamVotingRule
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TeamVote
import eu.mermali.tarot.game.gamestate.GameState

private data class TeamVoteUiState(val currentPlayerIndex: Int = 0, val isVoteVisible: Boolean = false, val selectedVote: TeamVote? = null)

@Composable
fun TeamVoteScreen(gameState: GameState, onBack: () -> Unit, onSubmitVote: (playerId: Int, vote: TeamVote) -> Unit) {
    val players = remember(gameState.players) { gameState.players.sortedBy { it.position } }
    val team = remember(gameState.proposedTeam) { gameState.proposedTeam.sortedBy { it.position } }
    val teamKey = remember(team) { team.joinToString { it.id.toString() } }
    var voteState by remember(gameState.currentMissionIndex, teamKey) { mutableStateOf(TeamVoteUiState()) }
    val currentPlayer = players.getOrNull(voteState.currentPlayerIndex)

    Scaffold(topBar = { TeamVoteTopBar(onBack = onBack) } )
    { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                currentPlayer == null -> EmptyTeamVoteContent()
                voteState.isVoteVisible -> VoteChoiceContent(
                    player = currentPlayer,
                    team = team,
                    selectedVote = voteState.selectedVote,
                    onSelectVote = { selectedVote -> voteState = voteState.copy(selectedVote = selectedVote) },
                    onConfirmVote = {
                        val confirmedVote = voteState.selectedVote ?: return@VoteChoiceContent
                        val isLastPlayer = voteState.currentPlayerIndex == players.lastIndex
                        onSubmitVote(currentPlayer.id, confirmedVote)
                        if (!isLastPlayer) { voteState = TeamVoteUiState(currentPlayerIndex = voteState.currentPlayerIndex + 1) }
                    }
                )
                else -> PassPhoneVoteContent(
                    player = currentPlayer,
                    onStartVote = { voteState = voteState.copy(isVoteVisible = true) }
                )
            }
        }
    }
}

@Composable
fun TeamVoteResultScreen(gameState: GameState, onContinue: () -> Unit) {
    val players = remember(gameState.players) { gameState.players.sortedBy { it.position } }
    val summary = remember(gameState.teamVotes) { TeamVotingRule().resolve(gameState.teamVotes.values) }
    val title = if (summary.approved) "APPROVED" else "REJECTED"
    val resultColor = if (summary.approved) ApprovedColor else RejectedColor

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(56.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = resultColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                players.forEach { player -> VoteResultRow(player = player, vote = gameState.teamVotes[player.id])}
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text(if (summary.approved) "Proceed to the reading" else "Create a new team") }
    }
}

@Composable
private fun TeamVoteTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 6.dp)) { Text("Back") }
            Text(
                text = "Team Vote",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyTeamVoteContent() {
    Text(text = "No players available", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
}

@Composable
private fun PassPhoneVoteContent(player: Player, onStartVote: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            text = "Pass the phone to ${player.displayName()}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStartVote, modifier = Modifier.fillMaxWidth()) { Text("Start vote") }
    }
}

@Composable
private fun VoteChoiceContent(player: Player, team: List<Player>, selectedVote: TeamVote?, onSelectVote: (TeamVote) -> Unit, onConfirmVote: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = player.displayName(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Team", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                team.forEach { teamMember -> Text(text = teamMember.displayName(), style = MaterialTheme.typography.bodyLarge) }
            }
        }

        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            VoteChoiceButton(
                text = "Approve",
                selected = selectedVote == TeamVote.APPROVE,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelectVote(TeamVote.APPROVE) }
            )
            VoteChoiceButton(
                text = "Reject",
                selected = selectedVote == TeamVote.REJECT,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelectVote(TeamVote.REJECT) }
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onConfirmVote, enabled = selectedVote != null, modifier = Modifier.fillMaxWidth()) { Text("Confirm vote") }
    }
}

@Composable
private fun VoteChoiceButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) { Button(onClick = onClick, modifier = modifier) { Text(text) } }
    else { OutlinedButton(onClick = onClick, modifier = modifier) { Text(text) } }
}

@Composable
private fun VoteResultRow(player: Player, vote: TeamVote?) {
    val voteText = when (vote) {
        TeamVote.APPROVE -> "APPROVE"
        TeamVote.REJECT -> "REJECT"
        null -> "-"
    }

    val voteColor = when (vote) {
        TeamVote.APPROVE -> ApprovedColor
        TeamVote.REJECT -> RejectedColor
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = player.displayName(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = voteText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = voteColor
            )
        }
    }
}

private fun Player.displayName(): String { return name.ifBlank { "Player ${position + 1}" } }

private val ApprovedColor = Color(0xFF2E7D50)
private val RejectedColor = Color(0xFFC03945)
