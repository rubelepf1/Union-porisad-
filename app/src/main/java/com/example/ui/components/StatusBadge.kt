package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Citizen
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusCollecting
import com.example.ui.theme.StatusComplete
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusIncomplete
import com.example.ui.theme.StatusNew
import com.example.ui.theme.StatusSecretaryReady
import com.example.ui.theme.StatusUpazilaSent

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        Citizen.STATUS_NEW -> Pair(StatusNew.copy(alpha = 0.15f), StatusNew)
        Citizen.STATUS_COLLECTING -> Pair(StatusCollecting.copy(alpha = 0.15f), StatusCollecting)
        Citizen.STATUS_INCOMPLETE -> Pair(StatusIncomplete.copy(alpha = 0.15f), StatusIncomplete)
        Citizen.STATUS_COMPLETE -> Pair(StatusComplete.copy(alpha = 0.15f), StatusComplete)
        Citizen.STATUS_READY_SECRETARY -> Pair(StatusSecretaryReady.copy(alpha = 0.15f), StatusSecretaryReady)
        Citizen.STATUS_SENT_UPAZILA -> Pair(StatusUpazilaSent.copy(alpha = 0.15f), StatusUpazilaSent)
        Citizen.STATUS_DONE -> Pair(StatusDone.copy(alpha = 0.15f), StatusDone)
        Citizen.STATUS_CANCELLED -> Pair(StatusCancelled.copy(alpha = 0.15f), StatusCancelled)
        else -> Pair(Color.Gray.copy(alpha = 0.15f), Color.DarkGray)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
