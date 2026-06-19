package eu.mermali.tarot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import eu.mermali.tarot.ui.TarotApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)
        setContent {
            TarotApp()
        }
    }
}