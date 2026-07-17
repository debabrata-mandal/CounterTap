package com.countertap.customer.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.CardElevated
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary

@Composable
fun MenuDisplayModeToggle(
    currentMode: MenuDisplayMode,
    onModeSelected: (MenuDisplayMode) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    Column(modifier = modifier) {
        if (showLabel) {
            Text(
                text = "Menu layout",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardElevated)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MenuDisplayModeOption(
                label = "Scroll",
                icon = Icons.AutoMirrored.Filled.ViewList,
                selected = currentMode == MenuDisplayMode.Scroll,
                onClick = { onModeSelected(MenuDisplayMode.Scroll) },
                modifier = Modifier.weight(1f)
            )
            MenuDisplayModeOption(
                label = "Book",
                icon = Icons.Default.AutoStories,
                selected = currentMode == MenuDisplayMode.Book,
                onClick = { onModeSelected(MenuDisplayMode.Book) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MenuDisplayModeOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AccentBlue else CardElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) TextPrimary else TextSecondary,
            modifier = Modifier
                .size(18.dp)
                .padding(end = 6.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) TextPrimary else TextSecondary
        )
    }
}
