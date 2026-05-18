package com.gramasethu.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gramasethu.app.data.model.Bridge
import com.gramasethu.app.data.model.BridgeStatus
import com.gramasethu.app.data.repository.BridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val bridgeRepository: BridgeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // List of all bridges — updates in real time!
    private val _bridges = MutableStateFlow<List<Bridge>>(emptyList())
    val bridges: StateFlow<List<Bridge>> = _bridges.asStateFlow()

    // Currently selected bridge (when user taps a marker)
    private val _selectedBridge = MutableStateFlow<Bridge?>(null)
    val selectedBridge: StateFlow<Bridge?> = _selectedBridge.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Message to show user (success/error)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Simulation state
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    init {
        loadBridges()
    }

    /**
     * Start listening to Firestore for real-time bridge updates
     */
    private fun loadBridges() {
        viewModelScope.launch {
            bridgeRepository.getBridges()
                .catch { e ->
                    _message.value = "Error loading bridges: ${e.message}"
                    _isLoading.value = false
                }
                .collect { bridgeList ->
                    _bridges.value = bridgeList
                    _isLoading.value = false

                    // If no bridges exist yet, add sample data
                    if (bridgeList.isEmpty()) {
                        addSampleData()
                    }
                }
        }
    }

    /**
     * When user taps a map marker
     */
    fun onBridgeSelected(bridge: Bridge) {
        _selectedBridge.value = bridge
    }

    /**
     * Close the bridge detail bottom sheet
     */
    fun clearSelectedBridge() {
        _selectedBridge.value = null
    }

    /**
     * Update bridge status from Quick Report screen
     */
    fun updateBridgeStatus(bridgeId: String, status: BridgeStatus) {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email ?: "anonymous"
            val result = bridgeRepository.updateBridgeStatus(
                bridgeId, status, userEmail
            )
            _message.value = if (result.isSuccess) {
                "✅ Bridge status updated!"
            } else {
                "❌ Update failed. Try again."
            }
        }
    }

    /**
     * Simulate monsoon — randomly submerges bridges
     * This is for demonstration / testing purposes
     */
    fun simulateMonsoon() {
        viewModelScope.launch {
            _isSimulating.value = true
            _message.value = "🌧️ Simulating monsoon conditions..."

            val currentBridges = _bridges.value
            currentBridges.forEach { bridge ->
                // Randomly assign submerged or open status
                val newStatus = if (Math.random() > 0.5)
                    BridgeStatus.SUBMERGED
                else
                    BridgeStatus.OPEN

                bridgeRepository.updateBridgeStatus(
                    bridge.id, newStatus, "monsoon_simulation"
                )
            }

            _isSimulating.value = false
            _message.value = "🌊 Monsoon simulation complete!"
        }
    }

    /**
     * Reset all bridges to OPEN status
     */
    fun resetAllBridges() {
        viewModelScope.launch {
            _bridges.value.forEach { bridge ->
                bridgeRepository.updateBridgeStatus(
                    bridge.id, BridgeStatus.OPEN, "reset"
                )
            }
            _message.value = "✅ All bridges reset to OPEN"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private suspend fun addSampleData() {
        bridgeRepository.addSampleBridges()
    }
}