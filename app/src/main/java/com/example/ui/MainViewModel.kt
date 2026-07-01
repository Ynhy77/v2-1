package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ScanEntity
import com.example.data.ScanRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class ScreenState {
    LOGIN,
    LOADING,
    DASHBOARD
}

sealed interface ScanStatus {
    object Idle : ScanStatus
    data class Scanning(val timeLeft: Int) : ScanStatus
    data class Completed(val room: String, val winPercentage: Double, val timestamp: String) : ScanStatus
}

class MainViewModel(private val repository: ScanRepository) : ViewModel() {

    // Current Screen
    private val _screenState = MutableStateFlow(ScreenState.LOGIN)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    // User Data
    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Server Loading progress (0.0 to 1.0)
    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    private val _loadingStatusText = MutableStateFlow("Đang tải dữ liệu máy chủ...")
    val loadingStatusText: StateFlow<String> = _loadingStatusText.asStateFlow()

    // Dashboard Scanner states
    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    private val _currentRoom = MutableStateFlow("")
    val currentRoom: StateFlow<String> = _currentRoom.asStateFlow()

    private val _currentWinPercentage = MutableStateFlow(0.0)
    val currentWinPercentage: StateFlow<Double> = _currentWinPercentage.asStateFlow()

    private val _currentTimeStamp = MutableStateFlow("")
    val currentTimeStamp: StateFlow<String> = _currentTimeStamp.asStateFlow()

    // Auto-scan cycle configuration
    private val _isAutoScanEnabled = MutableStateFlow(true)
    val isAutoScanEnabled: StateFlow<Boolean> = _isAutoScanEnabled.asStateFlow()

    // Scan history from Room DB
    val scanHistory: StateFlow<List<ScanEntity>> = repository.allScans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var scanJob: Job? = null
    private var loadingJob: Job? = null

    private val rooms = listOf(
        "Nhà kho", 
        "Phòng họp", 
        "Phòng giám đốc", 
        "Phòng trò chuyện", 
        "Phòng giám sát", 
        "Văn phòng", 
        "Phòng nhân sự", 
        "Phòng tài vụ"
    )

    fun onUserIdChanged(newId: String) {
        // Keep only alphanumeric/digits as input
        if (newId.all { it.isDigit() }) {
            _userId.value = newId
            _loginError.value = null
        }
    }

    fun attemptLogin() {
        val id = _userId.value
        if (id.length >= 6 && id.all { it.isDigit() }) {
            _loginError.value = null
            startLoadingTransition()
        } else {
            _loginError.value = "ID không hợp lệ. Phải chứa ít nhất 6 chữ số."
        }
    }

    private fun startLoadingTransition() {
        _screenState.value = ScreenState.LOADING
        _loadingProgress.value = 0f
        
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            val totalSteps = 100
            val durationMs = 5000L // 5 seconds loading bar from python code
            val stepDelay = durationMs / totalSteps
            
            for (i in 1..totalSteps) {
                delay(stepDelay)
                _loadingProgress.value = i / 100f
                
                // Animating text dots
                val dots = ".".repeat((i / 5) % 4)
                _loadingStatusText.value = "🕒 Đang tải dữ liệu máy chủ$dots"
            }
            
            _loadingStatusText.value = "✅ Dữ liệu máy chủ đã sẵn sàng!"
            delay(800)
            _screenState.value = ScreenState.DASHBOARD
            
            // Automatically start scanning upon entering Dashboard
            if (_isAutoScanEnabled.value) {
                startScanningCycle()
            }
        }
    }

    fun startScanningCycle() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            while (true) {
                // Select random values
                val chosenRoom = rooms.random()
                val winRate = Random.nextDouble(0.0, 100.0)
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedTime = sdf.format(Date())

                _currentRoom.value = chosenRoom
                _currentWinPercentage.value = winRate
                _currentTimeStamp.value = formattedTime
                _scanStatus.value = ScanStatus.Scanning(60)

                // Countdown from 60 to 1 second
                for (secondsLeft in 60 downTo 1) {
                    _scanStatus.value = ScanStatus.Scanning(secondsLeft)
                    delay(1000)
                }

                // Completed!
                _scanStatus.value = ScanStatus.Completed(chosenRoom, winRate, formattedTime)

                // Save to Room DB
                val scanLog = ScanEntity(
                    userId = _userId.value,
                    room = chosenRoom,
                    winPercentage = winRate,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertScan(scanLog)

                // Let it stay completed for 2 seconds before looping again
                delay(2000)

                if (!_isAutoScanEnabled.value) {
                    break
                }
            }
        }
    }

    fun skipCurrentScanCountdown() {
        // Direct complete the scan for demo purposes to avoid waiting 60s
        scanJob?.cancel()
        viewModelScope.launch {
            val chosenRoom = _currentRoom.value.ifEmpty { rooms.random() }
            val winRate = if (_currentWinPercentage.value == 0.0) Random.nextDouble(0.0, 100.0) else _currentWinPercentage.value
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedTime = sdf.format(Date())

            _currentRoom.value = chosenRoom
            _currentWinPercentage.value = winRate
            _currentTimeStamp.value = formattedTime
            _scanStatus.value = ScanStatus.Completed(chosenRoom, winRate, formattedTime)

            // Save to Room
            val scanLog = ScanEntity(
                userId = _userId.value,
                room = chosenRoom,
                winPercentage = winRate,
                timestamp = System.currentTimeMillis()
            )
            repository.insertScan(scanLog)

            delay(2000)
            if (_isAutoScanEnabled.value) {
                startScanningCycle()
            } else {
                _scanStatus.value = ScanStatus.Idle
            }
        }
    }

    fun toggleAutoScan() {
        val nextVal = !_isAutoScanEnabled.value
        _isAutoScanEnabled.value = nextVal
        if (nextVal) {
            startScanningCycle()
        } else {
            scanJob?.cancel()
            _scanStatus.value = ScanStatus.Idle
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllScans()
        }
    }

    fun logout() {
        scanJob?.cancel()
        loadingJob?.cancel()
        _userId.value = ""
        _screenState.value = ScreenState.LOGIN
        _scanStatus.value = ScanStatus.Idle
        _currentRoom.value = ""
        _currentWinPercentage.value = 0.0
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
        loadingJob?.cancel()
    }
}

class MainViewModelFactory(private val repository: ScanRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
