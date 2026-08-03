package com.libremobileos.sidebar.ui.sidebar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.libremobileos.sidebar.ui.theme.SidebarTheme

class SidebarSettingsActivity : ComponentActivity() {
    private val viewModel: SidebarSettingsViewModel by viewModels { SidebarSettingsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SidebarTheme {
                SidebarSettingsPage(viewModel = viewModel)
            }
        }
    }
}
