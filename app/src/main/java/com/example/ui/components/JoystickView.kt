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
            val innerRadius = outerRadius * 0.45f

            // Outer ring: Matte dark background with a subtle Xbox Green outer highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1510B981), Color(0x0210B981)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            // Outer bezel
            drawCircle(
                color = Color(0xFF27272A),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            // Thin inner guide line
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.4f),
                radius = outerRadius - 3.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Inner thumb stick (Physical Xbox thumbstick cap representation)
            val thumbCenter = center + thumbOffset
            
            // Base cap: Charcoal gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3F3F46), Color(0xFF18181B)),
                    center = thumbCenter,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = thumbCenter
            )
            
            // The textured grip ring (concentric ridge on Xbox sticks)
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.8f), // Xbox green accent ridge
                radius = innerRadius * 0.75f,
                center = thumbCenter,
                style = Stroke(width = 1.5.dp.toPx())
            )
            
            // Subtle 4-way cardinal directional ridges on the thumbstick cap (classic Xbox controller design)
            val ridgeLength = innerRadius * 0.2f
            val ridgeInner = innerRadius * 0.4f
            val ridgeOuter = innerRadius * 0.8f
            
            // Up ridge
            drawLine(
                color = Color(0xFF71717A),
                start = Offset(thumbCenter.x, thumbCenter.y - ridgeInner),
                end = Offset(thumbCenter.x, thumbCenter.y - ridgeOuter),
                strokeWidth = 2.dp.toPx()
            )
            // Down ridge
            drawLine(
                color = Color(0xFF71717A),
                start = Offset(thumbCenter.x, thumbCenter.y + ridgeInner),
                end = Offset(thumbCenter.x, thumbCenter.y + ridgeOuter),
                strokeWidth = 2.dp.toPx()
            )
            // Left ridge
            drawLine(
                color = Color(0xFF71717A),
                start = Offset(thumbCenter.x - ridgeInner, thumbCenter.y),
                end = Offset(thumbCenter.x - ridgeOuter, thumbCenter.y),
                strokeWidth = 2.dp.toPx()
            )
            // Right ridge
            drawLine(
                color = Color(0xFF71717A),
                start = Offset(thumbCenter.x + ridgeInner, thumbCenter.y),
                end = Offset(thumbCenter.x + ridgeOuter, thumbCenter.y),
                strokeWidth = 2.dp.toPx()
            )

            // Center core indentation
            drawCircle(
                color = Color(0xFF18181B),
                radius = innerRadius * 0.3f,
                center = thumbCenter
            )
        }
    }
}
