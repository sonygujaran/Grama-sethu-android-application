package com.gramasethu.app.ui.report

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gramasethu.app.data.model.Bridge
import com.gramasethu.app.data.model.BridgeStatus
import com.gramasethu.app.ui.map.MapViewModel
import com.gramasethu.app.ui.theme.GreenOpen
import com.gramasethu.app.ui.theme.RedSubmerged
import com.gramasethu.app.ui.theme.YellowDamaged
import com.gramasethu.app.utils.TimeUtils

/**
 * ReportScreen allows users to update bridge status
 * with ONE TAP — Open, Damaged, or Submerged
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    bridge: Bridge,
    onBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val message by viewModel.message.collectAsState()
    var selectedStatus by remember { mutableStateOf<BridgeStatus?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var reportSubmitted by remember { mutableStateOf(false) }

    // Show success and go back
    LaunchedEffect(message) {
        if (message?.contains("✅") == true) {
            reportSubmitted = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Bridge Status") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Success screen after report
            if (reportSubmitted) {
                SuccessCard(onBack = onBack)
                return@Column
            }

            // Bridge Info Card
            BridgeInfoCard(bridge = bridge)

            // Instructions
            Text(
                text = "Tap to update bridge status:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // OPEN Button
            StatusButton(
                icon = "✅",
                title = "OPEN",
                subtitle = "Bridge is safe to cross",
                color = GreenOpen,
                isSelected = selectedStatus == BridgeStatus.OPEN,
                onClick = {
                    selectedStatus = BridgeStatus.OPEN
                    showConfirmDialog = true
                }
            )

            // DAMAGED Button
            StatusButton(
                icon = "⚠️",
                title = "DAMAGED",
                subtitle = "Bridge has damage — use caution",
                color = YellowDamaged,
                isSelected = selectedStatus == BridgeStatus.DAMAGED,
                onClick = {
                    selectedStatus = BridgeStatus.DAMAGED
                    showConfirmDialog = true
                }
            )

            // SUBMERGED Button
            StatusButton(
                icon = "🚨",
                title = "SUBMERGED",
                subtitle = "Bridge is underwater — DO NOT CROSS!",
                color = RedSubmerged,
                isSelected = selectedStatus == BridgeStatus.SUBMERGED,
                onClick = {
                    selectedStatus = BridgeStatus.SUBMERGED
                    showConfirmDialog = true
                }
            )

            // Current status info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Status",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (bridge.status) {
                            BridgeStatus.OPEN -> "✅ OPEN"
                            BridgeStatus.DAMAGED -> "⚠️ DAMAGED"
                            BridgeStatus.SUBMERGED -> "🚨 SUBMERGED"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Last updated: ${TimeUtils.getTimeAgo(bridge.lastUpdated)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog && selectedStatus != null) {
        ConfirmDialog(
            status = selectedStatus!!,
            bridgeName = bridge.name,
            onConfirm = {
                viewModel.updateBridgeStatus(bridge.id, selectedStatus!!)
                showConfirmDialog = false
                viewModel.clearMessage()
            },
            onDismiss = {
                showConfirmDialog = false
                selectedStatus = null
            }
        )
    }
}

/**
 * Big colorful status button — easy to tap with one hand!
 */
@Composable
fun StatusButton(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                color.copy(alpha = 0.3f)
            else
                color.copy(alpha = 0.1f)
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, color)
        else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = icon, fontSize = 36.sp)
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
            }
        }
    }
}

@Composable
fun BridgeInfoCard(bridge: Bridge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "🌉", fontSize = 40.sp)
            Column {
                Text(
                    text = bridge.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "📍 ${bridge.village}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
        }
    }
}

@Composable
fun SuccessCard(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "✅", fontSize = 80.sp)
        Text(
            text = "Report Submitted!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GreenOpen
        )
        Text(
            text = "All users will see the\nupdated status within 3 seconds",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenOpen)
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Map", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ConfirmDialog(
    status: BridgeStatus,
    bridgeName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val color = when (status) {
        BridgeStatus.OPEN -> GreenOpen
        BridgeStatus.DAMAGED -> YellowDamaged
        BridgeStatus.SUBMERGED -> RedSubmerged
    }
    val emoji = when (status) {
        BridgeStatus.OPEN -> "✅"
        BridgeStatus.DAMAGED -> "⚠️"
        BridgeStatus.SUBMERGED -> "🚨"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text(emoji, fontSize = 40.sp) },
        title = {
            Text(
                text = "Confirm Report",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Mark \"$bridgeName\" as ${status.name}?\n\nThis will update for ALL users immediately.",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("Yes, Report", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}