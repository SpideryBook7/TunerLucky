package com.spiderybook.tunerlucky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spiderybook.tunerlucky.shizuku.ShizukuManager
import com.spiderybook.tunerlucky.ui.theme.TunerLuckyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShizukuManager.init()

        setContent {
            TunerLuckyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isReady by ShizukuManager.isReady.collectAsState()
                    val hasPermission by ShizukuManager.hasPermission.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "LUCKY TUNER",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        if (!isReady) {
                            Text("Shizuku no está corriendo.", color = MaterialTheme.colorScheme.error)
                        } else if (!hasPermission) {
                            Text("Esperando permisos de Shizuku...", color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { ShizukuManager.checkPermission() }) {
                                Text("Solicitar Permiso")
                            }
                        } else {
                            Text("¡Shizuku Conectado!", color = MaterialTheme.colorScheme.primary)
                            // Dashboard will go here
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ShizukuManager.destroy()
    }
}
