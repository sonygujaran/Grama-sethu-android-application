package com.gramasethu.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * SplashScreen shows when app first opens.
 * It has a beautiful animation then moves to login/map.
 */
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    // Animation for the bridge emoji — it pulses!
    val scale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(true) {
        // Scale up the emoji
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Fade in the text
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        // Wait 2 seconds then go to next screen
        delay(2000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32),
                        Color(0xFF121212)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated bridge emoji
            Text(
                text = "🌉",
                fontSize = 100.sp,
                modifier = Modifier.scale(scale.value)
            )

            // App name with fade in
            Text(
                text = "Grama-Sethu",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = textAlpha.value)
            )

            Text(
                text = "Rural Bridge Monitor",
                fontSize = 18.sp,
                color = Color(0xFF81C784).copy(alpha = textAlpha.value)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Loading dots animation
            if (textAlpha.value > 0.5f) {
                LoadingDots()
            }
        }

        // Version text at bottom
        Text(
            text = "Karnataka • India 🇮🇳",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}

@Composable
fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        Color(0xFF4CAF50).copy(alpha = alpha),
                        shape = androidx.compose.foundation.shape
                            .CircleShape
                    )
            )
        }
    }
}