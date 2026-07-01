package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ScanDatabase
import com.example.data.ScanEntity
import com.example.data.ScanRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.ScanStatus
import com.example.ui.ScreenState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize local database and repository
        val database = ScanDatabase.getDatabase(applicationContext)
        val repository = ScanRepository(database.scanDao())
        
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(repository)
                )
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CyberBackground)
                            .padding(innerPadding)
                    ) {
                        // Ambient elegant glow effect background
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(CyberPrimary.copy(alpha = 0.04f), Color.Transparent),
                                    center = Offset(size.width * 0.2f, size.height * 0.2f),
                                    radius = size.width * 0.6f
                                ),
                                radius = size.width * 0.6f,
                                center = Offset(size.width * 0.2f, size.height * 0.2f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(CyberSecondary.copy(alpha = 0.04f), Color.Transparent),
                                    center = Offset(size.width * 0.8f, size.height * 0.8f),
                                    radius = size.width * 0.6f
                                ),
                                radius = size.width * 0.6f,
                                center = Offset(size.width * 0.8f, size.height * 0.8f)
                            )
                        }

                        val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                        
                        when (screenState) {
                            ScreenState.LOGIN -> LoginScreen(viewModel)
                            ScreenState.LOADING -> LoadingScreen(viewModel)
                            ScreenState.DASHBOARD -> DashboardScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: LOGIN / USER ID ENTRY SCREEN
// ==========================================
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val userId by viewModel.userId.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Futuristic Cyber Header
        HeaderBanner()

        Spacer(modifier = Modifier.height(32.dp))

        // Shield Logo Frame
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .border(2.dp, CyberPrimary, CircleShape)
                .background(CyberSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon_1782885436027),
                contentDescription = "AGM Cyber Shield Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title text
        Text(
            text = "XÁC MINH DANH TÍNH",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = CyberPrimary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Vui lòng nhập ID máy chủ để kết nối hệ thống quét bảo mật",
            style = MaterialTheme.typography.bodySmall,
            color = CyberTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        )

        // ID Input Field
        OutlinedTextField(
            value = userId,
            onValueChange = { viewModel.onUserIdChanged(it) },
            label = { Text("MÃ SỐ ID (Tối thiểu 6 số)") },
            placeholder = { Text("Ví dụ: 128901") },
            isError = loginError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    viewModel.attemptLogin()
                }
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (loginError != null) CyberError else CyberPrimary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = CyberBorder,
                focusedLabelColor = CyberPrimary,
                unfocusedLabelColor = CyberTextMuted,
                cursorColor = CyberPrimary,
                errorBorderColor = CyberError,
                errorLabelColor = CyberError
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("id_input"),
            shape = RoundedCornerShape(16.dp)
        )

        if (loginError != null) {
            Text(
                text = loginError ?: "",
                color = CyberError,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Connect Button
        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.attemptLogin()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberPrimary,
                contentColor = CyberOnPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = CyberPrimary, spotColor = CyberPrimary)
                .testTag("login_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KẾT NỐI MÁY CHỦ",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Contact details card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YÊU CẦU CẤP QUYỀN TRUY CẬP?",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CyberPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nếu ID của bạn chưa đăng ký trên máy chủ, liên hệ Telegram ➡️ Linh28901 hoặc tham gia nhóm telegram bên dưới để được cấp quyền nhanh chóng.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ==========================================
// SCREEN 2: IMERSIVE SERVER LOADING SCREEN
// ==========================================
@Composable
fun LoadingScreen(viewModel: MainViewModel) {
    val progress by viewModel.loadingProgress.collectAsStateWithLifecycle()
    val statusText by viewModel.loadingStatusText.collectAsStateWithLifecycle()

    // Matrix background flow simulation
    var matrixPhase by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            matrixPhase = (matrixPhase + 1) % 5
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorative hacking grid terminal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CyberSurface)
                .border(1.dp, CyberBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "LOG: CLIENT_HANDSHAKE_INITIATED",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPrimary
                )
                Text(
                    text = "IP: 192.168.72.${10 + matrixPhase} - PORT: 8080",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberTextMuted
                )
                Text(
                    text = "SECURE TUNNEL: SSL_TLS_v1.3_AES_256",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberSuccess
                )
                Text(
                    text = "SQL_DB: CONNECT_OK (LATENCY: ${14 + matrixPhase * 3}ms)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberTertiary
                )
                Text(
                    text = "AGM_TNL_CORE_ENGINE: LOADED_SUCCESSFULLY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Large Cybernetic Ring Progress Indicator
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = CyberSecondary,
                strokeWidth = 8.dp,
                trackColor = CyberSurface,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = CyberSecondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Dynamic Loading Text with pulsating dot animation
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = CyberSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Vui lòng giữ kết nối, đang thiết lập cấu hình...",
            style = MaterialTheme.typography.bodySmall,
            color = CyberTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// SCREEN 3: SCANNING DASHBOARD / RESULTS
// ==========================================
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val userId by viewModel.userId.collectAsStateWithLifecycle()
    val scanStatus by viewModel.scanStatus.collectAsStateWithLifecycle()
    val room by viewModel.currentRoom.collectAsStateWithLifecycle()
    val winRate by viewModel.currentWinPercentage.collectAsStateWithLifecycle()
    val timeStamp by viewModel.currentTimeStamp.collectAsStateWithLifecycle()
    val isAuto by viewModel.isAutoScanEnabled.collectAsStateWithLifecycle()
    val history by viewModel.scanHistory.collectAsStateWithLifecycle()

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome Header Bar with logout action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Xin chào,",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberTextMuted
                    )
                    Text(
                        text = "ID: $userId",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyberPrimary
                    )
                }
            }

            IconButton(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CyberError.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Đăng xuất",
                    tint = CyberError
                )
            }
        }

        // Horizontal scrolling decorative banner line
        HeaderBanner(mini = true)

        Spacer(modifier = Modifier.height(12.dp))

        // PRIMARY SCANNING CARD PANEL
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberBorder),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORE SECURITY SCANNER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyberPrimary
                    )

                    // Auto Status indicator pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isAuto) CyberSuccess.copy(alpha = 0.15f) else CyberTextMuted.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAuto) "AUTO ACTIVE" else "AUTO DISABLED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isAuto) CyberSuccess else CyberTextMuted,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gauge rendering success rate
                SuccessRateGauge(percentage = winRate)

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Information Block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Khu vực quét 🌐:", style = MaterialTheme.typography.bodyMedium, color = CyberTextMuted)
                        Text(
                            text = room.ifEmpty { "Đang chờ..." },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (room.isNotEmpty()) CyberPrimary else CyberTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Thời gian quét 🕒:", style = MaterialTheme.typography.bodyMedium, color = CyberTextMuted)
                        Text(
                            text = timeStamp.ifEmpty { "Chưa bắt đầu" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            color = CyberTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action controls / countdown progress representation
                when (val status = scanStatus) {
                    is ScanStatus.Scanning -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = CyberTertiary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Đang Quét Hệ Thống...",
                                        color = CyberTertiary,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "${status.timeLeft} giây",
                                    color = CyberTertiary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { status.timeLeft / 60f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = CyberTertiary,
                                trackColor = CyberSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bypass Countdown Button
                            Button(
                                onClick = { viewModel.skipCurrentScanCountdown() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberSecondary.copy(alpha = 0.2f),
                                    contentColor = CyberSecondary
                                ),
                                border = BorderStroke(1.dp, CyberSecondary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "HOÀN THÀNH NGAY (BỎ QUA CHỜ)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                            }
                        }
                    }
                    is ScanStatus.Completed -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyberSuccess.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    tint = CyberSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🎉 KẾT QUẢ ĐÃ SẴN SÀNG!",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = CyberSuccess
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.startScanningCycle() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberOnPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                ) {
                                    Text("QUÉT TIẾP", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                                
                                Button(
                                    onClick = { viewModel.toggleAutoScan() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAuto) CyberError.copy(alpha = 0.2f) else CyberSuccess.copy(alpha = 0.2f),
                                        contentColor = if (isAuto) CyberError else CyberOnSecondary
                                    ),
                                    border = BorderStroke(1.dp, if (isAuto) CyberError else CyberSuccess),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                ) {
                                    Text(
                                        text = if (isAuto) "TẮT TỰ ĐỘNG" else "BẬT TỰ ĐỘNG",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                    is ScanStatus.Idle -> {
                        // Scan trigger/play controls
                        Button(
                            onClick = { viewModel.startScanningCycle() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberOnPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BẮT ĐẦU QUÉT HỆ THỐNG",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RECENT SCAN LOGS HISTORY TITLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LỊCH SỬ QUÉT HỆ THỐNG",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color.White
            )

            if (history.isNotEmpty()) {
                Text(
                    text = "Xóa Lịch Sử",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = CyberError,
                    modifier = Modifier.clickable { showClearHistoryDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // History Log List Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(CyberSurface.copy(alpha = 0.5f))
                .border(1.dp, CyberBorder, RoundedCornerShape(24.dp))
        ) {
            if (history.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CyberTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Không có dữ liệu lịch sử",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kích hoạt quét bảo mật để lưu nhật ký lịch sử quét tại đây.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { log ->
                        ScanHistoryItem(log)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Nav/Meta Info Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberSurface)
                .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SUPPORT & TELEGRAM",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    color = CyberTextMuted
                )
                Text(
                    text = "@agmnetworkxyz001",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = CyberPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = CyberTextMuted
                )
                Text(
                    text = "SYSTEM ACTIVE",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    color = CyberSecondary
                )
            }
        }
    }

    // Clear confirmation dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = CyberSurface,
            titleContentColor = Color.White,
            textContentColor = CyberTextMuted,
            title = {
                Text(
                    text = "XÓA LỊCH SỬ?",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = "Bạn có chắc chắn muốn xóa toàn bộ lịch sử quét lưu trên thiết bị này không? Hành động này không thể hoàn tác.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(text = "XÓA TOÀN BỘ", color = CyberError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(text = "HỦY BỎ", color = Color.White)
                }
            }
        )
    }
}

// ==========================================
// COMPONENT: LOG HISTORY ROW ITEM
// ==========================================
@Composable
fun ScanHistoryItem(log: ScanEntity) {
    val formattedDate = remember(log.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, CyberBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Colored dot based on rate percentage
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                log.winPercentage >= 70.0 -> CyberSuccess
                                log.winPercentage >= 40.0 -> CyberWarning
                                else -> CyberError
                            }
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Phòng: ${log.room}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Lúc: $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f%%", log.winPercentage),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = when {
                        log.winPercentage >= 70.0 -> CyberSuccess
                        log.winPercentage >= 40.0 -> CyberWarning
                        else -> CyberError
                    }
                )
                Text(
                    text = "ID: ${log.userId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberTextMuted
                )
            }
        }
    }
}

