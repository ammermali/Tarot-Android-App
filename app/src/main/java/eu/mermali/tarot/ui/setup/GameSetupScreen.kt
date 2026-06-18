package eu.mermali.tarot.ui.setup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.mermali.tarot.domain.configuration.PlayerCountConfig
import eu.mermali.tarot.domain.configuration.TarotCardPreset
import eu.mermali.tarot.domain.configuration.TarotCardValidationResult
import eu.mermali.tarot.domain.configuration.TarotCardValidator
import eu.mermali.tarot.domain.model.CardDirection
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.domain.model.TarotCard
import eu.mermali.tarot.game.gamestate.GameState
import eu.mermali.tarot.game.usecases.StartGame
import eu.mermali.tarot.ui.cards.TarotCardArt
import eu.mermali.tarot.ui.cards.TarotCardImageResolver

private data class SetupPlayer(val name: String)

private data class SetupState(val playerCount: Int, val players: List<SetupPlayer>, val deck: List<TarotCard>)

private data class SelectedRoleDetails(val card: TarotCard, val roleDescription: RoleDescription)

private enum class FaceCardGroup(val idPrefix: String, val displayName: String, val direction: CardDirection, val description: String) {
    STRAIGHT(
        idPrefix = "straight_face_card",
        displayName = "Straight Face Card",
        direction = CardDirection.STRAIGHT,
        description = "A STRAIGHT player with no special ability."
    ),
    REVERSED(
        idPrefix = "reversed_face_card",
        displayName = "Reversed Face Card",
        direction = CardDirection.REVERSED,
        description = "A REVERSED player with no special ability."
    )
}

@Composable
fun GameSetupScreen(onBack: () -> Unit, onStartGame: (GameState) -> Unit) {
    val playerCountConfig = remember { PlayerCountConfig() }
    val cardPreset = remember { TarotCardPreset() }
    val cardValidator = remember { TarotCardValidator() }
    val startGame = remember { StartGame() }
    val context = LocalContext.current
    val supportedPlayerCounts = remember { playerCountConfig.supportedPlayerCounts().sorted() }
    val availableCards = remember { cardPreset.defaultDeck() }
    val roleDescriptions = remember(context) { RoleDescriptionRepository(context).load() }
    var setupState by remember { mutableStateOf(createSetupState(5, cardPreset)) }
    var selectedRoleDetails by remember { mutableStateOf<SelectedRoleDetails?>(null) }
    val validation = setupState.validate(cardValidator, playerCountConfig)

    selectedRoleDetails?.let { selectedRole ->
        RoleDescriptionDialog(
            card = selectedRole.card,
            roleDescription = selectedRole.roleDescription,
            onDismiss = { selectedRoleDetails = null }
        )
    }

    Scaffold(topBar = { SetupTopBar(onBack = onBack) })
    { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayerCountSection(
                selectedPlayerCount = setupState.playerCount,
                supportedPlayerCounts = supportedPlayerCounts,
                onPlayerCountSelected = { playerCount -> setupState = createSetupState(playerCount = playerCount, cardPreset = cardPreset, existingPlayers = setupState.players)}
            )

            PlayerOrderSection(
                players = setupState.players,
                onNameChange = { index, name -> setupState = setupState.renamePlayer(index, name) },
                onMove = { index, direction -> setupState = setupState.movePlayer(index, direction) }
            )

            DeckSection(
                setupState = setupState,
                availableCards = availableCards,
                roleDescriptions = roleDescriptions,
                validation = validation,
                canAddCard = { card -> setupState.canAddCard(card, playerCountConfig) },
                onResetDeck = { setupState = setupState.copy(deck = cardPreset.defaultSelection(setupState.playerCount)) },
                onAddCard = { card -> setupState = setupState.addCard(card) },
                onRemoveCard = { card -> setupState = setupState.removeCard(card) },
                onShowRoleDescription = { card, roleDescription -> selectedRoleDetails = SelectedRoleDetails(card, roleDescription) }
            )

            Button(onClick = { onStartGame(startGame(setupState.toGameState())) }, enabled = validation.isValid, modifier = Modifier.fillMaxWidth()) { Text("Start Game") }
        }
    }
}

