package com.libremobileos.sidebar.ui.sidebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.libremobileos.sidebar.R
import com.libremobileos.sidebar.bean.SidebarAppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarSettingsPage(
    viewModel: SidebarSettingsViewModel
) {
    var mainChecked = rememberSaveable { mutableStateOf(viewModel.getSidebarEnabled()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sidebar_label)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.enable_sideline)) },
                trailingContent = {
                    Switch(
                        checked = mainChecked.value,
                        onCheckedChange = {
                            mainChecked.value = it
                            viewModel.setSidebarEnabled(it)
                        },
                        enabled = viewModel.isEnabled
                    )
                }
            )

            if (mainChecked.value) {
                DividerLine()

                var predictedChecked = rememberSaveable {
                    mutableStateOf(viewModel.getPredictedAppsEnabled())
                }
                ListItem(
                    headlineContent = { Text(stringResource(R.string.sidebar_predicted_apps)) },
                    supportingContent = {
                        Text(stringResource(R.string.sidebar_predicted_apps_summary))
                    },
                    trailingContent = {
                        Switch(
                            checked = predictedChecked.value,
                            onCheckedChange = {
                                predictedChecked.value = it
                                viewModel.setPredictedAppsEnabled(it)
                            }
                        )
                    }
                )

                DividerLine()
                SidebarAppList(viewModel)
            }
        }
    }
}

@Composable
private fun DividerLine() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(color = MaterialTheme.colorScheme.outlineVariant)
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarAppList(
    viewModel: SidebarSettingsViewModel
) {
    val sidebarApps by viewModel.appListFlow.collectAsState()

    Text(
        text = stringResource(R.string.sidebar_app_setting_label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )

    LazyColumn {
        items(sidebarApps) { appInfo ->
            SidebarAppListItem(
                appInfo = appInfo,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        viewModel.addSidebarApp(appInfo)
                    } else {
                        viewModel.deleteSidebarApp(appInfo)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarAppListItem(
    appInfo: SidebarAppInfo,
    onCheckedChange: (Boolean) -> Unit
) {
    var appChecked = rememberSaveable { mutableStateOf(appInfo.isSidebarApp) }
    ListItem(
        headlineContent = { Text(appInfo.label) },
        leadingContent = {
            Image(
                painter = rememberDrawablePainter(appInfo.icon),
                contentDescription = appInfo.label,
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            Switch(
                checked = appChecked.value,
                onCheckedChange = {
                    appChecked.value = it
                    onCheckedChange(it)
                }
            )
        }
    )
}
