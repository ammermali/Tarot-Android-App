package eu.mermali.tarot.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import eu.mermali.tarot.domain.model.GamePhase
import eu.mermali.tarot.domain.model.MissionVote
import eu.mermali.tarot.domain.model.TeamVote
import eu.mermali.tarot.game.history.GameHistoryRepository
import eu.mermali.tarot.game.gamestate.GameState
import eu.mermali.tarot.game.usecases.ConfirmCardSeen
import eu.mermali.tarot.game.usecases.ProposeTeam
import eu.mermali.tarot.game.usecases.ResolveFinalElimination
import eu.mermali.tarot.game.usecases.ResolveMission
import eu.mermali.tarot.game.usecases.ResolveTeamVote
import eu.mermali.tarot.game.usecases.SubmitMissionVote
import eu.mermali.tarot.game.usecases.SubmitTeamVote
import eu.mermali.tarot.game.usecases.ResolveDevilGuess
import eu.mermali.tarot.ui.game.GameOverScreen
import eu.mermali.tarot.ui.game.GameStatusScreen
import eu.mermali.tarot.ui.game.MissionReadingRevealScreen
import eu.mermali.tarot.ui.game.MissionReadingResultScreen
import eu.mermali.tarot.ui.game.MissionVoteScreen
import eu.mermali.tarot.ui.game.PostGameEliminationScreen
import eu.mermali.tarot.ui.game.TeamCreationScreen
import eu.mermali.tarot.ui.game.TeamVoteResultScreen
import eu.mermali.tarot.ui.game.TeamVoteScreen
import eu.mermali.tarot.ui.game.PostGameDevilGuessScreen
import eu.mermali.tarot.ui.logs.GameLogsScreen
import eu.mermali.tarot.ui.main.MainMenuScreen
import eu.mermali.tarot.ui.reveal.RoleRevealScreen
import eu.mermali.tarot.ui.setup.GameSetupScreen

private enum class AppScreen {
    MAIN_MENU,
    SETUP,
    ROLE_REVEAL,
    GAME_STATUS,
    TEAM_CREATION,
    TEAM_VOTING,
    TEAM_VOTE_RESULT,
    MISSION_VOTING,
    MISSION_READING_REVEAL,
    MISSION_READING_RESULT,
    DEVIL_GUESS,
    FINAL_ELIMINATION,
    GAME_OVER,
    LOGS
}

