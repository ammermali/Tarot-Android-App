package eu.mermali.tarot.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.mermali.tarot.domain.configuration.PlayerCountConfig
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.MissionResult
import eu.mermali.tarot.domain.model.MissionVote
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotAbility
import eu.mermali.tarot.domain.model.TarotCard
import eu.mermali.tarot.domain.model.TeamVote
import eu.mermali.tarot.domain.gamerules.InitialVisibility
import eu.mermali.tarot.domain.gamerules.VisibilityReason
import eu.mermali.tarot.game.gamestate.GameState
import eu.mermali.tarot.game.usecases.ConfirmCardSeen
import eu.mermali.tarot.game.usecases.CreateGame
import eu.mermali.tarot.game.usecases.ExportGameLog
import eu.mermali.tarot.game.usecases.ProposeTeam
import eu.mermali.tarot.game.usecases.ResolveFinalElimination
import eu.mermali.tarot.game.usecases.ResolveMission
import eu.mermali.tarot.game.usecases.ResolveTeamVote
import eu.mermali.tarot.game.usecases.RevealCard
import eu.mermali.tarot.game.usecases.SelectCards
import eu.mermali.tarot.game.usecases.SetPlayerCount
import eu.mermali.tarot.game.usecases.SetPlayers
import eu.mermali.tarot.game.usecases.StartGame
import eu.mermali.tarot.game.usecases.SubmitMissionVote
import eu.mermali.tarot.game.usecases.SubmitTeamVote

private data class TarotUseCases(
    val createGame: CreateGame = CreateGame(),
    val setPlayerCount: SetPlayerCount = SetPlayerCount(),
    val setPlayers: SetPlayers = SetPlayers(),
    val selectCards: SelectCards = SelectCards(),
    val startGame: StartGame = StartGame(),
    val revealCard: RevealCard = RevealCard(),
    val confirmCardSeen: ConfirmCardSeen = ConfirmCardSeen(),
    val proposeTeam: ProposeTeam = ProposeTeam(),
    val submitTeamVote: SubmitTeamVote = SubmitTeamVote(),
    val resolveTeamVote: ResolveTeamVote = ResolveTeamVote(),
    val submitMissionVote: SubmitMissionVote = SubmitMissionVote(),
    val resolveMission: ResolveMission = ResolveMission(),
    val resolveFinalElimination: ResolveFinalElimination = ResolveFinalElimination(),
    val exportGameLog: ExportGameLog = ExportGameLog()
)

@Composable
fun TarotGameScreen() {
    val useCases = remember { TarotUseCases() }
    var gameState by remember { mutableStateOf(useCases.createGame()) }
    var selectedTeamIds by remember { mutableStateOf(emptySet<Int>()) }
    var showMissionVote by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tarot Pass-and-Play", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(gameState.phase.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            gameState = useCases.createGame()
                            selectedTeamIds = emptySet()
                            showMissionVote = false
                        }
                    ) { Text("Reset") }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScorePanel(gameState)
            when (gameState.phase) {
                GamePhase.PLAYER_SETUP -> PlayerSetupPanel(gameState, useCases) { gameState = it }
                GamePhase.CARD_SETUP -> CardSetupPanel(gameState, useCases) { gameState = it }
                GamePhase.CARD_REVEAL -> RevealPanel(gameState, useCases) { gameState = it }
                GamePhase.CARD_ASSIGNMENT,
                GamePhase.INITIAL_INFORMATION -> WaitingPhasePanel(gameState.phase)
                GamePhase.TEAM_PROPOSAL -> TeamProposalPanel(
                    gameState = gameState,
                    selectedTeamIds = selectedTeamIds,
                    onTogglePlayer = { playerId ->
                        selectedTeamIds = if (playerId in selectedTeamIds) { selectedTeamIds - playerId } else { selectedTeamIds + playerId }
                    },
                    onPropose = {
                        gameState = useCases.proposeTeam(gameState, selectedTeamIds.toList())
                        selectedTeamIds = emptySet()
                    }
                )
                GamePhase.TEAM_VOTING -> TeamVotingPanel(gameState, useCases) { gameState = it }
                GamePhase.TEAM_VOTE_RESULT -> TeamVoteResultPanel(gameState, useCases) {
                    gameState = it
                    showMissionVote = false
                }
                GamePhase.MISSION_VOTING -> MissionVotingPanel(
                    gameState = gameState,
                    showMissionVote = showMissionVote,
                    useCases = useCases,
                    onShowMissionVote = { showMissionVote = true },
                    onMissionVote = { newState ->
                        gameState = newState
                        showMissionVote = false
                    }
                )
                GamePhase.MISSION_RESULT -> MissionResultPanel(gameState, useCases) {
                    gameState = it
                    showMissionVote = false
                    selectedTeamIds = emptySet()
                }
                GamePhase.DEVIL_GUESS -> WaitingPhasePanel(gameState.phase)
                GamePhase.FINAL_ELIMINATION -> FinalEliminationPanel(gameState, useCases) { gameState = it }
                GamePhase.GAME_OVER -> GameOverPanel(gameState, useCases)
            }
            MissionHistoryPanel(gameState)
            if (gameState.phase == GamePhase.GAME_OVER) { LogPanel(gameState) }
        }
    }
}

