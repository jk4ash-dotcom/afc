package com.afcpoc.prayer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.afcpoc.prayer.data.ContentRepository
import com.afcpoc.prayer.ui.components.GuidedFlowScaffold

@Composable
fun DivineMercyChapletScreen(
    includeOpen: Boolean,
    includeClose: Boolean,
    repository: ContentRepository,
    onBack: () -> Unit
) {
    val steps = remember(includeOpen, includeClose) {
        repository.buildChapletSteps(includeOpen, includeClose)
    }
    var index by remember(includeOpen, includeClose) { mutableIntStateOf(0) }

    GuidedFlowScaffold(
        screenTitle = "Divine Mercy Chaplet",
        steps = steps,
        index = index,
        onIndexChange = { index = it },
        onBack = onBack
    )
}
