package com.countertap.customer.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.CardBackground
import com.countertap.customer.ui.theme.CardElevated
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.shared.Product
import kotlin.math.abs

private val avatarPalette = listOf(
    Color(0xFF5C6BC0), Color(0xFF26A69A), Color(0xFFEF5350),
    Color(0xFFFF7043), Color(0xFF66BB6A), Color(0xFFAB47BC),
    Color(0xFF26C6DA), Color(0xFF8D6E63), Color(0xFF78909C), Color(0xFFF4511E)
)

private fun avatarColor(name: String): Color =
    avatarPalette[abs(name.hashCode()) % avatarPalette.size]

@Composable
fun MenuProductCard(
    product: Product,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onOpenPicker: () -> Unit = {},
    isShopOpen: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hasOptions = product.optionGroups.isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(AccentBlue.copy(alpha = 0.7f))
        )

        Box(
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(avatarColor(product.name)),
            contentAlignment = Alignment.Center
        ) {
            if (product.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = product.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (product.description.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "₹${product.price.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                }
                if (hasOptions) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CardElevated)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Customisable",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        if (isShopOpen) {
            MenuProductActions(
                hasOptions = hasOptions,
                quantity = quantity,
                onAdd = onAdd,
                onRemove = onRemove,
                onOpenPicker = onOpenPicker
            )
        }

        Spacer(Modifier.width(10.dp))
    }
}

@Composable
private fun MenuProductActions(
    hasOptions: Boolean,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onOpenPicker: () -> Unit
) {
    if (hasOptions) {
        if (quantity == 0) {
            Button(
                onClick = onOpenPicker,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("ADD", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentBlue.copy(alpha = 0.15f))
            ) {
                Text(
                    text = "$quantity in cart",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue,
                    modifier = Modifier.padding(start = 10.dp)
                )
                IconButton(onClick = onOpenPicker, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add more", tint = AccentBlue, modifier = Modifier.size(16.dp))
                }
            }
        }
    } else {
        if (quantity == 0) {
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("ADD", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardElevated)
            ) {
                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove", tint = AccentBlue, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = quantity.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                IconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentBlue, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
