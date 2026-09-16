package com.afcpoc.prayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.afcpoc.prayer.data.ContentRepository
import com.afcpoc.prayer.data.Mystery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RosaryHubScreen(
    repository: ContentRepository,
    onBack: () -> Unit,
    onStart: (setName: String, includeAfter: Boolean) -> Unit
) {
    var today by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<String?>(null) }
    var includeAfter by remember { mutableStateOf(true) }
    var sets by remember { mutableStateOf<List<String>>(emptyList()) }
    var dayNote by remember { mutableStateOf<String?>(null) }
    var mysteriesBySet by remember { mutableStateOf<Map<String, List<Mystery>>>(emptyMap()) }

    LaunchedEffect(repository) {
        data class HubData(
            val today: String,
            val sets: List<String>,
            val dayNote: String?,
            val mysteriesBySet: Map<String, List<Mystery>>
        )
        val snapshot = withContext(Dispatchers.Default) {
            if (!repository.isPreloaded()) repository.preload()
            val t = repository.todayMysterySetName()
            val names = repository.mysterySetNames()
            val note = repository.mysteryCalendarNote()
            val map = names.associateWith { name ->
                repository.rosary.mysterySets[name]?.mysteries.orEmpty()
            }
            HubData(t, names, note, map)
        }
        today = snapshot.today
        selected = snapshot.today
        sets = snapshot.sets
        dayNote = snapshot.dayNote
        mysteriesBySet = snapshot.mysteriesBySet
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Holy Rosary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val todayReady = today
        val selectedReady = selected
        if (todayReady == null || selectedReady == null) {
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
        val mysteries = mysteriesBySet[selectedReady].orEmpty()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "Today’s mysteries: $todayReady",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            dayNote?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("Choose mystery set", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sets.forEach { name ->
                    FilterChip(
                        selected = selectedReady == name,
                        onClick = { selected = name },
                        label = { Text(name) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("$selectedReady Mysteries", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    mysteries.forEachIndexed { i, m ->
                        Text(
                            "${i + 1}. ${m.name}" + (m.fruit?.let { " — $it" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includeAfter, onCheckedChange = { includeAfter = it })
                Text(
                    "Include optional AFC after-Rosary set",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onStart(selectedReady, includeAfter) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Begin guided Rosary")
            }
        }
    }
}
