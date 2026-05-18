package com.gramasethu.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.gramasethu.app.data.model.BridgeStatus
import com.gramasethu.app.ui.map.MapViewModel
import com.gramasethu.app.ui.theme.GreenOpen
import com.gramasethu.app.ui.theme.RedSubmerged
import com.gramasethu.app.ui.theme.YellowDamaged
import javax.inject.Inject

/**
 * ProfileScreen shows user info and app statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val bridges by mapViewModel.bridges.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val openCount = bridges.count { it.status == BridgeStatus.OPEN }
    val damagedCount = bridges.count { it.status == BridgeStatus.DAMAGED }
    val submergedCount = bridges.count { it.status == BridgeStatus.SUBMERGED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
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

            // Profile Avatar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(GreenOpen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.email
                                ?.firstOrNull()
                                ?.uppercase() ?: "U",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = currentUser?.email ?: "User",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Grama-Kavalu badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = GreenOpen.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "🛡️ Grama-Kavalu Reporter",
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                            color = GreenOpen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Live Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📊 Live Bridge Statistics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = bridges.size.toString(),
                            label = "Total",
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatItem(
                            value = openCount.toString(),
                            label = "Open",
                            color = GreenOpen
                        )
                        StatItem(
                            value = damagedCount.toString(),
                            label = "Damaged",
                            color = YellowDamaged
                        )
                        StatItem(
                            value = submergedCount.toString(),
                            label = "Submerged",
                            color = RedSubmerged
                        )
                    }
                }
            }

            // App Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ℹ️ About Grama-Sethu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    InfoItem(
                        icon = "🎓",
                        title = "VTU Internship Project",
                        subtitle = "MindMatrix Program — Project #96"
                    )
                    InfoItem(
                        icon = "🌉",
                        title = "Purpose",
                        subtitle = "Rural bridge connectivity monitor for Karnataka"
                    )
                    InfoItem(
                        icon = "⚡",
                        title = "Real-time Updates",
                        subtitle = "Bridge status syncs in under 3 seconds"
                    )
                    InfoItem(
                        icon = "🛠️",
                        title = "Tech Stack",
                        subtitle = "Kotlin • Compose • Firebase • Google Maps"
                    )
                }
            }

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedSubmerged
                )
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 28.sp,
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
fun InfoItem(icon: String, title: String, subtitle: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 20.sp)
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }
    }
}