package com.afcpoc.prayer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.afcpoc.prayer.data.ContentRepository
import com.afcpoc.prayer.data.GuidedStep
import com.afcpoc.prayer.ui.components.GuidedFlowScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RosaryFlowScreen(
    setName: String,
    includeAfter: Boolean,
    repository: ContentRepository,
    onBack: () -> Unit
) {
    var steps by remember(setName, includeAfter) { mutableStateOf<List<GuidedStep>?>(null) }
    var index by rememberSaveable(setName, includeAfter) { mutableIntStateOf(0) }

    LaunchedEffect(setName, includeAfter) {
        steps = withContext(Dispatchers.Default) {
            repository.cachedRosarySteps(setName, includeAfter)
        }
    }

    val ready = steps
    if (ready == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    GuidedFlowScaffold(
        screenTitle = "Rosary — $setName",
        steps = ready,
        index = index.coerceIn(0, (ready.size - 1).coerceAtLeast(0)),
        onIndexChange = { index = it },
        onBack = onBack
    )
}
