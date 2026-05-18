package com.gramasethu.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.gramasethu.app.data.model.Bridge
import com.gramasethu.app.data.model.BridgeStatus
import com.gramasethu.app.ui.theme.GreenOpen
import com.gramasethu.app.ui.theme.RedSubmerged
import com.gramasethu.app.ui.theme.YellowDamaged
import com.gramasethu.app.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToReport: (Bridge) -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToProfile: () -> Unit,   // ✅ ADDED
    onLogout: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val bridges by viewModel.bridges.collectAsState()
    val selectedBridge by viewModel.selectedBridge.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()

    val karnatakaCenterPosition = LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(karnatakaCenterPosition, 10f)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌉", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Grama-Sethu",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAlerts) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // ✅ ADDED Profile button
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LegendDot(color = GreenOpen, label = "Open")
                        LegendDot(color = YellowDamaged, label = "Damaged")
                        LegendDot(color = RedSubmerged, label = "Submerged")
                    }

                    Button(
                        onClick = { viewModel.simulateMonsoon() },
                        enabled = !isSimulating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSimulating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Default.Water,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                bridges.forEach { bridge ->
                    BridgeMarker(
                        bridge = bridge,
                        onClick = {
                            viewModel.onBridgeSelected(bridge)
                            true
                        }
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenOpen)
                }
            }

            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "📍 ${bridges.size} Bridges",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        selectedBridge?.let { bridge ->
            BridgeDetailSheet(
                bridge = bridge,
                onDismiss = { viewModel.clearSelectedBridge() },
                onReportClick = {
                    viewModel.clearSelectedBridge()
                    onNavigateToReport(bridge)
                }
            )
        }
    }
}

@Composable
fun BridgeMarker(
    bridge: Bridge,
    onClick: () -> Boolean
) {
    val markerColor = when (bridge.status) {
        BridgeStatus.OPEN -> BitmapDescriptorFactory.HUE_GREEN
        BridgeStatus.DAMAGED -> BitmapDescriptorFactory.HUE_YELLOW
        BridgeStatus.SUBMERGED -> BitmapDescriptorFactory.HUE_RED
    }

    val statusEmoji = when (bridge.status) {
        BridgeStatus.OPEN -> "✅"
        BridgeStatus.DAMAGED -> "⚠️"
        BridgeStatus.SUBMERGED -> "🚨"
    }

    Marker(
        state = MarkerState(
            position = LatLng(bridge.latitude, bridge.longitude)
        ),
        title = "$statusEmoji ${bridge.name}",
        snippet = "Status: ${bridge.status.name} • ${TimeUtils.getTimeAgo(bridge.lastUpdated)}",
        icon = BitmapDescriptorFactory.defaultMarker(markerColor),
        onClick = { onClick() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BridgeDetailSheet(
    bridge: Bridge,
    onDismiss: () -> Unit,
    onReportClick: () -> Unit
) {
    val statusColor = when (bridge.status) {
        BridgeStatus.OPEN -> GreenOpen
        BridgeStatus.DAMAGED -> YellowDamaged
        BridgeStatus.SUBMERGED -> RedSubmerged
    }

    val statusText = when (bridge.status) {
        BridgeStatus.OPEN -> "✅ OPEN — Safe to Cross"
        BridgeStatus.DAMAGED -> "⚠️ DAMAGED — Use Caution"
        BridgeStatus.SUBMERGED -> "🚨 SUBMERGED — Do NOT Cross!"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = bridge.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(12.dp),
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            InfoRow(icon = "📍", label = "Village", value = bridge.village)
            InfoRow(
                icon = "🕐",
                label = "Last Updated",
                value = TimeUtils.getTimeAgo(bridge.lastUpdated)
            )
            InfoRow(
                icon = "👤",
                label = "Reported By",
                value = bridge.reportedBy.ifEmpty { "Unknown" }
            )

            if (bridge.status != BridgeStatus.OPEN &&
                bridge.alternateRoute.isNotEmpty()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1565C0).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🔀 Alternate Route",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF42A5F5)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bridge.alternateRoute,
                            color = Color(0xFF90CAF9)
                        )
                    }
                }
            }

            Button(
                onClick = onReportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Update Bridge Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$icon $label",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(6.dp))
        )
        Text(text = label, fontSize = 11.sp)
    }
}