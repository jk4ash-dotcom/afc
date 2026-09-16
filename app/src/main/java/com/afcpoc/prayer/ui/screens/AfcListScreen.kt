package com.afcpoc.prayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afcpoc.prayer.data.AfcPrayer
import com.afcpoc.prayer.data.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val categoryOrder = listOf("daily", "consecration", "optional", "after-rosary")
private val categoryLabels = mapOf(
    "daily" to "Daily prayers",
    "consecration" to "Consecration",
    "optional" to "Optional",
    "after-rosary" to "After the Rosary"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AfcListScreen(
    repository: ContentRepository,
    onBack: () -> Unit,
    onOpen: (String) -> Unit
) {
    var grouped by remember { mutableStateOf<Map<String, List<AfcPrayer>>?>(null) }

    LaunchedEffect(repository) {
        grouped = withContext(Dispatchers.Default) {
            if (!repository.isPreloaded()) repository.preload()
            repository.afcGrouped()
        }
    }

    val sections = remember(grouped) {
        val g = grouped ?: return@remember emptyList()
        categoryOrder.filter { g.containsKey(it) } +
            g.keys.filterNot { it in categoryOrder }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AFC Prayers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val g = grouped
        if (g == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            sections.forEach { category ->
                val items = g[category].orEmpty()
                item {
                    Text(
                        text = categoryLabels[category] ?: category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                items(items, key = { it.id }) { prayer: AfcPrayer ->
                    ListItem(
                        headlineContent = {
                            Text(prayer.title, style = MaterialTheme.typography.titleMedium)
                        },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(prayer.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
