package com.example.myapplicationfocusflow.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationfocusflow.model.FocusModel
import com.example.myapplicationfocusflow.model.FocusSessionModel
import com.example.myapplicationfocusflow.ViewModel.FocusModeViewModel
import com.example.myapplicationfocusflow.ViewModel.FocusSessionViewModel
import java.text.SimpleDateFormat
import java.util.*

data class SessionWithFocus(
    val session: FocusSessionModel,
    val focusModel: FocusModel?
)

@Composable
fun HistoryListView(
    focusViewModel: FocusModeViewModel,
    sessionViewModel: FocusSessionViewModel,
    onBackClick: () -> Unit
) {
    val focusList by focusViewModel.focusList.collectAsState()
    val sessionsList by sessionViewModel.getAllSessions().collectAsState(initial = emptyList())

    // Gabungkan sessions dengan focus models yang sesuai
    val sessionsWithFocus = remember(sessionsList, focusList) {
        sessionsList.map { session ->
            val focusModel = focusList.find { it.focus_id == session.focusId }
            SessionWithFocus(session, focusModel)
        }.sortedByDescending { it.session.startTime } // Urutkan berdasarkan waktu mulai terbaru
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "HISTORY\nFOCUS FLOW",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            IconButton(
                onClick = { /* Add new focus */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // History List
        if (sessionsWithFocus.isEmpty()) {
            // Tampilkan pesan jika tidak ada session
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No focus sessions yet",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessionsWithFocus) { sessionWithFocus ->
                    HistoryItem(sessionWithFocus = sessionWithFocus)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(sessionWithFocus: SessionWithFocus) {
    val session = sessionWithFocus.session
    val focusModel = sessionWithFocus.focusModel

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp), // Tinggi sedikit diperbesar untuk info lebih lengkap
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = focusModel?.title ?: "Unknown Focus",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = focusModel?.category ?: "No Category",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                // Tambahkan informasi durasi
                focusModel?.let { focus ->
                    Text(
                        text = "${focus.focusDuration}m focus • ${focus.restDuration}m rest",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatDate(session.startTime),
                    color = Color.White,
                    fontSize = 12.sp
                )

                // Tampilkan durasi session jika ada endTime
                session.endTime?.let { endTime ->
                    val duration = (endTime - session.startTime) / 1000 / 60 // dalam menit
                    Text(
                        text = "${duration}m session",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = when {
                        session.isCompleted -> "Completed"
                        session.endTime != null -> "Finished"
                        else -> "Incomplete"
                    },
                    color = when {
                        session.isCompleted -> Color.Green
                        session.endTime != null -> Color.Yellow
                        else -> Color.Red
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM - HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}