@Composable
private fun PlayerSetupPanel(gameState: GameState, useCases: TarotUseCases, onStateChange: (GameState) -> Unit) {
    val playerCountConfig = remember { PlayerCountConfig() }
    SectionCard {
        SectionTitle("Phase 1: Player setup")
        Text("Choose the number of players.")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            playerCountConfig.supportedPlayerCounts().sorted().forEach { playerCount ->
                val rules = useCases.setPlayerCount.rulesFor(playerCount)
                OutlinedButton(onClick = { onStateChange(useCases.setPlayerCount(gameState, playerCount)) }) {
                    Text("$playerCount (${rules?.straightCards}/${rules?.reversedCards})")
                }
            }
        }
    }
}

@Composable
private fun WaitingPhasePanel(phase: GamePhase) {
    SectionCard {
        SectionTitle(phase.name)
        Text("Fase disponibile nel dominio, non ancora usata dal loop UI.")
    }
}

@Composable
private fun CardSetupPanel(gameState: GameState, useCases: TarotUseCases, onStateChange: (GameState) -> Unit) {
    val rules = useCases.selectCards.rulesFor(gameState)
    val validation = useCases.selectCards.validationFor(gameState)
    val straightSelected = gameState.selectedCards.count { it.direction == CardDirection.STRAIGHT }
    val reversedSelected = gameState.selectedCards.count { it.direction == CardDirection.REVERSED }
    SectionCard {
        SectionTitle("Player names")
        gameState.players.sortedBy { it.position }.forEach { player ->
            TextField(
                value = player.name,
                onValueChange = { onStateChange(useCases.setPlayers.rename(gameState, player.id, it)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                singleLine = true,
                label = { Text("Player ${player.position + 1}") }
            )
        }
    }
    SectionCard {
        SectionTitle("Setup deck")
        DirectionLine(CardDirection.STRAIGHT, "STRAIGHT $straightSelected/${rules.straightCards}")
        DirectionLine(CardDirection.REVERSED, "REVERSED $reversedSelected/${rules.reversedCards}")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onStateChange(useCases.selectCards.fillDefault(gameState)) }) { Text("Default deck") }
            Button(onClick = { onStateChange(useCases.startGame(gameState)) }, enabled = validation.isValid) { Text("Start") }
        }
        if (!validation.isValid) {
            Spacer(Modifier.height(8.dp))
            validation.errors.forEach { error -> Text(error, color = MaterialTheme.colorScheme.error) }
        }
    }
    SectionCard {
        SectionTitle("Available cards")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            useCases.selectCards.availableCards().forEach { card ->
                CardPickerRow(
                    card = card,
                    isSelected = gameState.selectedCards.any { it.id == card.id },
                    canAdd = useCases.selectCards.canAdd(gameState, card),
                    onAdd = { onStateChange(useCases.selectCards.add(gameState, card.id)) },
                    onRemove = { onStateChange(useCases.selectCards.remove(gameState, card.id)) }
                )
            }
        }
    }
}

@Composable
private fun CardPickerRow(card: TarotCard, isSelected: Boolean, canAdd: Boolean, onAdd: () -> Unit, onRemove: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DirectionDot(card.direction)
            Column(modifier = Modifier.weight(1f)) {
                Text(card.displayName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(card.abilityLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) { OutlinedButton(onClick = onRemove) { Text("Remove") } }
            else { Button(onClick = onAdd, enabled = canAdd) { Text("Add") } }
        }
    }
}

@Composable
private fun RevealPanel(gameState: GameState, useCases: TarotUseCases, onStateChange: (GameState) -> Unit) {
    val reveal = useCases.revealCard(gameState)
    SectionCard {
        SectionTitle("Reveal pass-and-play")
        Text("Passa il device a ${reveal.player.displayName()}.")
        Spacer(Modifier.height(10.dp))
        PlayerCard(reveal.player, showCard = true)
        Spacer(Modifier.height(10.dp))
        InitialInformation(reveal.visibleInformation, gameState.players)
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onStateChange(useCases.confirmCardSeen(gameState)) }) { Text("Conferma") }
    }
}

