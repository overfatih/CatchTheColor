package com.profplay.catchthecorrectcolor.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.profplay.catchthecorrectcolor.model.GameState

class GameViewModel : ViewModel() {

    // UI'ın gözlemleyeceği (Observe edeceği) canlı veri
    private val _gameState = MutableLiveData(GameState())
    val gameState: LiveData<GameState> = _gameState

    private var timer: CountDownTimer? = null

    // Oyun parametreleri (İleride zorluk ayarı için dinamik olacak)
    private val initialTime = 10000L // 10 saniye
    private val timePenaltyStep = 500L // Her seviyede 0.5sn azalsın

    // Renk havuzu (Basit tuttum, genişletebiliriz)
    private val colorsMap = mapOf(
        "Red" to "#FF0000",
        "Green" to "#00FF00",
        "Blue" to "#0000FF",
        "Yellow" to "#FFFF00",
        "Black" to "#000000",
        "White" to "#FFFFFF",
        "Cyan" to "#00FFFF",
        "Magenta" to "#FF00FF"
    )

    // Oyunu Başlat
    fun startGame() {
        _gameState.value = GameState(
            timeLeftMs = initialTime,
            score = 0,
            level = 1,
            isGameOver = false
        )
        generateNextLevel()
        startTimer(initialTime)
    }

    // Kullanıcı (veya AI) bir renk seçtiğinde çağrılır
    fun processMove(selectedColorHex: String) {
        val currentState = _gameState.value ?: return
        if (currentState.isGameOver) return

        if (selectedColorHex == currentState.targetColorHex) {
            // DOĞRU CEVAP
            val nextLevel = currentState.level + 1
            // Seviye arttıkça süre azalsın (Zorluk)
            val newTime = (initialTime - (nextLevel * timePenaltyStep)).coerceAtLeast(1000L) // Min 1sn

            _gameState.value = currentState.copy(
                score = currentState.score + 1,
                level = nextLevel,
                timeLeftMs = newTime
            )
            generateNextLevel()
            startTimer(newTime) // Süreyi resetle
        } else {
            // YANLIŞ CEVAP -> OYUN BİTER
            endGame()
        }
    }

    private fun generateNextLevel() {
        val keys = colorsMap.keys.toList()
        // Hedef rengi rastgele seç
        val targetKey = keys.random()
        val targetHex = colorsMap[targetKey] ?: "#FFFFFF"

        // Şıkları karıştır (Biri kesinlikle hedef renk olmalı)
        val options = mutableListOf<String>()
        options.add(targetHex)
        // Kalan 3 şıkkı rastgele doldur
        while (options.size < 4) {
            val randomKey = keys.random()
            val randomHex = colorsMap[randomKey] ?: "#000000"
            if (!options.contains(randomHex)) {
                options.add(randomHex)
            }
        }

        // Mevcut durumu güncelle
        val current = _gameState.value ?: GameState()
        _gameState.value = current.copy(
            targetColorName = targetKey,
            targetColorHex = targetHex,
            buttonColors = options.shuffled() // Şıkların yerini karıştır
        )
    }

    private fun startTimer(durationMs: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(durationMs, 10) { // 10ms'de bir güncelle (Akıcı görünmesi için)
            override fun onTick(millisUntilFinished: Long) {
                val current = _gameState.value ?: return
                _gameState.value = current.copy(timeLeftMs = millisUntilFinished)
            }

            override fun onFinish() {
                endGame()
            }
        }.start()
    }

    private fun endGame() {
        timer?.cancel()
        val current = _gameState.value ?: return
        _gameState.value = current.copy(
            isGameOver = true,
            timeLeftMs = 0
        )
    }

    // ViewModel ölünce timer'ı durdur (Memory Leak önlemi)
    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}