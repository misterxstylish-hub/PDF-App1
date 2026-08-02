package com.pdfapp.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pdfapp.app.ui.theme.ProGold
import com.pdfapp.app.ui.theme.PrimaryBlue

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val isPro by viewModel.isPro.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Tools Pro") },
                actions = {
                    IconButton(onClick = { /* Navigate to settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    if (!isPro) {
                        IconButton(onClick = { /* Navigate to subscription */ }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Go Pro",
                                tint = ProGold
                            )
                        }
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
            // Pro Banner (if not pro)
            if (!isPro) {
                ProBannerCard(onClick = { /* Navigate to subscription */ })
            }
            
            // Tool Grid
            ToolGrid(isPro = isPro)
        }
    }
}

@Composable
fun ProBannerCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upgrade to Pro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Remove watermarks & unlock all features",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            FilledTonalButton(onClick = onClick) {
                Text("Upgrade")
            }
        }
    }
}

@Composable
fun ToolGrid(isPro: Boolean) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToolCard(
                icon = Icons.Default.SwipeVertical,
                title = "Compress PDF",
                description = "Reduce file size",
                isProFeature = false,
                onClick = { /* Navigate to compress */ }
            )
        }
        
        item {
            ToolCard(
                icon = Icons.Default.Merge,
                title = "Merge PDFs",
                description = "Combine multiple files",
                isProFeature = true,
                isPro = isPro,
                onClick = { /* Navigate to merge */ }
            )
        }
        
        item {
            ToolCard(
                icon = Icons.Default.Image,
                title = "PDF ↔ JPG",
                description = "Convert to/from images",
                isProFeature = true,
                isPro = isPro,
                onClick = { /* Navigate to convert */ }
            )
        }
        
        item {
            ToolCard(
                icon = Icons.Default.RotateRight,
                title = "Rotate PDF",
                description = "Rotate pages",
                isProFeature = true,
                isPro = isPro,
                onClick = { /* Navigate to rotate */ }
            )
        }
        
        item {
            ToolCard(
                icon = Icons.Default.Description,
                title = "View PDF",
                description = "Read documents",
                isProFeature = false,
                onClick = { /* Navigate to viewer */ }
            )
        }
    }
}

@Composable
fun ToolCard(
    icon: ImageVector,
    title: String,
    description: String,
    isProFeature: Boolean = false,
    isPro: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(enabled = !isProFeature || isPro, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isProFeature && !isPro) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (isProFeature && !isPro) {
                        Color.Gray
                    } else {
                        PrimaryBlue
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isProFeature && !isPro) {
                        Color.Gray
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isProFeature && !isPro) {
                        Color.Gray
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 2
                )
                
                if (isProFeature && !isPro) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = { },
                        label = { Text("PRO", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
