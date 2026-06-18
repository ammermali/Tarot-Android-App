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
import eu.mermali.tarot.domain.gamerules.MissionVotingRule
import eu.mermali.tarot.domain.gamerules.MissionResultRule
import eu.mermali.tarot.domain.model.MissionResult
import eu.mermali.tarot.domain.model.MissionVote
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.game.gamestate.GameState

private data class MissionVoteUiState(val currentPlayerIndex: Int, val isVoteVisible: Boolean = false, val selectedVote: MissionVote? = null)

private data class MissionVoteOption(val vote: MissionVote, val label: String, val color: Color)

@Composable
fun MissionVoteScreen(gameState: GameState, onBack: () -> Unit, onSubmitVote: (playerId: Int, vote: MissionVote) -> Unit) {
    val missionTeam = remember(gameState.proposedTeam, gameState.currentMission) {
        if (gameState.proposedTeam.isNotEmpty()) { gameState.proposedTeam.sortedBy { it.position } }
        else { gameState.currentMission?.proposedTeam.orEmpty().sortedBy { it.position } }
    }
    val teamKey = remember(missionTeam) { missionTeam.joinToString { it.id.toString() } }
    var voteState by remember(gameState.currentMissionIndex, teamKey) {
        mutableStateOf(MissionVoteUiState(currentPlayerIndex = gameState.missionVotes.size.coerceAtMost(missionTeam.size)))
    }
    val currentPlayer = missionTeam.getOrNull(voteState.currentPlayerIndex)

    Scaffold(topBar = { MissionVoteTopBar(onBack = onBack) }) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                currentPlayer == null -> EmptyMissionVoteContent()
                voteState.isVoteVisible -> MissionVoteChoiceContent(
                    player = currentPlayer,
                    selectedVote = voteState.selectedVote,
                    onSelectVote = { selectedVote -> voteState = voteState.copy(selectedVote = selectedVote) },
                    onConfirmVote = {
                        val confirmedVote = voteState.selectedVote ?: return@MissionVoteChoiceContent
                        val isLastPlayer = voteState.currentPlayerIndex == missionTeam.lastIndex
                        onSubmitVote(currentPlayer.id, confirmedVote)
                        if (!isLastPlayer) { voteState = MissionVoteUiState(currentPlayerIndex = voteState.currentPlayerIndex + 1) }
                    }
                )
                else -> PassPhoneMissionVoteContent(
                    player = currentPlayer,
                    onStartVote = { voteState = voteState.copy(isVoteVisible = true) }
                )
            }
        }
    }
}

@Composable
fun MissionReadingRevealScreen(gameState: GameState, onRevealReading: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pass the phone to ${gameState.currentReader?.displayName() ?: "the Reader"}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRevealReading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Reveal the reading") }
    }
}

@Composable
fun MissionReadingResultScreen(gameState: GameState, onChooseNextTeam: () -> Unit) {
    val mission = gameState.currentMission
    val resolvedMission = remember(mission, gameState.missionVotes) { mission?.let { MissionResultRule().resolve(it, gameState.missionVotes) } }
    val result = resolvedMission?.result ?: MissionResult.PENDING
    val shuffledVotes = remember(gameState.currentMissionIndex, gameState.missionVotes) { gameState.missionVotes.shuffled() }
    val resultColor = when (result) {
        MissionResult.STRAIGHT -> StraightResultColor
        MissionResult.REVERSED -> ReversedResultColor
        MissionResult.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))
        Text(
            text = result.name,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = resultColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shuffledVotes.forEachIndexed { index, vote -> MissionVoteResultRow(index = index + 1, vote = vote) }
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onChooseNextTeam, modifier = Modifier.fillMaxWidth()) { Text("Choose next team") }
    }
}

@Composable
private fun MissionVoteTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 6.dp)) { Text("Back") }
            Text(
                text = "Reading Vote",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyMissionVoteContent() {
    Text(
        text = "No team selected",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PassPhoneMissionVoteContent(player: Player, onStartVote: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
private fun MissionVoteChoiceContent(player: Player, selectedVote: MissionVote?, onSelectVote: (MissionVote) -> Unit, onConfirmVote: () -> Unit) {
    val missionVotingRule = remember { MissionVotingRule() }
    val voteOptions = missionVoteOptions().filter { option -> missionVotingRule.canCastMissionVote(player, option.vote) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = player.displayName(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            voteOptions.forEach { option ->
                MissionVoteOptionButton(
                    option = option,
                    selected = selectedVote == option.vote,
                    onClick = { onSelectVote(option.vote) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onConfirmVote, enabled = selectedVote != null, modifier = Modifier.fillMaxWidth()) { Text("Confirm vote") }
    }
}

@Composable
private fun MissionVoteOptionButton(option: MissionVoteOption, selected: Boolean, onClick: () -> Unit) {
    val content: @Composable () -> Unit = {
        Text(text = option.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }

    if (selected) { Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { content() } }
    else { OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, option.color)) { content() } }
}

@Composable
private fun MissionVoteResultRow(index: Int, vote: MissionVote) {
    val voteColor = when (vote) {
        MissionVote.STRAIGHT -> StraightResultColor
        MissionVote.REVERSED -> ReversedResultColor
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Vote $index",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = vote.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = voteColor
            )
        }
    }
}

private fun missionVoteOptions(): List<MissionVoteOption> {
    return listOf(
        MissionVoteOption(vote = MissionVote.STRAIGHT, label = "STRAIGHT", color = StraightResultColor ),
        MissionVoteOption(vote = MissionVote.REVERSED, label = "REVERSED", color = ReversedResultColor)
    )
}

private fun Player.displayName(): String {
    return name.ifBlank { "Player ${position + 1}" }
}

private val StraightResultColor = Color(0xFF1565C0)
private val ReversedResultColor = Color(0xFFC62828)
