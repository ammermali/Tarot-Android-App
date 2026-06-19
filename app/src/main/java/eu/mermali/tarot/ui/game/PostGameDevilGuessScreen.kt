package eu.mermali.tarot.ui.game
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.mermali.tarot.domain.model.Player
import eu.mermali.tarot.game.gamestate.GameState

@Composable
fun PostGameDevilGuessScreen(
    gameState: GameState,
    onTargetSelected: (deathPlayerId: Int, targetPlayerId: Int) -> Unit,
    onSkipToFinalElimination: () -> Unit
    ) {
    val death = gameState.players.firstOrNull { it.card?.id == "reversed_death" }
    if(death == null){
        onSkipToFinalElimination()
        return
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "Devil Guess", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text(text = "Reversed Death must guess who the Straight Devil is.", style=MaterialTheme.typography.bodyLarge,textAlign=TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            gameState.players.sortedBy { it.position }.forEach {target ->
                OutlinedButton(
                    onClick = { onTargetSelected(death.id, target.id)},
                    modifier = Modifier.fillMaxWidth().padding(vertical=4.dp)
                ){ Text( target.displayName() )}
            }
        }
    }
}

private fun Player.displayName(): String {
    return name.ifBlank { "Player ${position + 1}" }
}
