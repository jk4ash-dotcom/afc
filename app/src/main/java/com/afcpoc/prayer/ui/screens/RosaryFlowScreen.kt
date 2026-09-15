package com.afcpoc.prayer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.afcpoc.prayer.data.ContentRepository
import com.afcpoc.prayer.ui.components.GuidedFlowScaffold

@Composable
fun RosaryFlowScreen(
    setName: String,
    includeAfter: Boolean,
    repository: ContentRepository,
    onBack: () -> Unit
) {
    val steps = remember(setName, includeAfter) {
        repository.buildRosarySteps(setName, includeAfter)
    }
    var index by remember(setName, includeAfter) { mutableIntStateOf(0) }

    GuidedFlowScaffold(
        screenTitle = "Rosary — $setName",
        steps = steps,
        index = index,
        onIndexChange = { index = it },
        onBack = onBack
    )
}
