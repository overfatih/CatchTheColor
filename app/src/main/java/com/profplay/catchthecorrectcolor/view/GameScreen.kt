package com.profplay.catchthecorrectcolor.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profplay.catchthecorrectcolor.model.GameState
import com.profplay.catchthecorrectcolor.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel, onBackToMenu: () -> Unit) {
    // ViewModel'deki LiveData'yı dinle (State güncellendikçe ekran çizilir)
    val gameState by viewModel.gameState.observeAsState()
    val state = gameState ?: return // Null ise çizme

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- ÜST BİLGİ PANELİ ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Level: ${state.level}", style = MaterialTheme.typography.titleLarge)
            Text("Time: ${"%.2f".format(state.elapsedTime / 100.0)}s", style = MaterialTheme.typography.titleLarge)
        }

        Text(
            text = "Score: ${"%.2f".format(state.score)}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- HEDEF RENK ---
        Text("HEDEF RENK:", style = MaterialTheme.typography.labelLarge)
        Text(
            text = state.targetColorName,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- OYUN ALANI (GRID) ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                GameCircle(0, state, viewModel)
                Spacer(modifier = Modifier.width(24.dp))
                GameCircle(1, state, viewModel)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                GameCircle(2, state, viewModel)
                Spacer(modifier = Modifier.width(24.dp))
                GameCircle(3, state, viewModel)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- KONTROL BUTONLARI ---
        if (state.targetColorName == "ANALİZ TAMAMLANDI" && state.isGameOver) {
            val context = androidx.compose.ui.platform.LocalContext.current

            Button(
                onClick = { viewModel.saveSimulationToDownloads(context) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Turuncu renk
            ) {
                Text("💾 SONUÇLARI JSON OLARAK İNDİR")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (!state.isPlaying) {
            Button(
                onClick = { viewModel.startGame() }, // Burası Monte Carlo'yu yeniden başlatmaz, insan oyununa döner
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(if (state.isGameOver) "TEKRAR OYNA / MENÜYE DÖN" else "BAŞLAT")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Menüye Dön")
            }
        }

        // --- GAME OVER DIALOG ---
        if (state.isGameOver && !state.isPlaying) {
            // Eğer başlıkta "ANALİZ" veya "TEST" yazıyorsa bu bir simülasyondur.
            val isSimulation = state.targetColorName.contains("ANALİZ") || state.targetColorName.contains("TEST") || state.targetColorName.contains("BİTTİ")

            // GAME OVER DIALOG (Sadece İNSAN oynarken çıksın)
            // Eğer simülasyonsa dialog çıkmasın, böylece Kaydet butonuna basabilirsin.
            if (state.isGameOver && !state.isPlaying && !isSimulation) {
                AlertDialog(
                    onDismissRequest = { /* Kapatılamaz */ },
                    title = { Text("Oyun Bitti!") },
                    text = { Text("${state.gameOverMessage}\nToplam Puan: ${"%.5f".format(state.puan)}") },
                    confirmButton = {
                        Button(onClick = { viewModel.startGame() }) {
                            Text("Tekrar Dene")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onBackToMenu) {
                            Text("Çıkış")
                        }
                    }
                )
            }
        }
    }
}

// Yardımcı Composable (Daire)
@Composable
fun GameCircle(index: Int, state: GameState, viewModel: GameViewModel) {
    val colorInt = if (index < state.buttonColors.size) state.buttonColors[index] else android.graphics.Color.GRAY

    // Eğer "isPlaying" true ise AMA "Simülasyon" modundaysak tıklamayı kapatmalıyız.
    // Bunu anlamanın basit yolu: Simülasyon çok hızlıdır, tıklamaya yetişemezsin :)
    // Veya targetColorName içinde "ANALİZ" kelimesi geçiyorsa tıklaMA.

    val isSimulationRunning = state.targetColorName.contains("ANALİZ") || state.targetColorName.contains("TEST")

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color(colorInt))
            .border(4.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
            .clickable(enabled = state.isPlaying && !isSimulationRunning) {
                // Sadece İNSAN oynarken ve simülasyon değilken tıkla
                viewModel.processMove(colorInt)
            }
    )
}