// ==========================================
// COMPONENT: BRANDING ASCII BANNER HEADER
// ==========================================
@Composable
fun HeaderBanner(mini: Boolean = false) {
    if (mini) {
        // Simple elegant sliding single-line brand label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, CyberPrimary, Color.Transparent)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AGM SECURE CHANNEL ACTIVE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = CyberPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, CyberPrimary, Color.Transparent)
                        )
                    )
            )
        }
    } else {
        // High fidelity cyber grid representation of ASCII Art
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberPrimary, RoundedCornerShape(12.dp))
                .background(CyberSurface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AGMNETWORK TECHNOLOGY",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = CyberSecondary,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = CyberPrimary.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HỆ THỐNG QUÉT BẢO MẬT AGM TNL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CyberTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Made by: AGMNETWORKGAME",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Telegram: T.me/agmnetworkxyz001",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = CyberPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Phiên bản: AGM TOOL Android v1.0",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = CyberTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==========================================
// COMPONENT: CIRCULAR GAUGE FOR PERCENTAGE
// ==========================================
@Composable
fun SuccessRateGauge(percentage: Double, modifier: Modifier = Modifier) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "percentageAnimation"
    )

    val colorAccent = when {
        animatedPercentage >= 70f -> CyberSuccess
        animatedPercentage >= 40f -> CyberWarning
        else -> CyberError
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(180.dp)
            .drawBehind {
                // Draw elegant dashed outer ring representing the scanning radar
                drawCircle(
                    color = CyberBorder,
                    radius = (size.minDimension / 2) - 4.dp.toPx(),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(12f, 8f), 0f
                        )
                    )
                )
            }
    ) {
        // Inner glowing solid circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(CyberBackground)
                .border(3.5.dp, colorAccent, CircleShape)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = colorAccent.copy(alpha = 0.25f),
                    spotColor = colorAccent.copy(alpha = 0.25f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f", percentage),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 32.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 4.dp, start = 1.dp)
                    )
                }
                Text(
                    text = "TỶ LỆ AN TOÀN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.sp
                    ),
                    color = CyberPrimary
                )
            }
        }
    }
}
