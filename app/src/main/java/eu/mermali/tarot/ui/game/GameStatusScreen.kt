package eu.mermali.tarot.ui.game
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.mermali.tarot.domain.gamerules.RejectionRule
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.Mission
import eu.mermali.tarot.domain.model.MissionResult
import eu.mermali.tarot.domain.model.MissionToken
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.game.gamestate.GameState
import eu.mermali.tarot.ui.cards.TarotCardArt
import eu.mermali.tarot.ui.cards.TarotCardImageResolver

@Composable
fun GameStatusScreen(gameState: GameState, onCreateTeam: () -> Unit, onVoteTeam: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = GameTableColor, contentColor = MaterialTheme.colorScheme.onBackground) {
        LandscapeFrame {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(modifier = Modifier.fillMaxWidth(0.92f), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                        QuestRow(missions = gameState.missions)
                        Spacer(Modifier.height(16.dp))
                        RejectVictoryIndicator(rejectedTeams = gameState.consecutiveRejectedTeams)
                    }

                    ReaderPanel(
                        reader = gameState.currentReader,
                        team = gameState.proposedTeam,
                        phase = gameState.phase,
                        onCreateTeam = onCreateTeam,
                        onVoteTeam = onVoteTeam,
                        modifier = Modifier.width(220.dp).height(220.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeFrame(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(GameTableColor), contentAlignment = Alignment.Center) {
        val shouldRotate = maxHeight > maxWidth
        val contentWidth = if (shouldRotate) maxHeight else maxWidth
        val contentHeight = if (shouldRotate) maxWidth else maxHeight
        val rotation = if (shouldRotate) 90f else 0f

        Box(modifier = Modifier.requiredSize(width = contentWidth, height = contentHeight).graphicsLayer(rotationZ = rotation)) { content() }
    }
}

@Composable
private fun QuestRow(missions: List<Mission>) {
    val missionSlots = (1..QuestCount).map { questIndex -> missions.firstOrNull { it.index == questIndex }}

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        missionSlots.forEachIndexed { index, mission ->
            QuestCard(questNumber = index + 1, mission = mission, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuestCard(questNumber: Int, mission: Mission?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val result = mission?.result ?: MissionResult.PENDING
    val resultColor = result.questColor()
    val imageAssetPath = rememberQuestImageAssetPath(result = result, mission = mission, context = context)
    val borderColor = if (result == MissionResult.PENDING) MaterialTheme.colorScheme.outlineVariant else resultColor
    val cardColor = if (result == MissionResult.PENDING) { MaterialTheme.colorScheme.surface } else { resultColor.copy(alpha = 0.14f) }

    Surface(
        modifier = modifier.height(176.dp),
        shape = RoundedCornerShape(8.dp),
        color = cardColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(2.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (result == MissionResult.PENDING) Arrangement.SpaceBetween else Arrangement.Center
        ) {
            HermitTokenBadges(tokens = mission?.tokens.orEmpty())
            if (result == MissionResult.PENDING) {
                Text(
                    text = "Quest $questNumber",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TarotCardArt(
                    assetPath = imageAssetPath,
                    contentDescription = "Quest $questNumber pending",
                    modifier = Modifier.height(82.dp),
                    fallbackLabel = "?"
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    QuestSpecLine(label = "Players", value = mission?.requiredPlayerCount?.toString() ?: "-")
                    QuestSpecLine(label = "REVERSED", value = mission?.reversedVotesRequired?.toString() ?: "-")
                }
            } else {
                TarotCardArt(
                    assetPath = imageAssetPath,
                    contentDescription = "Quest $questNumber ${result.questLabel()}",
                    modifier = Modifier.fillMaxWidth(0.86f),
                    fallbackLabel = result.questLabel()
                )
            }
        }
    }
}

@Composable
private fun QuestSpecLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun RejectVictoryIndicator(rejectedTeams: Int) {
    val threshold = RejectionRule.MAX_CONSECUTIVE_REJECTED_TEAMS
    val current = rejectedTeams.coerceIn(0, threshold)
    val remaining = (threshold - current).coerceAtLeast(0)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Reject victory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$remaining left",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RejectColor
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(threshold) { index -> RejectStep(filled = index < current, modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RejectStep(filled: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(16.dp).clip(RoundedCornerShape(8.dp)).background(if (filled) RejectColor else MaterialTheme.colorScheme.surfaceVariant).border(width = 1.dp, color = if (filled) RejectColor else MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp)))
}

@Composable
private fun ReaderPanel(reader: Player?, team: List<Player>, phase: GamePhase, onCreateTeam: () -> Unit, onVoteTeam: () -> Unit, modifier: Modifier = Modifier) {
    val hasTeam = team.isNotEmpty()
    val actionLabel = when {
        phase == GamePhase.TEAM_VOTING && hasTeam -> "Vote Team"
        phase == GamePhase.TEAM_PROPOSAL -> "Create team"
        else -> null
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.Start) {
            Column {
                Text(text = "Current Reader", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(text = reader?.displayName() ?: "-", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (hasTeam) {
                    Spacer(Modifier.height(10.dp))
                    Text(text = "Team", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = team.joinToString { it.displayName() }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }

            if (actionLabel != null) {
                Button(onClick = if (actionLabel == "Vote Team") onVoteTeam else onCreateTeam, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = actionLabel,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberQuestImageAssetPath(result: MissionResult, mission: Mission?, context: android.content.Context): String? {
    val seed = (mission?.index ?: 0) * 31 +
        (mission?.straightVoteCount ?: 0) * 7 +
        (mission?.reversedVoteCount ?: 0) * 13
    return androidx.compose.runtime.remember(context, result, seed, mission?.artKey) {
        if (result == MissionResult.PENDING) { TarotCardImageResolver.cardBackAssetPath() }
        else { mission?.artKey?.let { TarotCardImageResolver.artKeyAssetPath(it) } ?: TarotCardImageResolver.readingCardAssetPath(result, seed)}
    }
}

private fun Player.displayName(): String {
    return name.ifBlank { "Player ${position + 1}" }
}

private fun MissionResult.questLabel(): String {
    return when (this) {
        MissionResult.STRAIGHT -> "STRAIGHT"
        MissionResult.REVERSED -> "REVERSED"
        MissionResult.PENDING -> "?"
    }
}

@Composable
private fun MissionResult.questColor(): Color {
    return when (this) {
        MissionResult.STRAIGHT -> StraightQuestColor
        MissionResult.REVERSED -> ReversedQuestColor
        MissionResult.PENDING -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun HermitTokenBadges(tokens: Set<MissionToken>) {
    if (tokens.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        tokens.forEach { token ->
            HermitTokenBadge(token)
        }
    }

    Spacer(Modifier.height(6.dp))
}

@Composable
private fun HermitTokenBadge(token: MissionToken) {
    val label = when (token) {
        MissionToken.STRAIGHT_HERMIT -> "HS"
        MissionToken.REVERSED_HERMIT -> "HR"
    }

    val color = when (token) {
        MissionToken.STRAIGHT_HERMIT -> StraightQuestColor
        MissionToken.REVERSED_HERMIT -> ReversedQuestColor
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private const val QuestCount = 5
private val GameTableColor = Color(0xFFF7F3F8)
private val StraightQuestColor = Color(0xFF1565C0)
private val ReversedQuestColor = Color(0xFFC62828)
private val RejectColor = Color(0xFFC03945)
