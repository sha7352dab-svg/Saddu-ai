package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantMode
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHigh

@Composable
fun ModeSelectorChips(
    selectedMode: AssistantMode,
    onSelectMode: (AssistantMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistantMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            val icon = getModeIcon(mode)

            val chipBg = if (isSelected) SurfaceContainerHigh else SurfaceContainerDark
            val chipBorder = if (isSelected) CyanNeon else SurfaceBorderDark
            val chipColor = if (isSelected) CyanNeon else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(chipBg)
                    .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
                    .clickable { onSelectMode(mode) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("mode_chip_${mode.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = mode.title,
                        tint = chipColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            color = chipColor
                        )
                    )
                }
            }
        }
    }
}

private fun getModeIcon(mode: AssistantMode): ImageVector {
    return when (mode) {
        AssistantMode.ALL_ROUNDER -> Icons.Default.Bolt
        AssistantMode.ANIMATION -> Icons.Default.Brush
        AssistantMode.CODING -> Icons.Default.Code
        AssistantMode.CREATIVE -> Icons.Default.Brush
        AssistantMode.LOGIC_MATH -> Icons.Default.Calculate
        AssistantMode.HINGLISH_DOST -> Icons.Default.Favorite
    }
}
