package com.countertap.business.ui.qr

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.ui.theme.AccentBlueLight
import com.countertap.business.ui.theme.BackgroundDark
import com.countertap.business.ui.theme.CardBackground
import com.countertap.business.ui.theme.SuccessGreen
import com.countertap.business.ui.theme.SurfaceColor
import com.countertap.business.ui.theme.TextPrimary
import com.countertap.business.ui.theme.TextSecondary
import com.countertap.business.ui.theme.WarningOrange
import com.countertap.business.viewmodel.MenuViewModel
import com.countertap.business.viewmodel.TenantState
import com.countertap.business.viewmodel.TenantViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File

@Composable
fun QrCodeScreen(
    tenantViewModel: TenantViewModel = hiltViewModel(),
    menuViewModel: MenuViewModel = hiltViewModel()
) {
    val tenantState by tenantViewModel.tenantState.collectAsState()
    val menuState by menuViewModel.uiState.collectAsState()
    val shop = (tenantState as? TenantState.Ready)?.shop
    val context = LocalContext.current

    val qrBitmap = remember(shop?.id) {
        shop?.id?.let { generateQrBitmap(it, 512, moduleColor = 0xFFFFFFFF.toInt(), bgColor = 0xFF101827.toInt()) }
    }

    val productCount = menuState.products.size
    val categoryCount = menuState.categories.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "QR Code",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Share with customers to start receiving orders",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // QR card — dark themed, consistent with app
        if (shop != null && qrBitmap != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Shop identity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = shop.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (shop.address.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = shop.address,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = AccentBlue.copy(alpha = 0.20f))
                    Spacer(Modifier.height(18.dp))

                    // QR code — white modules on dark bg, matches theme
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Scan to browse menu & place orders",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = AccentBlue.copy(alpha = 0.20f))
                    Spacer(Modifier.height(10.dp))

                    // Branding strip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Powered by ",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "CounterTap",
                            fontSize = 11.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Inventory2,
                    value = "$productCount",
                    label = "Items"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.GridView,
                    value = "$categoryCount",
                    label = "Categories"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    value = if (shop.active) "Open" else "Paused",
                    label = "Status",
                    valueColor = if (shop.active) SuccessGreen else WarningOrange
                )
            }

            // Shop details card
            val hasDetails = shop.phone.isNotBlank() || shop.upiId.isNotBlank()
            if (hasDetails) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SHOP DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        if (shop.phone.isNotBlank()) {
                            InfoRow(icon = Icons.Default.Phone, text = shop.phone)
                        }
                        if (shop.upiId.isNotBlank()) {
                            InfoRow(icon = Icons.Default.Payment, text = shop.upiId)
                        }
                    }
                }
            }

            // Tip card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentBlue.copy(alpha = 0.10f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = AccentBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Print this QR and place it on your counter so customers can scan easily.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Share button
            Button(
                onClick = { shareQrCode(context, qrBitmap, shop.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Share QR Code",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    AccentBlue.copy(alpha = 0.12f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentBlueLight,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}

private fun generateQrBitmap(
    content: String,
    size: Int,
    moduleColor: Int = Color.BLACK,
    bgColor: Int = Color.WHITE
): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bits[x, y]) moduleColor else bgColor)
        }
    }
    return bitmap
}

private fun shareQrCode(context: android.content.Context, bitmap: Bitmap, shopName: String) {
    val file = File(context.cacheDir, "qr_code.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Scan this QR code to order from $shopName on CounterTap")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share QR Code"))
}
