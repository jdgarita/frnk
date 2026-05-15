package dev.jdgarita.frnk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.jdgarita.frnk.ui.atoms.ProvideToolkitTheme
import dev.jdgarita.frnk.ui.atoms.ToolkitColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideToolkitTheme(
                // Demonstrate host-side override of default tokens.
                colors =
                    ToolkitColors(
                        primary =
                            androidx.compose.ui.graphics
                                .Color(0xFF0A84FF),
                        onPrimary = androidx.compose.ui.graphics.Color.White,
                    ),
            ) {
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
