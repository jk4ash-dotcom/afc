package com.afcpoc.prayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.afcpoc.prayer.navigation.AfcNavGraph
import com.afcpoc.prayer.ui.theme.AfcPrayerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { AfcApp.from(this@MainActivity).repository.preload() }
        }
        enableEdgeToEdge()
        setContent {
            AfcPrayerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AfcNavGraph()
                }
            }
        }
    }
}
