package com.example.myapplicationfocusflow.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.myapplicationfocusflow.model.SessionPhase
import com.example.myapplicationfocusflow.ViewModel.FocusModeViewModel
import com.example.myapplicationfocusflow.ViewModel.FocusSessionViewModel
import com.example.myapplicationfocusflow.model.FocusModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun FocusSessionView(
    focusId: Int,
    sessionViewModel: FocusSessionViewModel,
    focusViewModel: FocusModeViewModel,
    onSessionComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val currentPhase by sessionViewModel.currentPhase.collectAsState()
    val timeLeft by sessionViewModel.timeLeft.collectAsState()
    val isRunning by sessionViewModel.isRunning.collectAsState()
    val sessionCompleted by sessionViewModel.sessionCompleted.collectAsState()
    val completedCycles by sessionViewModel.completedCycles.collectAsState()
    var focusModel by remember { mutableStateOf<FocusModel?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(focusId) {
        focusModel = focusViewModel.getFocusModelById(focusId)
        focusModel?.let { model ->
            sessionViewModel.startSession(focusId, model.focusDuration, model.restDuration)
        }
    }

    LaunchedEffect(sessionCompleted) {
        if (sessionCompleted) {
            focusModel?.let { model ->
                val updatedModel = model.copy(
                    completedSessions = model.completedSessions + 1,
                    isCompleted = model.completedSessions + 1 >= model.totalSessions
                )
                focusViewModel.updateFocusModel(updatedModel)
            }
            onSessionComplete()
        }
    }

    val backgroundColor = when (currentPhase) {
        SessionPhase.FOCUS -> Color.Black
        SessionPhase.REST -> Color.White
        else -> Color.Black
    }

    val textColor = when (currentPhase) {
        SessionPhase.FOCUS -> Color.White
        SessionPhase.REST -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cycles Counter
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentPhase == SessionPhase.FOCUS) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "Cycles Completed: $completedCycles",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Phase Indicator
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentPhase == SessionPhase.FOCUS) Color.White else Color.Black
                )
            ) {
                Text(
                    text = when (currentPhase) {
                        SessionPhase.FOCUS -> "Focus"
                        SessionPhase.REST -> "Rest"
                        SessionPhase.PAUSED -> "Paused"
                        SessionPhase.COMPLETED -> "Completed"
                    },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = if (currentPhase == SessionPhase.FOCUS) Color.Black else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Timer Display
            Text(
                text = formatTime(timeLeft),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = { sessionViewModel.resetTimer() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Play/Pause Button
                IconButton(
                    onClick = { sessionViewModel.toggleTimer() },
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Menu else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = textColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Skip Button
                IconButton(
                    onClick = { sessionViewModel.skipPhase() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Skip",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Complete Session Button
            Button(
                onClick = {
                    sessionViewModel.completeSession()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPhase == SessionPhase.FOCUS) Color.White else Color.Black,
                    contentColor = if (currentPhase == SessionPhase.FOCUS) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Complete",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complete Session",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Back Button
            TextButton(
                onClick = {
                    scope.launch {
                        sessionViewModel.resetSession()
                        onBackClick()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = textColor.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = "Back to Setup",
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}