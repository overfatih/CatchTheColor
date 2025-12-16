package com.profplay.catchthecorrectcolor.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.profplay.catchthecorrectcolor.model.Agent
import com.profplay.catchthecorrectcolor.model.AppSettings
import com.profplay.catchthecorrectcolor.viewmodel.GameViewModel
import java.io.InputStreamReader

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onStartNewSim: () -> Unit,      // Monte Carlo
    onLoadJson: () -> Unit,         // (Boş kalabilir, içeride hallediyoruz)
    onSingleAgentTest: () -> Unit,  // Stres Testi
    onSettings: () -> Unit,
    onStartHumanGame: () -> Unit    // İnsan Oyunu
) {
    val context = LocalContext.current

    // JSON YÜKLEYİCİ (Burada tanımlı olması şart)
    val jsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<Agent>>() {}.type
                val agents: List<Agent> = Gson().fromJson(reader, type)

                // Veriyi Global Ayarlara Yükle
                AppSettings.loadedPopulation = agents

                // ViewModel'i de haberdar edelim (Opsiyonel ama iyi olur)
                // viewModel.setPopulation(agents)

                Toast.makeText(context, "✅ ${agents.size} Ajan Başarıyla Yüklendi!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "❌ Hata: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🧬 AI LAB: Tez Simülasyonu", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        // 1. KENDİN OYNA (Kontrol Grubu)
        OutlinedButton(
            onClick = onStartHumanGame,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.VideogameAsset, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kendin Oyna (İnsan Modu)")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // 2. VERİ YÜKLEME
        Button(
            onClick = {
                // Launcher'ı doğrudan burada tetikliyoruz
                jsonLauncher.launch("application/json")
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            // UploadFile ikonu yoksa Share kullan, sorun değil
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Norm Verisi Yükle (.json)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. SİMÜLASYON BAŞLAT (Monte Carlo)
        Button(
            onClick = onStartNewSim,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Monte Carlo Simülasyonu")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. STRES TESTİ
        Button(
            onClick = onSingleAgentTest,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Science, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tekil Ajan Stres Testi")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. AYARLAR
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Text("Parametre Ayarları")
        }
    }
}