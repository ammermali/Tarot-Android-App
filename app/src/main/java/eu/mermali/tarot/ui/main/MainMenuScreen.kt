package eu.mermali.tarot.ui.main
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(onPlay: () -> Unit, onOpenLogs: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MainMenuBackground, contentColor = MainMenuText) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainMenuPrimary, contentColor = Color.White)
            ) { Text("Play") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onOpenLogs,
                modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MainMenuPrimary),
                border = BorderStroke(1.dp, MainMenuPrimary)
            ) { Text("Game History") }
        }
    }
}

private val MainMenuBackground = Color(0xFFF7F3F8)
private val MainMenuPrimary = Color(0xFF3F5E9A)
private val MainMenuText = Color(0xFF1C1B1F)
