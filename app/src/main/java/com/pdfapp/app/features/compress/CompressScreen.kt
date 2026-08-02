package com.pdfapp.app.features.compress

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

@Composable
fun CompressScreen(
    onNavigateBack: () -> Unit,
    viewModel: CompressViewModel = hiltViewModel()
) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var quality by remember { mutableFloatStateOf(0.7f) }
    var targetSize by remember { mutableStateOf<Long?>(null) }
    val isProcessing by viewModel.isProcessing.collectAsState()
    val result by viewModel.result.collectAsState()
    val isPro by viewModel.isPro.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Convert URI to File in production
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compress PDF") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // File Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { filePicker.launch("application/pdf") }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedFile != null) {
                        Text(text = selectedFile!!.name)
                    } else {
                        Text(text = "Select PDF File")
                    }
                }
            }

            // Quality Slider (Pro Feature)
            if (isPro) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Quality: ${(quality * 100).toInt()}%")
                        Slider(
                            value = quality,
                            onValueChange = { quality = it },
                            valueRange = 0.3f..1.0f
                        )
                    }
                }
            }

            // Target Size (Pro Feature)
            if (isPro) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Target Size (KB) - Optional")
                        OutlinedTextField(
                            value = targetSize?.toString() ?: "",
                            onValueChange = { 
                                targetSize = it.toLongOrNull()
                            },
                            placeholder = { Text("e.g., 500") }
                        )
                    }
                }
            }

            // Free User Info
            if (!isPro) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.warningContainer
                    )
                ) {
                    Text(
                        text = "⚠️ Free version includes watermark",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Compress Button
            Button(
                onClick = { /* Process compression */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedFile != null && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Compress PDF")
                }
            }

            // Result
            result?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Compression complete! Saved to: $it",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