@Composable
private fun SetupTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 6.dp)) { Text("Back") }
            Text(
                text = "Setup",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PlayerCountSection(selectedPlayerCount: Int, supportedPlayerCounts: List<Int>, onPlayerCountSelected: (Int) -> Unit) {
    SetupSection(title = "Players") {
        val playerCountRows = supportedPlayerCounts.chunked(3)

        Text(text = "Number of players", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        playerCountRows.forEachIndexed { rowIndex, rowCounts ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowCounts.forEach { playerCount ->
                    PlayerCountButton(
                        playerCount = playerCount,
                        selected = selectedPlayerCount == playerCount,
                        onClick = { onPlayerCountSelected(playerCount) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowCounts.size) { Spacer(Modifier.weight(1f)) }
            }
            if (rowIndex < playerCountRows.lastIndex) { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun PlayerCountButton(playerCount: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (selected) { Button(onClick = onClick, modifier = modifier) { Text(playerCount.toString()) }
    } else { OutlinedButton(onClick = onClick, modifier = modifier) { Text(playerCount.toString()) } }
}

@Composable
private fun PlayerOrderSection(players: List<SetupPlayer>, onNameChange: (Int, String) -> Unit, onMove: (Int, Int) -> Unit) {
    SetupSection(title = "Player names and order") {
        players.forEachIndexed { index, player ->
            PlayerSetupRow(
                position = index + 1,
                name = player.name,
                canMoveUp = index > 0,
                canMoveDown = index < players.lastIndex,
                onNameChange = { name -> onNameChange(index, name) },
                onMoveUp = { onMove(index, -1) },
                onMoveDown = { onMove(index, 1) }
            )
            if (index < players.lastIndex) { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun DeckSection(
    setupState: SetupState,
    availableCards: List<TarotCard>,
    roleDescriptions: Map<String, RoleDescription>,
    validation: TarotCardValidationResult,
    canAddCard: (TarotCard) -> Boolean,
    onResetDeck: () -> Unit,
    onAddCard: (TarotCard) -> Unit,
    onRemoveCard: (TarotCard) -> Unit,
    onShowRoleDescription: (TarotCard, RoleDescription) -> Unit
) {
    SetupSection(title = "Deck") {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${setupState.deck.size}/${setupState.playerCount} cards selected", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onResetDeck) { Text("Reset") }
        }
        Text(text = setupState.deckCountLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        ValidationMessages(validation)
        SelectedDeckList(
            title = "In play",
            setupState = setupState,
            availableCards = availableCards,
            roleDescriptions = roleDescriptions,
            canAddCard = canAddCard,
            onShowRoleDescription = onShowRoleDescription,
            onAddCard = onAddCard,
            onRemoveCard = onRemoveCard
        )
        AvailableNamedRoleList(
            title = "Available",
            cards = availableCards.filter { card -> card.faceCardGroup() == null && setupState.deck.none { it.id == card.id } },
            roleDescriptions = roleDescriptions,
            canAddCard = canAddCard,
            onShowRoleDescription = onShowRoleDescription,
            onAddCard = onAddCard
        )
    }
}

@Composable
private fun ValidationMessages(validation: TarotCardValidationResult) {
    if (validation.isValid) { return }

    Spacer(Modifier.height(8.dp))
    validation.errors.forEach { error -> Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)}
}

@Composable
private fun SelectedDeckList(
    title: String,
    setupState: SetupState,
    availableCards: List<TarotCard>,
    roleDescriptions: Map<String, RoleDescription>,
    canAddCard: (TarotCard) -> Boolean,
    onShowRoleDescription: (TarotCard, RoleDescription) -> Unit,
    onAddCard: (TarotCard) -> Unit,
    onRemoveCard: (TarotCard) -> Unit
) {
    Spacer(Modifier.height(12.dp))
    Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(6.dp))

    val namedCards = setupState.deck.filter { it.faceCardGroup() == null }

    namedCards.forEach { card ->
        val roleDescription = roleDescriptions.roleDescriptionFor(card)
        DeckCardRow(
            roleDescription = roleDescription,
            action = "Remove",
            enabled = true,
            onRoleClick = { onShowRoleDescription(card, roleDescription) },
            onAction = { onRemoveCard(card) }
        )
        Spacer(Modifier.height(6.dp))
    }

    FaceCardGroup.values().forEachIndexed { index, group ->
        val selectedFaceCards = setupState.deck.filter { it.faceCardGroup() == group }
        val addableFaceCard = availableCards.firstOrNull { card -> card.faceCardGroup() == group && setupState.deck.none { it.id == card.id } && canAddCard(card) }
        val representativeCard = selectedFaceCards.firstOrNull() ?: availableCards.firstOrNull { it.faceCardGroup() == group } ?: return@forEachIndexed
        val roleDescription = roleDescriptions.roleDescriptionFor(representativeCard)

        FaceCardCounterRow(
            roleDescription = roleDescription,
            count = selectedFaceCards.size,
            canDecrease = selectedFaceCards.isNotEmpty(),
            canIncrease = addableFaceCard != null,
            onRoleClick = { onShowRoleDescription(representativeCard, roleDescription) },
            onDecrease = { selectedFaceCards.lastOrNull()?.let(onRemoveCard) },
            onIncrease = { addableFaceCard?.let(onAddCard) }
        )
        if (index < FaceCardGroup.values().lastIndex) { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun AvailableNamedRoleList(
    title: String,
    cards: List<TarotCard>,
    roleDescriptions: Map<String, RoleDescription>,
    canAddCard: (TarotCard) -> Boolean,
    onShowRoleDescription: (TarotCard, RoleDescription) -> Unit,
    onAddCard: (TarotCard) -> Unit
) {
    Spacer(Modifier.height(12.dp))
    Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(6.dp))

    if (cards.isEmpty()) {
        Text(text = "No named roles available.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    cards.forEachIndexed { index, card ->
        val roleDescription = roleDescriptions.roleDescriptionFor(card)
        DeckCardRow(
            roleDescription = roleDescription,
            action = "Add",
            enabled = canAddCard(card),
            onRoleClick = { onShowRoleDescription(card, roleDescription) },
            onAction = { onAddCard(card) }
        )
        if (index < cards.lastIndex) { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun SetupSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun PlayerSetupRow(position: Int, name: String, canMoveUp: Boolean, canMoveDown: Boolean, onNameChange: (String) -> Unit, onMoveUp: () -> Unit, onMoveDown: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = position.toString(),
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("Name") }
        )
        Spacer(Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = onMoveUp, enabled = canMoveUp) { Text("↑") }
            OutlinedButton(onClick = onMoveDown, enabled = canMoveDown) { Text("↓") }
        }
    }
}

@Composable
private fun DeckCardRow(roleDescription: RoleDescription, action: String, enabled: Boolean = true, onRoleClick: () -> Unit, onAction: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onRoleClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = roleDescription.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedButton(onClick = onAction, enabled = enabled) { Text(action) }
        }
    }
}

@Composable
private fun FaceCardCounterRow(roleDescription: RoleDescription, count: Int, canDecrease: Boolean, canIncrease: Boolean, onRoleClick: () -> Unit, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onRoleClick), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = roleDescription.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            OutlinedButton(onClick = onDecrease, enabled = canDecrease,modifier = Modifier.width(48.dp)) { Text("-") }
            Text(text = count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = onIncrease, enabled = canIncrease, modifier = Modifier.width(48.dp)) { Text("+") }
        }
    }
}

@Composable
private fun RoleDescriptionDialog(card: TarotCard, roleDescription: RoleDescription, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val imageAssetPath = remember(context, card) { TarotCardImageResolver.roleCardAssetPath(card) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = roleDescription.name, fontWeight = FontWeight.Bold ) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TarotCardArt(assetPath = imageAssetPath, contentDescription = roleDescription.name, modifier = Modifier.fillMaxWidth(0.62f))
                Text(text = roleDescription.description.ifBlank { "No description available yet." }, style = MaterialTheme.typography.bodyLarge)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private fun createSetupState(playerCount: Int, cardPreset: TarotCardPreset, existingPlayers: List<SetupPlayer> = emptyList()): SetupState {
    val players = List(playerCount) { index -> SetupPlayer(name = existingPlayers.getOrNull(index)?.name ?: "Player ${index + 1}") }

    return SetupState(playerCount = playerCount, players = players, deck = cardPreset.defaultSelection(playerCount))
}

private fun SetupState.renamePlayer(index: Int, name: String): SetupState {
    return copy(players = players.mapIndexed { playerIndex, player -> if (playerIndex == index) player.copy(name = name) else player })
}

private fun SetupState.movePlayer(index: Int, direction: Int): SetupState {
    val targetIndex = index + direction
    if (targetIndex !in players.indices) { return this }

    val reorderedPlayers = players.toMutableList()
    val selectedPlayer = reorderedPlayers[index]
    reorderedPlayers[index] = reorderedPlayers[targetIndex]
    reorderedPlayers[targetIndex] = selectedPlayer
    return copy(players = reorderedPlayers)
}

private fun SetupState.addCard(card: TarotCard): SetupState {
    if (deck.size >= playerCount || deck.any { it.id == card.id }) { return this }

    return copy(deck = deck + card)
}

private fun SetupState.removeCard(card: TarotCard): SetupState {
    return copy(deck = deck.filterNot { it.id == card.id })
}

private fun SetupState.toGameState(): GameState {
    val gamePlayers = players.mapIndexed { index, player -> Player(id = index + 1, name = player.name.trim().ifBlank { "Player ${index + 1}" }, position = index)}

    return GameState(players = gamePlayers, selectedCards = deck, phase = GamePhase.CARD_SETUP)
}

private fun SetupState.validate(cardValidator: TarotCardValidator, playerCountConfig: PlayerCountConfig): TarotCardValidationResult {
    val errors = cardValidator.validate(playerCount, deck).errors.toMutableList()
    val rules = playerCountConfig.forPlayerCount(playerCount)

    if (rules != null) {
        val straightCount = deck.count { it.direction == CardDirection.STRAIGHT }
        val reversedCount = deck.count { it.direction == CardDirection.REVERSED }

        if (straightCount != rules.straightCards) { errors.add("Select ${rules.straightCards} STRAIGHT cards.") }
        if (reversedCount != rules.reversedCards) { errors.add("Select ${rules.reversedCards} REVERSED cards.") }
    }

    return TarotCardValidationResult(isValid = errors.isEmpty(), errors = errors.distinct())
}

private fun SetupState.canAddCard(card: TarotCard, playerCountConfig: PlayerCountConfig): Boolean {
    val rules = playerCountConfig.forPlayerCount(playerCount) ?: return false
    if (deck.size >= playerCount || deck.any { it.id == card.id }) { return false }

    val currentDirectionCount = deck.count { it.direction == card.direction }
    val maxDirectionCount = when (card.direction) {
        CardDirection.STRAIGHT -> rules.straightCards
        CardDirection.REVERSED -> rules.reversedCards
    }
    return currentDirectionCount < maxDirectionCount
}

private fun SetupState.deckCountLabel(): String {
    val straightCount = deck.count { it.direction == CardDirection.STRAIGHT }
    val reversedCount = deck.count { it.direction == CardDirection.REVERSED }
    return "STRAIGHT $straightCount - REVERSED $reversedCount"
}

private fun TarotCard.faceCardGroup(): FaceCardGroup? {
    return FaceCardGroup.values().firstOrNull { group -> id.startsWith(group.idPrefix) }
}

private fun Map<String, RoleDescription>.roleDescriptionFor(card: TarotCard): RoleDescription {
    val faceCardGroup = card.faceCardGroup()
    return this[card.id]
        ?: faceCardGroup?.let { group -> RoleDescription(id = group.idPrefix, name = group.displayName, description = group.description) }
        ?: RoleDescription(id = card.id, name = card.displayName, description = "No description available yet.")
}
