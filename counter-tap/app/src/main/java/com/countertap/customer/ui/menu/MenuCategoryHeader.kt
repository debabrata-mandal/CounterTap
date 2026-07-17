package com.countertap.customer.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.CardBackground
import com.countertap.customer.ui.theme.TextHint
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.shared.categoryEmoji
import kotlin.math.abs

private val headerPalette = listOf(
    Color(0xFF5C6BC0), Color(0xFF26A69A), Color(0xFFEF5350),
    Color(0xFFFF7043), Color(0xFF66BB6A), Color(0xFFAB47BC),
    Color(0xFF26C6DA), Color(0xFF8D6E63), Color(0xFF78909C), Color(0xFFF4511E)
)

private fun headerColor(name: String): Color =
    headerPalette[abs(name.hashCode()) % headerPalette.size]

@Composable
fun MenuCategoryHeader(
    name: String,
    count: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val color = headerColor(name)
    val emoji = categoryEmoji(name)

    if (compact) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 24.dp, bottom = 10.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentBlue)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = name.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                letterSpacing = 1.5.sp
            )
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.12f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$count item${if (count != 1) "s" else ""}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Text(
            text = emoji,
            fontSize = 22.sp,
            color = TextHint.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun MenuBookChapterHeader(
    name: String,
    count: Int,
    pageIndex: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    val color = headerColor(name)
    val emoji = categoryEmoji(name)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.radialGradient(
                        listOf(color.copy(alpha = 0.40f), color.copy(alpha = 0.10f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 42.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = name,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentBlue.copy(alpha = 0.6f))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$count item${if (count != 1) "s" else ""}  ·  $pageIndex of $pageCount",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}
