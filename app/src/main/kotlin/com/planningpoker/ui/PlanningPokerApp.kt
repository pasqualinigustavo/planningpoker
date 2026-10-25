package com.planningpoker.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.planningpoker.navigation.PlanningPokerNavHost

@Composable
fun PlanningPokerApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        PlanningPokerNavHost()
    }
}
