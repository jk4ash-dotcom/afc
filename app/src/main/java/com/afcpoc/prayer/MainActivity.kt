package com.afcpoc.prayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.afcpoc.prayer.navigation.AfcNavGraph
import com.afcpoc.prayer.ui.theme.AfcPrayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
