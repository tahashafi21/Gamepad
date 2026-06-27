package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun DpadView(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    onDirectionChange: (direction: Byte) -> Unit // 0=Up, 1=Down, 2=Left, 3=Right, 4=UL, 5=UR, 6=DL, 7=DR, 8=Neutral
) {
    var activeDirection by remember { mutableStateOf<Byte>(8) } // 8 is Neutral

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                val center = size.toPx() / 2f
                val maxRadius = size.toPx() / 2f
                val minRadius = maxRadius * 0.25f // Deadzone center

                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        
                        if (change != null && change.pressed) {
                            change.consume()
                            val pos = change.position
                            val dx = pos.x - center
                            val dy = pos.y - center
                            val distance = sqrt(dx * dx + dy * dy)

                            if (distance in minRadius..maxRadius) {
                                // Calculate angle in degrees [-180, 180]
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f // Normalize to [0, 360]

                                // Determine 8-way sector:
                                // Up: 225 to 315
                                // Down: 45 to 135
                                // Left: 135 to 225
                                // Right: 315 to 45 (wrap)
                                val dir: Byte = when {
                                    angle in 337.5f..360f || angle in 0f..22.5f -> 3  // Right
                                    angle in 22.5f..67.5f -> 7                      // Down-Right
                                    angle in 67.5f..112.5f -> 1                     // Down
                                    angle in 112.5f..157.5f -> 6                    // Down-Left
                                    angle in 157.5f..202.5f -> 2                    // Left
                                    angle in 202.5f..247.5f -> 4                    // Up-Left
                                    angle in 247.5f..292.5f -> 0                    // Up
                                    angle in 292.5f..337.5f -> 5                    // Up-Right
                                    else -> 8
                                }
                                if (activeDirection != dir) {
                                    activeDirection = dir
                                    onDirectionChange(dir)
                                }
                            } else {
                                if (activeDirection != 8.toByte()) {
                                    activeDirection = 8.toByte()
                                    onDirectionChange(8.toByte())
                                }
                            }
                        } else {
                            if (activeDirection != 8.toByte()) {
                                activeDirection = 8.toByte()
                                onDirectionChange(8.toByte())
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val outerRadius = size.toPx() / 2f
            val innerRadius = outerRadius * 0.35f

            // Background Circle Disc
            drawCircle(
                color = Color(0xFF27272A),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.3f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Dynamic sector highlight paths
            val upActive = activeDirection == 0.toByte() || activeDirection == 4.toByte() || activeDirection == 5.toByte()
            val downActive = activeDirection == 1.toByte() || activeDirection == 6.toByte() || activeDirection == 7.toByte()
            val leftActive = activeDirection == 2.toByte() || activeDirection == 4.toByte() || activeDirection == 6.toByte()
            val rightActive = activeDirection == 3.toByte() || activeDirection == 5.toByte() || activeDirection == 7.toByte()

            // Drawing Cross Arrows
            val w = outerRadius * 0.3f
            val h = outerRadius * 0.8f

            // UP Arrow Segment
            drawPath(
                path = Path().apply {
                    moveTo(center.x - w, center.y - innerRadius)
                    lineTo(center.x + w, center.y - innerRadius)
                    lineTo(center.x, center.y - h)
                    close()
                },
                color = if (upActive) Color(0xFF10B981) else Color(0xFF18181B)
            )

            // DOWN Arrow Segment
            drawPath(
                path = Path().apply {
                    moveTo(center.x - w, center.y + innerRadius)
                    lineTo(center.x + w, center.y + innerRadius)
                    lineTo(center.x, center.y + h)
                    close()
                },
                color = if (downActive) Color(0xFF10B981) else Color(0xFF18181B)
            )

            // LEFT Arrow Segment
            drawPath(
                path = Path().apply {
                    moveTo(center.x - innerRadius, center.y - w)
                    lineTo(center.x - innerRadius, center.y + w)
                    lineTo(center.x - h, center.y)
                    close()
                },
                color = if (leftActive) Color(0xFF10B981) else Color(0xFF18181B)
            )

            // RIGHT Arrow Segment
            drawPath(
                path = Path().apply {
                    moveTo(center.x + innerRadius, center.y - w)
                    lineTo(center.x + innerRadius, center.y + w)
                    lineTo(center.x + h, center.y)
                    close()
                },
                color = if (rightActive) Color(0xFF10B981) else Color(0xFF18181B)
            )

            // Center Disc Core
            drawCircle(
                color = Color(0xFF1E1E21),
                radius = innerRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF3F3F46),
                radius = innerRadius * 0.7f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
