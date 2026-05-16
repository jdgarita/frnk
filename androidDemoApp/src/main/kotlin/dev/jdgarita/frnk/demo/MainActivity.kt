package dev.jdgarita.frnk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.jdgarita.frnk.ui.theme.FrnkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrnkTheme(config = demoBlueThemeConfig()) {
                DemoScreen(onEffect = ::handleEffect)
            }
        }
    }

    private fun handleEffect(effect: DemoEffect) =
        when (effect) {
            is DemoEffect.Navigate -> Toast.makeText(this, "Navigate → ${effect.routeKey}", Toast.LENGTH_SHORT).show()
            is DemoEffect.Toast -> Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
        }
}
