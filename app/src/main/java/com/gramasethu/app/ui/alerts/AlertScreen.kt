package com.gramasethu.app.ui.alerts

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gramasethu.app.data.model.Bridge
import com.gramasethu.app.data.model.BridgeStatus
import com.gramasethu.app.ui.map.MapViewModel
import com.gramasethu.app.ui.theme.GreenOpen
import com.gramasethu.app.ui.theme.RedSubmerged
import com.gramasethu.app.ui.theme.YellowDamaged
import com.gramasethu.app.utils.NotificationUtils
import com.gramasethu.app.utils.SoundUtils
import com.gramasethu.app.utils.TimeUtils

/**
 * AlertsScreen shows all dangerous bridges
 * and allows monsoon simulation with notifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val bridges by viewModel.bridges.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter only dangerous bridges
    val dangerousBridges = bridges.filter {
        it.status == BridgeStatus.SUBMERGED || it.status == BridgeStatus.DAMAGED
    }

    // Show snackbar messages
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Play sound if any bridge is submerged
    LaunchedEffect(bridges) {
        val hasSubmerged = bridges.any { it.status == BridgeStatus.SUBMERGED }
        if (hasSubmerged) {
            SoundUtils.playWarningSound(context)
            SoundUtils.vibrateWarning(context)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("🔔 Monsoon Alerts") },
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Summary card at top
            item {
                SummaryCard(
                    totalBridges = bridges.size,
                    openCount = bridges.count { it.status == BridgeStatus.OPEN },
                    damagedCount = bridges.count { it.status == BridgeStatus.DAMAGED },
                    submergedCount = bridges.count { it.status == BridgeStatus.SUBMERGED }
                )
            }

            // Simulate & Reset buttons
            item {
                SimulationControls(
                    isSimulating = isSimulating,
                    onSimulate = {
                        viewModel.simulateMonsoon()
                        // Show notification after simulation
                        val submergedCount = bridges.count {
                            it.status == BridgeStatus.SUBMERGED
                        }
                        NotificationUtils.showMonsoonAlert(context, submergedCount + 2)
                    },
                    onReset = { viewModel.resetAllBridges() },
                    onTestSound = {
                        SoundUtils.playWarningSound(context)
                        SoundUtils.vibrateWarning(context)
                    },
                    context = context,
                    bridges = bridges
                )
            }

            // Section header
            item {
                if (dangerousBridges.isEmpty()) {
                    AllClearCard()
                } else {
                    Text(
                        text = "⚠️ ${dangerousBridges.size} Bridges Need Attention",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedSubmerged
                    )
                }
            }

            // List of dangerous bridges
            items(dangerousBridges) { bridge ->
                AlertBridgeCard(
                    bridge = bridge,
                    context = context
                )
            }

            // All bridges section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All Bridges (${bridges.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(bridges) { bridge ->
                SmallBridgeCard(bridge = bridge)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SummaryCard(
    totalBridges: Int,
    openCount: Int,
    damagedCount: Int,
    submergedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bridge Status Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusCount(
                    count = openCount,
                    label = "Open",
                    color = GreenOpen
                )
                StatusCount(
                    count = damagedCount,
                    label = "Damaged",
                    color = YellowDamaged
                )
                StatusCount(
                    count = submergedCount,
                    label = "Submerged",
                    color = RedSubmerged
                )
            }
        }
    }
}

@Composable
fun StatusCount(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
        )
    }
}

@Composable
fun SimulationControls(
    isSimulating: Boolean,
    onSimulate: () -> Unit,
    onReset: () -> Unit,
    onTestSound: () -> Unit,
    context: Context,
    bridges: List<Bridge>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D47A1).copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🌧️ Monsoon Simulation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF42A5F5)
            )

            Text(
                text = "Test the app by simulating heavy rainfall conditions",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )

            // Simulate button
            Button(
                onClick = onSimulate,
                enabled = !isSimulating,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                if (isSimulating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simulating...")
                } else {
                    Icon(Icons.Default.Water, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "🌊 Simulate Monsoon",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reset button
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset All")
                }

                // Test Sound button
                OutlinedButton(
                    onClick = onTestSound,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Sound")
                }
            }

            // Test notification button
            Button(
                onClick = {
                    bridges.firstOrNull { it.status == BridgeStatus.SUBMERGED }
                        ?.let { bridge ->
                            NotificationUtils.showBridgeAlert(
                                context = context,
                                bridgeName = bridge.name,
                                status = "SUBMERGED",
                                distance = "0.5 km"
                            )
                        } ?: NotificationUtils.showBridgeAlert(
                        context = context,
                        bridgeName = "Test Bridge",
                        status = "SUBMERGED",
                        distance = "0.3 km"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedSubmerged.copy(alpha = 0.8f)
                )
            ) {
                Icon(Icons.Default.Notifications, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔔 Test Alert Notification")
            }
        }
    }
}

@Composable
fun AlertBridgeCard(bridge: Bridge, context: Context) {
    val isSubmerged = bridge.status == BridgeStatus.SUBMERGED
    val cardColor = if (isSubmerged)
        RedSubmerged.copy(alpha = 0.15f)
    else
        YellowDamaged.copy(alpha = 0.15f)
    val borderColor = if (isSubmerged) RedSubmerged else YellowDamaged

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSubmerged) "🚨 SUBMERGED" else "⚠️ DAMAGED",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
                Text(
                    text = TimeUtils.getTimeAgo(bridge.lastUpdated),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }

            Text(
                text = bridge.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "📍 ${bridge.village}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )

            if (bridge.alternateRoute.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🔀", fontSize = 14.sp)
                    Text(
                        text = bridge.alternateRoute,
                        fontSize = 13.sp,
                        color = Color(0xFF42A5F5)
                    )
                }
            }

            // Send alert button
            if (isSubmerged) {
                Button(
                    onClick = {
                        NotificationUtils.showBridgeAlert(
                            context = context,
                            bridgeName = bridge.name,
                            status = "SUBMERGED"
                        )
                        SoundUtils.playWarningSound(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedSubmerged
                    )
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Alert to Nearby Users")
                }
            }
        }
    }
}

@Composable
fun SmallBridgeCard(bridge: Bridge) {
    val statusColor = when (bridge.status) {
        BridgeStatus.OPEN -> GreenOpen
        BridgeStatus.DAMAGED -> YellowDamaged
        BridgeStatus.SUBMERGED -> RedSubmerged
    }
    val statusEmoji = when (bridge.status) {
        BridgeStatus.OPEN -> "✅"
        BridgeStatus.DAMAGED -> "⚠️"
        BridgeStatus.SUBMERGED -> "🚨"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(statusEmoji, fontSize = 20.sp)
                Column {
                    Text(
                        text = bridge.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = bridge.village,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            }
            Text(
                text = bridge.status.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun AllClearCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GreenOpen.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, GreenOpen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✅", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Clear!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GreenOpen
            )
            Text(
                text = "All bridges are currently safe",
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )
        }
    }
}