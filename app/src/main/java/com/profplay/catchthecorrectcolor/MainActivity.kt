package com.profplay.catchthecorrectcolor

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.profplay.catchthecorrectcolor.databinding.ActivityMainBinding
import com.profplay.catchthecorrectcolor.viewmodel.GameViewModel

class MainActivity : AppCompatActivity() {

    // ViewBinding kullanıyorsan (Tavsiye ederim):
    private lateinit var binding: ActivityMainBinding

    // ViewModel'i delegate ile bağla
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeGameData()

        // Oyunu başlat
        viewModel.startGame()
    }

    private fun setupClickListeners() {
        // Artık Button değil, ImageView (ivCircle) kullanıyoruz
        val circles = listOf(binding.ivCircle0, binding.ivCircle1, binding.ivCircle2, binding.ivCircle3)

        circles.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val currentState = viewModel.gameState.value
                if (currentState != null && index < currentState.buttonColors.size) {
                    val clickedColor = currentState.buttonColors[index]
                    viewModel.processMove(clickedColor)
                }
            }
        }

        // Restart butonu (XML'e ekledik)
        binding.buttonRestart.setOnClickListener {
            viewModel.startGame()
            binding.buttonRestart.visibility = android.view.View.GONE // Oyuna başlayınca gizle
        }
    }

    private fun observeGameData() {
        viewModel.gameState.observe(this) { state ->

            // Skor, Seviye, Süre, Hedef Yazı aynen kalıyor...
            binding.textViewScore.text = "Score: ${state.score} (Lvl: ${state.level})"
            binding.textViewTime.text = "Time: ${"%.1f".format(state.timeLeftMs / 1000.0)}s" // Virgülden sonra tek hane
            binding.textViewTargetColor.text = state.targetColorName
            binding.textViewTargetColor.setTextColor(Color.parseColor(state.targetColorHex))

            // --- DEĞİŞEN KISIM: RESİMLERİ BOYAMA ---
            val circles = listOf(binding.ivCircle0, binding.ivCircle1, binding.ivCircle2, binding.ivCircle3)

            state.buttonColors.forEachIndexed { index, colorHex ->
                if (index < circles.size) {
                    // Daire resminin üzerine renk filtresi atıyoruz
                    circles[index].setColorFilter(Color.parseColor(colorHex))
                }
            }

            // Oyun Bitti mi?
            if (state.isGameOver) {
                Toast.makeText(this, "Game Over! Final Score: ${state.score}", Toast.LENGTH_SHORT).show()
                binding.buttonRestart.visibility = android.view.View.VISIBLE // Butonu göster
            }
        }
    }
}