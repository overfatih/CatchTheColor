package com.profplay.catchthecorrectcolor.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profplay.catchthecorrectcolor.model.AppSettings

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // Config'i State'e kopyalıyoruz (Anlık değişimleri görmek için)
    // remember { mutableStateOf(...) } kullandık ki ekran dönünce sıfırlanmasın
    var config by remember { mutableStateOf(AppSettings.currentConfig.copy()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("⚙️ Parametre Ayarları", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // --- HIZ AYARI ---
        SettingSlider(
            title = "Min Hız (ms): ${config.minSpeed.toInt()}",
            value = config.minSpeed.toFloat(),
            range = 100f..1000f,
            onValueChange = { config = config.copy(minSpeed = it.toDouble()) }
        )

        // --- HATA ORANI ---
        SettingSlider(
            title = "Hata Oranı: %.3f".format(config.baseErrorRate),
            value = config.baseErrorRate.toFloat(),
            range = 0.0f..0.2f,
            onValueChange = { config = config.copy(baseErrorRate = it.toDouble()) }
        )

        // --- SEBAT (GRIT) ---
        SettingSlider(
            title = "Sebat (Grit): %.2f".format(config.baseGrit),
            value = config.baseGrit.toFloat(),
            range = 0.0f..1.0f,
            onValueChange = { config = config.copy(baseGrit = it.toDouble()) }
        )

        // --- ODAK (FOCUS) ---
        SettingSlider(
            title = "Odak (Focus): %.2f".format(config.baseFocus),
            value = config.baseFocus.toFloat(),
            range = 0.0f..1.0f,
            onValueChange = { config = config.copy(baseFocus = it.toDouble()) }
        )

        // --- SIKILMA EŞİĞİ ---
        SettingSlider(
            title = "Sıkılma Eşiği: ${config.baseBoredom.toInt()}",
            value = config.baseBoredom.toFloat(),
            range = 10f..100f,
            onValueChange = { config = config.copy(baseBoredom = it.toDouble()) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // KAYDET BUTONU
        Button(
            onClick = {
                AppSettings.currentConfig = config // Global ayarları güncelle
                onBack() // Geri dön
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Kaydet ve Dön")
        }
    }
}

// Yardımcı Composable (Slider Tasarımı)
@Composable
fun SettingSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(title, fontWeight = FontWeight.SemiBold)
        Slider(value = value, valueRange = range, onValueChange = onValueChange)
        Spacer(modifier = Modifier.height(8.dp))
    }
}