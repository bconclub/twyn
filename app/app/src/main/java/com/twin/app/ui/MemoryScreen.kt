package com.twin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Bg = Color(0xFF111111)
private val Surface2 = Color(0xFF1D1D1F)
private val Accent = Color(0xFF7C6CFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    onClose: () -> Unit,
    vm: MemoryViewModel = viewModel(),
) {
    val files by vm.files.collectAsState()
    val path by vm.path.collectAsState()
    val draft by vm.draft.collectAsState()
    val busy by vm.busy.collectAsState()
    val status by vm.status.collectAsState()
    var showNew by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Bg)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    if (path != null) vm.closeEditor() else onClose()
                }) { Text(if (path != null) "Files" else "Back", color = Color.Gray) }
                Text(
                    if (path != null) path!! else "Memory",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                if (path == null) {
                    TextButton(onClick = { showNew = true }, enabled = !busy) {
                        Text("New", color = Accent)
                    }
                } else {
                    TextButton(onClick = { confirmDelete = true }, enabled = !busy) {
                        Text("Delete", color = Color(0xFFE5484D))
                    }
                    TextButton(onClick = { vm.save() }, enabled = !busy) {
                        Text("Save", color = Accent)
                    }
                }
            }

            if (status != null) {
                Text(
                    status!!,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }

            if (path == null) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (files.isEmpty() && !busy) {
                        item {
                            Text(
                                "Markdown on disk. Edit yourself — Claude never sees this screen.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 24.dp),
                            )
                        }
                    }
                    items(files, key = { it.path }) { f ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface2)
                                .clickable(enabled = !busy) { vm.open(f.path) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Text(f.path, color = Color.White, fontSize = 15.sp)
                            Text("${f.bytes} B", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = draft,
                    onValueChange = vm::setDraft,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 20.sp,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Surface2,
                        unfocusedContainerColor = Surface2,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )
            }
        }
    }

    if (showNew) {
        var name by remember { mutableStateOf("projects/note.md") }
        AlertDialog(
            onDismissRequest = { showNew = false },
            title = { Text("New memory file") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("path.md") },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showNew = false
                    vm.create(name)
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNew = false }) { Text("Cancel") }
            },
        )
    }

    if (confirmDelete && path != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete $path?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteCurrent()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}
