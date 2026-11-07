package com.myceeee.idk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val Bg = Color(0xFF090D14)
private val CardBg = Color(0xFF121B27)
private val Accent = Color(0xFF55E6A5)
private val Muted = Color(0xFF8B9AAA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(
            primary = Accent, background = Bg, surface = CardBg
        )) { DemoScreen() } }
    }
}

@Composable
private fun DemoScreen() {
    var labels by remember { mutableStateOf(true) }
    var trails by remember { mutableStateOf(false) }
    var radar by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf(true) }
    val transition = rememberInfiniteTransition(label = "demo")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("IDK  /  VISUAL LAB", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Live demo", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("A self-contained animation playground. All objects are generated locally in this app.", color = Muted, fontSize = 13.sp)
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SIMULATION VIEW", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("RUNNING", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Canvas(Modifier.fillMaxWidth().height(300.dp).background(Color(0xFF070B10), RoundedCornerShape(12.dp))) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = size.minDimension * .31f
                    drawCircle(Color(0xFF1C3540), radius, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                    drawLine(Color(0x332B5660), Offset(cx, 0f), Offset(cx, size.height), 1.dp.toPx())
                    drawLine(Color(0x332B5660), Offset(0f, cy), Offset(size.width, cy), 1.dp.toPx())
                    repeat(6) { i ->
                        val a = phase + i * (2f * PI.toFloat() / 6f)
                        val x = cx + cos(a) * radius
                        val y = cy + sin(a * 1.4f) * radius * .72f
                        if (trails) drawLine(Accent.copy(alpha = .18f), Offset(cx, cy), Offset(x, y), 1.dp.toPx())
                        drawCircle(if (i % 2 == 0) Accent else Color(0xFF70A8FF), 7.dp.toPx(), Offset(x, y))
                        if (labels) drawCircle(Color.White.copy(alpha = .12f), 13.dp.toPx(), Offset(x, y), style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                    }
                    drawCircle(Accent, 4.dp.toPx(), Offset(cx, cy))
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("DISPLAY OPTIONS", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                DemoToggle("Object rings", labels) { labels = it }
                DemoToggle("Motion trails", trails) { trails = it }
                DemoToggle("Mini radar", radar) { radar = it }
                DemoToggle("Performance stats", stats) { stats = it }
            }
        }
        if (radar || stats) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (radar) MetricCard("OBJECTS", "06", Modifier.weight(1f))
            if (stats) MetricCard("RENDER", "60 FPS*", Modifier.weight(1f))
        }
        Text("*Illustrative demo indicator, not a measured device benchmark.", color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun DemoToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
