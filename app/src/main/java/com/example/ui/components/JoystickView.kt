package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun JoystickView(
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    sensitivity: Float = 1.0f,
    deadzone: Float = 0.15f,
    onMove: (x: Float, y: Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                val maxRadius = size.toPx() / 2f
                val thumbMaxRadius = maxRadius * 0.75f

                detectDragGestures(
                    onDragStart = { },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        
                        // Accumulate drag offsets
                        val rawOffset = thumbOffset + dragAmount
                        val distance = sqrt(rawOffset.x * rawOffset.x + rawOffset.y * rawOffset.y)
                        
                        thumbOffset = if (distance <= thumbMaxRadius) {
                            rawOffset
                        } else {
                            Offset(
                                x = (rawOffset.x / distance) * thumbMaxRadius,
                                y = (rawOffset.y / distance) * thumbMaxRadius
                            )
                        }

                        // Calculate normalized values (-1f to 1f)
                        var normX = (thumbOffset.x / thumbMaxRadius) * sensitivity
                        var normY = (thumbOffset.y / thumbMaxRadius) * sensitivity

                        normX = normX.coerceIn(-1f, 1f)
                        normY = normY.coerceIn(-1f, 1f)

                        // Apply deadzone check
                        val normDistance = sqrt(normX * normX + normY * normY)
                        if (normDistance < deadzone) {
                            onMove(0f, 0f)
                        } else {
                            onMove(normX, normY)
                        }
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val outerRadius = size.toPx() / 2f
            val innerRadius = outerRadius * 0.35f

            // Outer ring with cybernetic glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3300F0FF), Color(0x0500F0FF)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF00F0FF),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color(0x3300F0FF),
                radius = outerRadius - 4.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Inner thumb stick (glowing orb)
            val thumbCenter = center + thumbOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8A2BE2), Color(0xFF4B0082)),
                    center = thumbCenter,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = thumbCenter
            )
            drawCircle(
                color = Color(0xFFC084FC),
                radius = innerRadius,
                center = thumbCenter,
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Center core
            drawCircle(
                color = Color.White,
                radius = innerRadius * 0.25f,
                center = thumbCenter
            )
        }
    }
}