@Composable
private fun TeamProposalPanel(gameState: GameState, selectedTeamIds: Set<Int>, onTogglePlayer: (Int) -> Unit, onPropose: () -> Unit) {
    val mission = gameState.currentMission
    SectionCard {
        SectionTitle("Team Proposal")
        Text("Reader: ${gameState.currentReader?.displayName() ?: "-"}")
        Text("Reading ${mission?.index ?: "-"}: choose ${mission?.requiredPlayerCount ?: 0} players.")
        Spacer(Modifier.height(10.dp))
        gameState.players.sortedBy { it.position }.forEach { player ->
            val selected = player.id in selectedTeamIds
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(player.displayName(), modifier = Modifier.weight(1f))
                if (selected) { Button(onClick = { onTogglePlayer(player.id) }) { Text("Selected") } }
                else { OutlinedButton(onClick = { onTogglePlayer(player.id) }) { Text("Pick") } }
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = onPropose, enabled = mission != null && selectedTeamIds.size == mission.requiredPlayerCount) { Text("Proponi squadra") }
    }
}

@Composable
private fun TeamVotingPanel(gameState: GameState, useCases: TarotUseCases, onStateChange: (GameState) -> Unit) {
    SectionCard {
        SectionTitle("Voto squadra")
        Text("Team: ${gameState.proposedTeam.joinToString { it.displayName() }}")
        Spacer(Modifier.height(10.dp))
        gameState.players.sortedBy { it.position }.forEach { player ->
            val alreadyVoted = gameState.teamVotes.containsKey(player.id)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(player.displayName(), modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { onStateChange(useCases.submitTeamVote(gameState, player.id, TeamVote.REJECT)) }, enabled = !alreadyVoted) { Text("Reject") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onStateChange(useCases.submitTeamVote(gameState, player.id, TeamVote.APPROVE)) }, enabled = !alreadyVoted) { Text("Approve") }
            }
        }
    }
}

@Composable
private fun TeamVoteResultPanel(gameState: GameState, useCases: TarotUseCases, onStateChange: (GameState) -> Unit) {
    SectionCard {
        SectionTitle("Risultato voto squadra")
        Text("Approve: ${gameState.teamVotes.values.count { it == TeamVote.APPROVE }}")
        Text("Reject: ${gameState.teamVotes.values.count { it == TeamVote.REJECT }}")
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onStateChange(useCases.resolveTeamVote(gameState)) }) { Text("Continua") }
    }
}

@Composable
private fun MissionVotingPanel(
    gameState: GameState,
    showMissionVote: Boolean,
    useCases: TarotUseCases,
    onShowMissionVote: () -> Unit,
    onMissionVote: (GameState) -> Unit
) {
    val team = if (gameState.proposedTeam.isNotEmpty()) { gameState.proposedTeam } else { gameState.currentMission?.proposedTeam.orEmpty() }
    val currentPlayer = team.sortedBy { it.position }.getOrNull(gameState.missionVotes.size)
    SectionCard {
        SectionTitle("Lettura")
        if (currentPlayer == null) {
            Text("Tutti i voti sono stati raccolti.")
            return@SectionCard
        }
        Spacer(Modifier.height(10.dp))

        if (!showMissionVote) {
            Text("Passa il device a ${currentPlayer.displayName()}.")
            Spacer(Modifier.height(10.dp))
            Button(onClick = onShowMissionVote, modifier = Modifier.fillMaxWidth()) {
                Text("Sono ${currentPlayer.displayName()}")
            }
        } else {
            Text("${currentPlayer.displayName()}, scegli la carta da giocare.")
            Text("Il voto non viene mostrato agli altri giocatori.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onMissionVote(useCases.submitMissionVote(gameState, currentPlayer.id, MissionVote.STRAIGHT)) },
                    modifier = Modifier.weight(1f)
                ) { Text("STRAIGHT") }
                Button(
                    onClick = { onMissionVote(useCases.submitMissionVote(gameState, currentPlayer.id, MissionVote.REVERSED)) },
                    enabled = currentPlayer.card?.direction == CardDirection.REVERSED,
                    modifier = Modifier.weight(1f)
                ) { Text("REVERSED") }
            }
        }
    }
}

@Composable
private fun MissionResultPanel(gameState: GameState, useCases: TarotUseCases, onStateChange: (GameState) -> Unit) {
    SectionCard {
        SectionTitle("Risultato Lettura")
        Text("Voti raccolti: ${gameState.missionVotes.size}/${gameState.currentMission?.requiredPlayerCount ?: 0}")
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onStateChange(useCases.resolveMission(gameState)) }) { Text("Risolvi Lettura") }
    }
}

