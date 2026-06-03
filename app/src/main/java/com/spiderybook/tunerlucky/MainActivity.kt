package com.spiderybook.tunerlucky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.spiderybook.tunerlucky.shizuku.ShizukuManager
import com.spiderybook.tunerlucky.ui.GameSpaceScreen
import com.spiderybook.tunerlucky.ui.theme.TunerLuckyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        ShizukuManager.init()

        setContent {

            TunerLuckyTheme {

                GameSpaceScreen()
            }
        }
    }

    override fun onDestroy() {

        ShizukuManager.destroy()

        super.onDestroy()
    }
}