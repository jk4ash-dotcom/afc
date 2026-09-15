package com.afcpoc.prayer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afcpoc.prayer.data.ContentRepository
import com.afcpoc.prayer.ui.components.PrayerBodyText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourOfGreatMercyScreen(
    repository: ContentRepository,
    onBack: () -> Unit
) {
    val hour = repository.divineMercy.hourOfGreatMercy

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hour of Great Mercy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(hour.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            hour.diaryQuote?.let { quote ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        quote,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
            hour.prayers.forEach { prayer ->
                Text(
                    prayer.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                prayer.text?.let { PrayerBodyText(it) }
                prayer.texts?.forEach { t ->
                    PrayerBodyText("• $t")
                }
                prayer.repeat?.let {
                    Text(
                        "Often repeated $it times",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
            hour.notes?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            hour.sourceUrl?.let { url ->
                Spacer(Modifier.height(16.dp))
                Text(
                    "Source: $url",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
