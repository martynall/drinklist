package com.example.drinklist.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.sin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SplashScreen(navigateToMain: () -> Unit) {
    val backgroundColors = listOf(
        Color(0xFF1A0B3D), // Deep purple
        Color(0xFF0F0F23), // Dark navy
        Color(0xFF000000)  // Black
    )

    val glassColor = Color.White
    val liquidColor = Color(0xFF00F5FF)
    val textColor = Color.White
    val accentColor1 = Color(0xFF00F5FF)
    val accentColor2 = Color(0xFFFF6B9D)
    val accentColor3 = Color(0xFF9B59B6)

    // Main animations
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    // Background bubble animations
    val bubble1Scale = remember { Animatable(0f) }
    val bubble2Scale = remember { Animatable(0f) }
    val bubble3Scale = remember { Animatable(0f) }

    // Shimmer effect
    val shimmerOffset = remember { Animatable(0f) }

    // Drink pouring animation
    val pourProgress = remember { Animatable(0f) }
    val bubbleAnimations = remember {
        List(6) { Animatable(0f) }
    }

    // Floating animation
    val floatingOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Start all animations (same as before)
        launch {
            // Background bubbles
            launch {
                delay(100)
                bubble1Scale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
            }
            launch {
                delay(200)
                bubble2Scale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
            }
            launch {
                delay(300)
                bubble3Scale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
            }

            // Main animations
            launch {
                scale.animateTo(1.1f, tween(600, easing = FastOutSlowInEasing))
                scale.animateTo(1f, tween(200, easing = LinearEasing))
            }
            launch { alpha.animateTo(1f, tween(400)) }

            launch {
                delay(200)
                rotation.animateTo(5f, tween(300, easing = FastOutSlowInEasing))
                rotation.animateTo(-5f, tween(300, easing = FastOutSlowInEasing))
                rotation.animateTo(4f, tween(300, easing = FastOutSlowInEasing))
                rotation.animateTo(-4f, tween(300, easing = FastOutSlowInEasing))
                rotation.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
            }
            launch {
                delay(400)
                textAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            }
            launch {
                delay(600)
                while (true) {
                    shimmerOffset.animateTo(1f, tween(1500, easing = LinearEasing))
                    shimmerOffset.snapTo(0f)
                }
            }

            // Bubble animations
            bubbleAnimations.forEachIndexed { index, bubble ->
                launch {
                    delay(1000 + (index * 150L))
                    while (true) {
                        bubble.animateTo(1f, tween(2500, easing = LinearEasing))
                        bubble.snapTo(0f)
                        delay(500)
                    }
                }
            }

            // Floating animation
            launch {
                delay(800)
                while (true) {
                    floatingOffset.animateTo(6f, tween(2000, easing = FastOutSlowInEasing))
                    floatingOffset.animateTo(-6f, tween(2000, easing = FastOutSlowInEasing))
                }
            }
        }

        delay(4000)
        navigateToMain()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = backgroundColors,
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative bubbles
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-120).dp)
                .size(60.dp)
                .scale(bubble1Scale.value)
                .alpha(0.4f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor1.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = (-80).dp)
                .size(40.dp)
                .scale(bubble2Scale.value)
                .alpha(0.5f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor2.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .offset(x = (-120).dp, y = 100.dp)
                .size(35.dp)
                .scale(bubble3Scale.value)
                .alpha(0.4f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor3.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        )

        // Main vertical content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = floatingOffset.value.dp)
        ) {
            // Main icon container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .rotate(rotation.value)
                    .graphicsLayer {
                        shadowElevation = 20.dp.toPx()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentColor1.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Main icon background with gradient
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667EEA),
                                    Color(0xFF764BA2),
                                    Color(0xFFF093FB)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom martini glass
                    Canvas(
                        modifier = Modifier.size(70.dp)
                    ) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val glassRadius = size.width * 0.35f
                        val stemHeight = size.height * 0.3f
                        val stemWidth = size.width * 0.06f
                        val baseWidth = size.width * 0.3f

                        // Martini glass bowl
                        val glassPath = Path().apply {
                            moveTo(centerX - glassRadius, centerY - glassRadius * 0.2f)
                            lineTo(centerX + glassRadius, centerY - glassRadius * 0.2f)
                            lineTo(centerX, centerY + glassRadius * 0.5f)
                            lineTo(centerX - glassRadius, centerY - glassRadius * 0.2f)
                            close()
                        }

                        drawPath(glassPath, glassColor)
                        drawPath(glassPath, glassColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                        // Draw stem
                        drawLine(
                            color = glassColor,
                            start = Offset(centerX, centerY + glassRadius * 0.5f),
                            end = Offset(centerX, centerY + glassRadius * 0.5f + stemHeight),
                            strokeWidth = stemWidth,
                            cap = StrokeCap.Round
                        )

                        // Draw base
                        drawLine(
                            color = glassColor,
                            start = Offset(centerX - baseWidth / 2, centerY + glassRadius * 0.5f + stemHeight),
                            end = Offset(centerX + baseWidth / 2, centerY + glassRadius * 0.5f + stemHeight),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App title with shimmer
            Text(
                text = "DrinkBase",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .graphicsLayer {
                        val shimmer = sin(shimmerOffset.value * Math.PI * 2).toFloat()
                        this.alpha = textAlpha.value * (0.7f + (shimmer * 0.3f).coerceAtLeast(0f))
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Discover Amazing Cocktails",
                fontSize = 16.sp,
                color = textColor.copy(alpha = 0.8f),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            bubbleAnimations.forEachIndexed { index, bubble ->
                val progress = bubble.value
                if (progress > 0f) {
                    // Calculate positions relative to screen center
                    val startY = (-70).dp // Adjust this to match your glass position
                    val endY = startY - 150.dp
                    val currentY = startY + (endY - startY) * progress
                    val sideMovement = (sin(progress * Math.PI * 4) * 25).dp
                    val horizontalSpread = (index - 2.5f) * 30.dp

                    Box(
                        modifier = Modifier
                            .offset(
                                x = horizontalSpread + sideMovement,
                                y = currentY + floatingOffset.value.dp
                            )
                            .size((8 + sin(progress * Math.PI * 2) * 4).dp)
                            .alpha((1f - progress * 0.5f))
                            .zIndex(1f) // Ensure bubbles appear above everything
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        liquidColor.copy(alpha = 0.9f),
                                        liquidColor.copy(alpha = 0.5f),
                                        Color.Transparent
                                    ),
                                    center = Offset(0.3f, 0.3f)
                                )
                            )
                    )
                }
            }
        }

        // Loading indicator dots at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(textAlpha.value),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                val dotAlpha = remember { Animatable(0.3f) }
                LaunchedEffect(Unit) {
                    delay(800 + (index * 200L))
                    while (true) {
                        dotAlpha.animateTo(1f, tween(400))
                        dotAlpha.animateTo(0.3f, tween(400))
                        delay(600)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(dotAlpha.value)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.8f))
                )
            }
        }
    }
}

// Single preview with dark theme
@Preview(name = "Splash Screen", showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(navigateToMain = {})
}