@Composable
private fun FinalEliminationPanel(gameState: GameState, useCases: TarotUseCases, onStateChange: (GameState) -> Unit) {
    val eliminator = gameState.players.firstOrNull { it.card?.canAttemptFinalElimination == true }
    SectionCard {
        SectionTitle("Phase 4: Eliminazione finale")
        Text("Reversed Death: ${eliminator?.displayName() ?: "-"}")
        Spacer(Modifier.height(10.dp))
        gameState.players.sortedBy { it.position }.forEach { target ->
            Button(
                onClick = { if (eliminator != null) { onStateChange(useCases.resolveFinalElimination(gameState, eliminator.id, target.id)) } },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                enabled = eliminator != null
            ) { Text("Scegli ${target.displayName()}") }
        }
    }
}

@Composable
private fun GameOverPanel(gameState: GameState, useCases: TarotUseCases) {
    SectionCard {
        SectionTitle("Game over")
        val winner = gameState.winner
        if (winner != null) { DirectionLine(winner.side, "Vittoria ${winner.side}: ${winner.reason}") }
        Spacer(Modifier.height(10.dp))
        Text(useCases.exportGameLog(gameState), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ScorePanel(gameState: GameState) {
    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScoreTile("STRAIGHT", gameState.straightScore.toString(), straightColor(), Modifier.weight(1f))
            ScoreTile("REVERSED", gameState.reversedScore.toString(), reversedColor(), Modifier.weight(1f))
            ScoreTile("Rifiuti", gameState.consecutiveRejectedTeams.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Text("Reader: ${gameState.currentReader?.displayName() ?: "-"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ScoreTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MissionHistoryPanel(gameState: GameState) {
    SectionCard {
        SectionTitle("Storico Letture")
        gameState.missions.forEach { mission ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("L${mission.index}", fontWeight = FontWeight.SemiBold)
                Text(mission.result.name, color = mission.result.color())
                Text("${mission.reversedVoteCount}/${mission.reversedVotesRequired} REVERSED", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LogPanel(gameState: GameState) {
    SectionCard {
        SectionTitle("Phase 5: Log")
        gameState.gameLog.takeLast(10).asReversed().forEach { logLine ->
            Text(logLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlayerCard(player: Player, showCard: Boolean) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(player.displayName(), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            if (showCard) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(player.card?.displayName ?: "-", fontWeight = FontWeight.SemiBold)
                    Text(player.card?.abilityLabel() ?: "No ability", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun InitialInformation(information: List<InitialVisibility>, players: List<Player>) {
    if (information.isEmpty()) {
        Text("Nessuna informazione iniziale.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    information.forEach { item ->
        val visibleNames = item.visiblePlayerIds.mapNotNull { id -> players.firstOrNull { it.id == id }?.displayName() }.ifEmpty { listOf("-") }.joinToString()
        Text("${item.reason.label()}: $visibleNames")
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun DirectionLine(direction: CardDirection, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DirectionDot(direction)
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DirectionDot(direction: CardDirection) {
    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(direction.color()))
}

private fun Player.displayName(): String = name.ifBlank { "Player ${position + 1}" }

private fun TarotCard.abilityLabel(): String {
    val abilityNames = abilities.joinToString { it.label() }
    return if (abilityNames.isBlank()) { "No ability" } else { abilityNames }
}

private fun TarotAbility.label(): String = when (this) {
    TarotAbility.SeesReversed -> "sees reversed"
    TarotAbility.SeesOracles -> "sees oracles"
    TarotAbility.FinalEliminator -> "final eliminator"
    TarotAbility.AppearsAsOracle -> "appears as oracle"
    TarotAbility.HiddenFromSight -> "hidden from sight"
    TarotAbility.IsolatedReversed -> "isolated reversed"
    TarotAbility.CanCastMagic -> "can cast magic"
    TarotAbility.SeesFinalEliminator -> "can see the final eliminator"
    TarotAbility.AppearsReversed -> "appears reversed"
}

private fun VisibilityReason.label(): String = when (this) {
    VisibilityReason.REVERSED_NETWORK -> "Reversed network"
    VisibilityReason.ARCANE_SIGHT -> "Arcane sight"
    VisibilityReason.ORACLE_SIGHT -> "Oracle sight"
    VisibilityReason.FINAL_ELIMINATOR_SIGHT -> "Sight of Final Eliminator"
}

private fun CardDirection.color(): Color = when (this) {
    CardDirection.STRAIGHT -> straightColor()
    CardDirection.REVERSED -> reversedColor()
}

@Composable
private fun MissionResult.color(): Color = when (this) {
    MissionResult.STRAIGHT -> straightColor()
    MissionResult.REVERSED -> reversedColor()
    MissionResult.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun straightColor(): Color = Color(0xFF2E7D50)

private fun reversedColor(): Color = Color(0xFFB43C4A)