@Composable
fun TarotApp() {
    // Global UI state
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var startedGameState by remember { mutableStateOf<GameState?>(null) }
    val gameHistoryRepository = remember(context) { GameHistoryRepository(context) }
    val proposeTeam = remember { ProposeTeam() }
    val submitTeamVote = remember { SubmitTeamVote() }
    val resolveTeamVote = remember { ResolveTeamVote() }
    val submitMissionVote = remember { SubmitMissionVote() }
    val resolveMission = remember { ResolveMission() }
    val resolveFinalElimination = remember { ResolveFinalElimination() }
    val resolveDevilGuess = remember { ResolveDevilGuess() }

    fun returnToMainMenu(saveCompletedGame: Boolean = false) {
        if (saveCompletedGame) {
            startedGameState?.let { gameState -> gameHistoryRepository.saveCompletedGame(gameState) }
        }
        startedGameState = null
        currentScreen = AppScreen.MAIN_MENU
    }

    Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
        when (currentScreen) {
            AppScreen.MAIN_MENU -> MainMenuScreen(
                onPlay = { currentScreen = AppScreen.SETUP },
                onOpenLogs = { currentScreen = AppScreen.LOGS }
            )

            AppScreen.SETUP -> GameSetupScreen(
                onBack = { currentScreen = AppScreen.MAIN_MENU },
                onStartGame = { gameState ->
                    startedGameState = gameState
                    currentScreen = AppScreen.ROLE_REVEAL
                }
            )

            AppScreen.ROLE_REVEAL -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    RoleRevealScreen(
                        gameState = gameState,
                        onComplete = {
                            startedGameState = gameState.completeRoleReveal()
                            currentScreen = AppScreen.GAME_STATUS
                        },
                        onBack = {
                            startedGameState = null
                            currentScreen = AppScreen.SETUP
                        }
                    )
                }
            }

            AppScreen.GAME_STATUS -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    GameStatusScreen(
                        gameState = gameState,
                        onCreateTeam = { currentScreen = AppScreen.TEAM_CREATION },
                        onVoteTeam = { currentScreen = AppScreen.TEAM_VOTING }
                    )
                }
            }

            AppScreen.TEAM_CREATION -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    TeamCreationScreen(
                        gameState = gameState,
                        onBack = { currentScreen = AppScreen.GAME_STATUS },
                        onConfirmTeam = { selectedPlayerIds, watchTokenPlayerId ->
                            startedGameState = proposeTeam(gameState, selectedPlayerIds, watchTokenPlayerId)
                            currentScreen = AppScreen.GAME_STATUS
                        }
                    )
                }
            }

            AppScreen.TEAM_VOTING -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    TeamVoteScreen(
                        gameState = gameState,
                        onBack = { currentScreen = AppScreen.GAME_STATUS },
                        onSubmitVote = { playerId: Int, vote: TeamVote ->
                            val updatedState = submitTeamVote(gameState, playerId, vote)
                            startedGameState = updatedState
                            if (updatedState.phase == GamePhase.TEAM_VOTE_RESULT) {
                                currentScreen = AppScreen.TEAM_VOTE_RESULT
                            }
                        }
                    )
                }
            }

            AppScreen.TEAM_VOTE_RESULT -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    TeamVoteResultScreen(
                        gameState = gameState,
                        onContinue = {
                            val updatedState = resolveTeamVote(gameState)
                            startedGameState = updatedState
                            currentScreen = when (updatedState.phase) {
                                GamePhase.MISSION_VOTING -> AppScreen.MISSION_VOTING
                                GamePhase.GAME_OVER -> AppScreen.GAME_OVER
                                else -> AppScreen.GAME_STATUS
                            }
                        }
                    )
                }
            }

            AppScreen.MISSION_VOTING -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    MissionVoteScreen(
                        gameState = gameState,
                        onBack = { currentScreen = AppScreen.GAME_STATUS },
                        onSubmitVote = { playerId: Int, vote: MissionVote ->
                            val updatedState = submitMissionVote(gameState, playerId, vote)
                            startedGameState = updatedState
                            if (updatedState.phase == GamePhase.MISSION_RESULT) {
                                currentScreen = AppScreen.MISSION_READING_REVEAL
                            }
                        }
                    )
                }
            }

            AppScreen.MISSION_READING_REVEAL -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    MissionReadingRevealScreen(
                        gameState = gameState,
                        onRevealReading = { currentScreen = AppScreen.MISSION_READING_RESULT }
                    )
                }
            }

            AppScreen.MISSION_READING_RESULT -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    MissionReadingResultScreen(
                        gameState = gameState,
                        onChooseNextTeam = {
                            val updatedState = resolveMission(gameState)
                            startedGameState = updatedState
                            currentScreen = when (updatedState.phase) {
                                GamePhase.DEVIL_GUESS -> AppScreen.DEVIL_GUESS
                                GamePhase.FINAL_ELIMINATION -> AppScreen.FINAL_ELIMINATION
                                GamePhase.GAME_OVER -> AppScreen.GAME_OVER
                                else -> AppScreen.GAME_STATUS
                            }
                        }
                    )
                }
            }

            AppScreen.DEVIL_GUESS -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = {currentScreen = AppScreen.SETUP},
                        onOpenLogs = {currentScreen = AppScreen.LOGS}
                    )
                } else {
                    PostGameDevilGuessScreen(
                        gameState = gameState,
                        onTargetSelected = { deathPlayerId, targetPlayerId ->
                            val updateState = resolveDevilGuess(gameState, deathPlayerId, targetPlayerId)
                            startedGameState = updateState
                            currentScreen = AppScreen.FINAL_ELIMINATION
                        },
                        onSkipToFinalElimination = { currentScreen = AppScreen.FINAL_ELIMINATION }
                    )
                }
            }

            AppScreen.FINAL_ELIMINATION -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    PostGameEliminationScreen(
                        gameState = gameState,
                        onTargetSelected = { eliminatorPlayerId, targetPlayerId ->
                            val updatedState = resolveFinalElimination(gameState, eliminatorPlayerId, targetPlayerId)
                            startedGameState = updatedState
                            currentScreen = AppScreen.GAME_OVER
                        },
                        onMainMenu = { returnToMainMenu(saveCompletedGame = true) }
                    )
                }
            }

            AppScreen.GAME_OVER -> {
                val gameState = startedGameState
                if (gameState == null) {
                    MainMenuScreen(
                        onPlay = { currentScreen = AppScreen.SETUP },
                        onOpenLogs = { currentScreen = AppScreen.LOGS }
                    )
                } else {
                    LaunchedEffect(gameState.gameLog, gameState.winner) {
                        gameHistoryRepository.saveCompletedGame(gameState)
                    }
                    GameOverScreen(
                        gameState = gameState,
                        onMainMenu = { returnToMainMenu(saveCompletedGame = true) }
                    )
                }
            }

            AppScreen.LOGS -> GameLogsScreen(onBack = { currentScreen = AppScreen.MAIN_MENU })
        }
    }
}

private fun GameState.completeRoleReveal(): GameState {
    if (players.isEmpty() || phase != GamePhase.CARD_REVEAL) { return this }

    val confirmCardSeen = ConfirmCardSeen()
    var updatedState = this
    while (updatedState.phase == GamePhase.CARD_REVEAL) { updatedState = confirmCardSeen(updatedState) }
    return updatedState
}
