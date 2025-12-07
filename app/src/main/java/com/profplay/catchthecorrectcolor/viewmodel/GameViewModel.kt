package com.profplay.catchthecorrectcolor.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// isPlaying: Oyun şu an aktif mi? (Kronometre çalışıyor mu?)
data class GameState(
    val score: Double = 0.0,
    val puan: Double = 0.0,
    val level: Int = 1,
    val elapsedTime: Double = 0.0,
    val timeLeftMs: Long = 0,
    val targetColorHex: Int = 0,
    val targetColorName: String = "Hazır mısın?", // Başlangıç yazısı
    val buttonColors: List<Int> = emptyList(),
    val isGameOver: Boolean = false,
    val isPlaying: Boolean = false, // YENİ: Oyun başladı mı?
    val gameOverMessage: String = ""
)

class GameViewModel : ViewModel() {

    private val _gameState = MutableLiveData(GameState())
    val gameState: LiveData<GameState> = _gameState

    private var timer: CountDownTimer? = null

    private val myColors = mapOf(
        1 to Pair(android.graphics.Color.RED, "RED"),
        2 to Pair(android.graphics.Color.BLUE, "BLUE"),
        3 to Pair(android.graphics.Color.YELLOW, "YELLOW"),
        4 to Pair(android.graphics.Color.GREEN, "GREEN")
    )

    // Başlangıç renkleri (Gri tonları olabilir veya boş)
    private val initialColors = listOf(
        android.graphics.Color.LTGRAY, android.graphics.Color.LTGRAY,
        android.graphics.Color.LTGRAY, android.graphics.Color.LTGRAY
    )

    init {
        // Uygulama ilk açıldığında "Boş/Bekleme" durumu oluştur
        _gameState.value = GameState(
            buttonColors = initialColors,
            isPlaying = false,
            targetColorName = "START"
        )
    }

    private var currentNumber: Double = 0.0
    private var bestScore: Double? = null
    private var currentLevel: Int = 1
    private var currentPuan: Double = 0.0

    fun startGame() {
        currentNumber = 0.0
        bestScore = null
        currentLevel = 1
        currentPuan = 0.0

        startLevelAction()
    }

    private fun startLevelAction() {
        val randomKey = (1..4).random()
        val targetPair = myColors[randomKey]!!
        val targetColorInt = targetPair.first
        val targetName = targetPair.second

        val shuffledKeys = listOf(1, 2, 3, 4).shuffled()
        val buttonColors = shuffledKeys.map { myColors[it]!!.first }

        val limitMs = (300 - currentLevel) * 10L

        _gameState.value = GameState(
            score = bestScore ?: 0.0,
            puan = currentPuan,
            level = currentLevel,
            targetColorHex = targetColorInt,
            targetColorName = targetName,
            buttonColors = buttonColors,
            isGameOver = false,
            isPlaying = true // YENİ: Oyun başladı, butonu gizle
        )

        startTimer(limitMs)
    }

    fun processMove(selectedColorInt: Int) {
        val currentState = _gameState.value ?: return
        // Oyun oynamıyorsa (isPlaying = false) tıklamaları yoksay
        if (!currentState.isPlaying || currentState.isGameOver) return

        if (selectedColorInt == currentState.targetColorHex) {
            stopTimer()
            calculateScoreAndLevelUp()
        } else {
            stopTimer()
            endGame("Yanlış Cevap")
        }
    }

    // ... calculateScoreAndLevelUp AYNI KALSIN ...
    private fun calculateScoreAndLevelUp() {
        val reactionTime = currentNumber / 100.0

        if (bestScore == null) {
            bestScore = reactionTime
        } else {
            bestScore = ((bestScore!! * (currentLevel - 1)) + reactionTime) / currentLevel
        }

        if (bestScore != 0.0) {
            currentPuan = currentLevel * (1.0 / bestScore!!)
        }

        currentLevel += 1
        currentNumber = 0.0
        startLevelAction()
    }

    // ... startTimer AYNI KALSIN ...
    private fun startTimer(limitMs: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(limitMs, 10) {
            override fun onTick(millisUntilFinished: Long) {
                currentNumber += 1.0
                _gameState.value = _gameState.value?.copy(
                    elapsedTime = currentNumber,
                    timeLeftMs = millisUntilFinished
                )
            }

            override fun onFinish() {
                endGame("Süre Doldu!")
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    private fun endGame(message: String) {
        val current = _gameState.value ?: return
        _gameState.value = current.copy(
            isGameOver = true,
            isPlaying = false, // YENİ: Oyun bitti, buton geri gelsin
            gameOverMessage = message
        )
    }

    // ... onCleared AYNI KALSIN ...
    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}