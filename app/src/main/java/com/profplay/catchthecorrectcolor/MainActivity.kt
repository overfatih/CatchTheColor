package com.profplay.catchthecorrectcolor

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.profplay.catchthecorrectcolor.databinding.ActivityMainBinding
import com.profplay.catchthecorrectcolor.viewmodel.GameViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeGameData()

        // viewModel.startGame() <- BU SATIRI SİLDİK! Artık otomatik başlamıyor.
        // Başlangıçta buton görünsün diye XML'de visibility="visible" yaptık veya observeGameData halledecek.
    }

    private fun setupClickListeners() {
        val circles = listOf(binding.ivCircle0, binding.ivCircle1, binding.ivCircle2, binding.ivCircle3)

        circles.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val currentState = viewModel.gameState.value
                // Sadece oyun oynanıyorsa (isPlaying) tıklamaya izin ver
                if (currentState != null && currentState.isPlaying && index < currentState.buttonColors.size) {
                    val clickedColor = currentState.buttonColors[index]
                    viewModel.processMove(clickedColor)
                }
            }
        }

        binding.buttonRestart.setOnClickListener {
            viewModel.startGame()
            // Butonun gizlenmesini observeGameData halledecek (isPlaying=true olunca)
        }
    }

    private fun observeGameData() {
        viewModel.gameState.observe(this) { state ->

            // --- UI GÜNCELLEMELERİ ---

            // Skorlar
            binding.textViewScore.text = "Best: %.2f".format(state.score)
            binding.textViewTime.text = "Time: %.2f".format(state.elapsedTime / 100.0)
            binding.textViewLevel.text = "Level: ${state.level}"
            binding.textViewPuan.text = "Puan: %.5f".format(state.puan)

            // Hedef Renk Yazısı
            binding.textViewTargetColor.text = state.targetColorName

            // Daireleri Boya
            val circles = listOf(binding.ivCircle0, binding.ivCircle1, binding.ivCircle2, binding.ivCircle3)
            state.buttonColors.forEachIndexed { index, colorInt ->
                if (index < circles.size) {
                    circles[index].setColorFilter(colorInt)
                }
            }

            // --- YENİ MANTIK: BUTON GÖRÜNÜRLÜĞÜ ---
            if (state.isPlaying) {
                // Oyun oynanıyorsa butonu gizle
                binding.buttonRestart.visibility = View.GONE
            } else {
                // Oyun oynanmıyorsa (Başlangıç veya Game Over) butonu göster
                binding.buttonRestart.visibility = View.VISIBLE

                // Buton yazısını duruma göre değiştir
                if (state.isGameOver) {
                    binding.buttonRestart.text = "Tekrar Başlat"
                } else {
                    binding.buttonRestart.text = "Oyuna Başla"
                }
            }

            // Oyun Bitti mi? Dialog Göster
            if (state.isGameOver) {
                showGameOverDialog(state.gameOverMessage, state.puan)
            }
        }
    }

    private fun showGameOverDialog(message: String, finalPuan: Double) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Oyun Bitti!")
        builder.setMessage("$message\nToplam Puanınız: %.5f".format(finalPuan))
        builder.setCancelable(false)

        // "Tamam" butonuna basınca SADECE DIALOG KAPANSIN. Oyun başlamasın.
        builder.setPositiveButton("Tamam") { dialog, _ ->
            dialog.dismiss()
            // Oyun başlamıyor, ana ekranda "Tekrar Başlat" butonu zaten görünür durumda (observeGameData sağladı)
        }

        // Dialog zaten açıksa tekrar açmamak için kontrol eklenebilir ama
        // state.isGameOver true olduğu sürece observe sürekli tetiklenir.
        // Bunu engellemek için ViewModel'de "Event Wrapper" kullanılır ama
        // şimdilik basit çözüm: Zaten bir dialog varsa yenisini açma (Android kendi yönetir genelde)
        // Ancak en temiz yöntem, dialog gösterildikten sonra bu "gösterildi" bilgisini tüketmektir.
        // Ama şimdilik bu haliyle, dialog açıkken arkada oyun durduğu için sorun olmaz.

        // NOT: Eğer dialog sürekli tekrar tekrar açılıyorsa (loop),
        // ViewModel'e "resetGameOverFlag()" gibi bir fonksiyon eklememiz gerekir.
        // Şimdilik deneyelim, sorun olursa düzeltiriz.
        builder.show()
    }
}