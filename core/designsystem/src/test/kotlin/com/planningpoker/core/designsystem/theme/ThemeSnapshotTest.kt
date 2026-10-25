package com.planningpoker.core.designsystem.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class ThemeSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun lightTheme() {
        paparazzi.snapshot {
            PlanningPokerTheme(darkTheme = false, dynamicColor = false) { ThemeSample() }
        }
    }

    @Test
    fun darkTheme() {
        paparazzi.snapshot {
            PlanningPokerTheme(darkTheme = true, dynamicColor = false) { ThemeSample() }
        }
    }
}

@Composable
private fun ThemeSample() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Planning Poker", style = MaterialTheme.typography.headlineMedium)
            Text("Body text on surface", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = {}) { Text("Primary action") }
            OutlinedButton(onClick = {}) { Text("Secondary action") }
            Card { Text("Card content", modifier = Modifier.padding(16.dp)) }
        }
    }
}
