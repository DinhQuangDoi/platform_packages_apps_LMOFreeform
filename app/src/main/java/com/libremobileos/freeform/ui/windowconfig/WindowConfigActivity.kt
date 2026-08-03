package com.libremobileos.freeform.ui.windowconfig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple UI to add/edit/delete per-app freeform window rules.
 */
class WindowConfigActivity : ComponentActivity() {
    private val viewModel: WindowConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WindowConfigScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WindowConfigScreen(viewModel: WindowConfigViewModel) {
    val configs by viewModel.configs.collectAsState()
    val apps by viewModel.apps.collectAsState()
    var editingPackage by remember { mutableStateOf<String?>(null) }
    var pickingPackage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Freeform per-app window") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Button(
                    onClick = { pickingPackage = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Add rule")
                }
            }
            if (configs.isEmpty()) {
                item {
                    Text(
                        text = "No per-app rules yet",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(configs.keys.sorted()) { packageName ->
                    val config = configs[packageName] ?: return@items
                    ListItem(
                        headlineText = { Text(packageName) },
                        supportingText = {
                            Text(
                                "${config.width}x${config.height} at " +
                                    "(${config.x},${config.y})" +
                                    if (config.forceResizeable) " - force resizeable" else ""
                            )
                        },
                        trailingContent = {
                            Row {
                                Button(onClick = { editingPackage = packageName }) {
                                    Text("Edit")
                                }
                                Spacer(Modifier.width(8.dp))
                                TextButton(onClick = { viewModel.removeConfig(packageName) }) {
                                    Text("Delete")
                                }
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }

    if (pickingPackage) {
        AppPickerDialog(
            apps = apps,
            onDismiss = { pickingPackage = false },
            onPick = { picked ->
                pickingPackage = false
                editingPackage = picked
            }
        )
    }

    editingPackage?.let { packageName ->
        val config = configs[packageName]
        if (config != null) {
            ConfigEditorDialog(
                packageName = packageName,
                initial = config,
                onDismiss = { editingPackage = null },
                onSave = { updated ->
                    viewModel.saveConfig(packageName, updated)
                    editingPackage = null
                }
            )
        }
    }
}

@Composable
fun AppPickerDialog(
    apps: List<AppItem>,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick an app") },
        text = {
            if (apps.isEmpty()) {
                Text("No launcher apps found")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(apps) { app ->
                        Text(
                            text = app.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(app.packageName) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigEditorDialog(
    packageName: String,
    initial: WindowConfig,
    onDismiss: () -> Unit,
    onSave: (WindowConfig) -> Unit
) {
    var width by remember { mutableStateOf(initial.width.toString()) }
    var height by remember { mutableStateOf(initial.height.toString()) }
    var x by remember { mutableStateOf(initial.x.toString()) }
    var y by remember { mutableStateOf(initial.y.toString()) }
    var forceResizeable by remember { mutableStateOf(initial.forceResizeable) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(packageName)
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text("Width") }
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = x,
                        onValueChange = { x = it },
                        label = { Text("X") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = y,
                        onValueChange = { y = it },
                        label = { Text("Y") },
                        modifier = Modifier.weight(1f)
                    )
                }
                ListItem(
                    headlineText = { Text("Force resizeable") },
                    supportingText = { Text("Prevent letterboxing and activity reload") },
                    trailingContent = {
                        Switch(
                            checked = forceResizeable,
                            onCheckedChange = { forceResizeable = it }
                        )
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        WindowConfig(
                            width = width.toIntOrNull() ?: initial.width,
                            height = height.toIntOrNull() ?: initial.height,
                            x = x.toIntOrNull() ?: 0,
                            y = y.toIntOrNull() ?: 0,
                            forceResizeable = forceResizeable
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
