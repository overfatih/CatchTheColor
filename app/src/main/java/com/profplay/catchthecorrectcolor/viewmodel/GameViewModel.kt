package com.profplay.catchthecorrectcolor.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.profplay.catchthecorrectcolor.model.AIProfile
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import com.google.gson.Gson // JSON dönüşümü için
import kotlin.math.max
import kotlin.math.min

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
    private val javaRandom = Random()
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

    // --- SİMÜLASYON BÖLÜMÜ ---

    // Gifted: Hızlı, Odaklı, Azimli (Pes etmez), Sıkılmaya meyilli
    val giftedProfile = AIProfile(
        typeName = "Gifted-Like",
        baseReflexTime = 250.0,
        focusStability = 1.5,
        noiseResistance = 0.1,
        errorProneFactor = 0.01,
        // YENİ ÖZELLİKLER:
        grit = 0.9,             // Çok Hırslı/Azimli (Pes etmez)
        boredomThreshold = 0.8, // DİKKAT: Zeki olduğu için çabuk sıkılabilir (Eşik yüksekse çabuk sıkılır mantığı kurduysak)
        // Kodda: motivation -= wins * threshold demiştik. Yüksek = Çabuk sıkılır.
        fatigueRate = 0.3       // Az yorulur (Mental kapasite yüksek)
    )

    // Average: Ortalama hız, Ortalama azim
    val averageProfile = AIProfile(
        typeName = "Typical",
        baseReflexTime = 350.0,
        focusStability = 1.0,
        noiseResistance = 0.5,
        errorProneFactor = 0.05,
        // YENİ ÖZELLİKLER:
        grit = 0.5,             // Ortalama Azim
        boredomThreshold = 0.2, // Kolay kolay sıkılmaz (Rutin işi sever)
        fatigueRate = 0.5       // Normal yorulur
    )
    /**
     * Headless Simülasyonu Başlat
     * Ekransız, animasyonsuz, matematiksel hızda oyun.
     */
    /**
     * Headless Simülasyonu Başlat (Coroutine Destekli)
     */
    fun runSimulation(profile: AIProfile, noiseLevel: Double) {
        // Bu işlem UI thread'i kilitlemesin diye Coroutine başlatıyoruz
        viewModelScope.launch {

            // ADIM 1: Önce UI'a "Oyun Başladı" sinyali gönder (Butonları gizlemesi için)
            val currentState = _gameState.value ?: GameState()
            _gameState.value = currentState.copy(
                isPlaying = true, // BUTONLAR GİZLENSİN
                targetColorName = "SİMÜLASYON HESAPLANIYOR...",
                isGameOver = false
            )

            // ADIM 2: Çok kısa bir bekleme (UI'ın güncellenmesine fırsat ver)
            delay(100)

            // --- HESAPLAMA BAŞLIYOR ---
            var simLevel = 1
            var simScore = 0.0
            var simPuan = 0.0
            var simTotalTime = 0.0
            var isSimGameOver = false

            val aiPlayer = AIPlayer(profile)

            while (!isSimGameOver) {
                // Ajan Hamlesini Yapsın
                val moveResult = aiPlayer.makeMove(simLevel, noiseLevel)

                // Süre Limiti Hesabı
                val timeLimitMs = (300 - simLevel) * 10.0

                if (moveResult.isCorrect && moveResult.reactionTimeMs < timeLimitMs) {
                    val reactionTimeRaw = moveResult.reactionTimeMs / 10.0
                    val reactionTimeSec = reactionTimeRaw / 100.0

                    if (simLevel == 1) {
                        simScore = reactionTimeSec
                    } else {
                        simScore = ((simScore * (simLevel - 1)) + reactionTimeSec) / simLevel
                    }

                    if (simScore != 0.0) {
                        simPuan = simLevel * (1.0 / simScore)
                    }

                    simLevel++
                    simTotalTime += moveResult.reactionTimeMs
                } else {
                    isSimGameOver = true
                }

                if (simLevel > 1000) isSimGameOver = true
            }
            // --- HESAPLAMA BİTTİ ---

            // ADIM 3: Sonucu Ekrana Bas ve "Oyun Bitti" de (Butonlar geri gelsin)
            _gameState.value = GameState(
                score = simScore,
                puan = simPuan,
                level = simLevel,
                targetColorName = "SİMÜLASYON BİTTİ",
                gameOverMessage = "Ajan: ${profile.typeName}\nGürültü: $noiseLevel\nSeviye: $simLevel",
                isGameOver = true,
                isPlaying = false // BUTONLAR GÖRÜNÜR OLSUN
            )
        }
    }

    /**
     * MONTE CARLO NORM SİMÜLASYONU
     * Evreni temsil eden 10.000 rastgele ajan üretir ve yarıştırır.
     */
    /**
     * GELİŞMİŞ MONTE CARLO (Psycho-Engine Dahil)
     */
    fun runMonteCarloNorming(agentCount: Int = 10000) { // Test için 10bin
        viewModelScope.launch {

            _gameState.value = _gameState.value?.copy(
                isPlaying = true,
                targetColorName = "PSİKOLOJİK SİMÜLASYON...\n($agentCount Ajan)"
            )
            delay(50)

            val records = mutableListOf<SimulationRecord>()
            val populationScores = mutableListOf<Double>()

            for (i in 1..agentCount) {

                // 1. GAUSSIAN RANDOM İLE AJAN YARATMA

                // Bilişsel (Eski)
                var speed = 300.0 + (javaRandom.nextGaussian() * 50.0)
                speed = max(150.0, min(500.0, speed))

                var focus = 2.0 + (javaRandom.nextGaussian() * 0.5)
                focus = max(0.5, min(4.0, focus))

                var error = 0.03 + (javaRandom.nextGaussian() * 0.01)
                error = max(0.001, min(0.1, error))

                // Duyuşsal (YENİ - Karakter Özellikleri)
                // Azim: Ort 0.6, Sapma 0.15
                var grit = 0.6 + (javaRandom.nextGaussian() * 0.15)
                grit = max(0.1, min(1.0, grit)) // 0.1 ile 1.0 arasına sıkıştır

                // Sıkılma: Ort 0.3, Sapma 0.1
                var boredom = 0.3 + (javaRandom.nextGaussian() * 0.1)
                boredom = max(0.0, min(0.8, boredom))

                // Yorulma: Ort 0.5, Sapma 0.1
                var fatigue = 0.5 + (javaRandom.nextGaussian() * 0.1)
                fatigue = max(0.1, min(2.0, fatigue))

                val tempProfile = AIProfile(
                    typeName = "MC-$i",
                    baseReflexTime = speed,
                    focusStability = focus,
                    noiseResistance = 0.0,
                    errorProneFactor = error,
                    grit = grit,           // YENİ
                    boredomThreshold = boredom, // YENİ
                    fatigueRate = fatigue  // YENİ
                )

                // 2. PSİKOLOJİK SİMÜLASYON (Sabit 50 oyun değil, pes edene kadar!)
                val result = runPsychoSimulation(tempProfile)

                // 3. KAYIT
                if (result.avgScore > 0) {
                    populationScores.add(result.avgScore)

                    records.add(SimulationRecord(
                        id = i,
                        inputBaseSpeed = speed,
                        inputFocus = focus,
                        inputGrit = grit,
                        inputBoredom = boredom,
                        // inputFatigue = fatigue,

                        outputAvgScore = result.avgScore,
                        outputTotalGames = result.gamesPlayed,
                        outputQuitReason = result.reason,
                        outputMaxLevel = result.maxLevel
                    ))
                }

                if (i % 100 == 0) {
                    _gameState.value = _gameState.value?.copy(
                        targetColorName = "Analiz: %${(i * 100) / agentCount}"
                    )
                    delay(1)
                }
            }

            // --- ANALİZ VE SONUÇ ---
            // --- ANALİZ KISMI (GameViewModel.kt) ---
            val mean = populationScores.average()
            val variance = populationScores.map { (it - mean) * (it - mean) }.average()
            val stdDev = Math.sqrt(variance)
            val cutOffScore = mean + (2 * stdDev)
            val actualMaxScore = populationScores.maxOrNull() ?: 0.0
            val actualMaxLevel = records.maxOfOrNull { it.outputMaxLevel } ?: 0

            // SAYILARI ÖNCE FORMATLIYORUZ (String'e çeviriyoruz)
            val strMean = "%.2f".format(mean)
            val strStd = "%.2f".format(stdDev)
            val strCutOff = "%.2f".format(cutOffScore)
            val strMaxScore = "%.2f".format(actualMaxScore)

            // JSON Loglama
            val gson = Gson()
            val jsonStr = gson.toJson(records)
            // Logcat'e parça parça bas (Çok uzun olacağı için)
            val chunkSize = 4000
            for (i in 0 until jsonStr.length step chunkSize) {
                android.util.Log.d("MC_JSON", jsonStr.substring(i, min(jsonStr.length, i + chunkSize)))
            }

            _gameState.value = GameState(
                score = actualMaxScore,
                puan = mean,
                level = actualMaxLevel,
                targetColorName = "ANALİZ TAMAMLANDI",

                // ARTIK FORMAT HATASI OLMAZ ÇÜNKÜ HAZIR STRİNGLERİ ($str...) KOYUYORUZ
                gameOverMessage = """
                    Simüle Edilen Ajan: $agentCount
                    Ortalama Puan: $strMean
                    Std Sapma: $strStd
                    -----------------
                    BARAJ (+2SD): $strCutOff
                    EN YÜKSEK SKOR: $strMaxScore
                    EN YÜKSEK LEVEL: $actualMaxLevel
                    -----------------
                    *JSON Logcat'te hazır*
                """.trimIndent(),

                isGameOver = true,
                isPlaying = false
            )
        }
    }

    // Yardımcı Veri Sınıfı
    data class AgentResult(val avgScore: Double, val maxLevel: Int)

    // Gelişmiş "Psycho-Engine" Simülasyonu
    // YENİ: Psikolojik Motor (Psycho-Engine)
    // Ajanın motivasyonu bitene kadar oyun oynatır.
    private fun runPsychoSimulation(profile: AIProfile): SimulationSummary {

        var currentMotivation = 100.0 // Depo
        var totalGames = 0
        var totalScore = 0.0
        val history = mutableListOf<Double>()
        var consecutiveWins = 0
        var quitReason = "LIMIT"
        var globalMaxLevel = 0

        // Max 200 oyun sınırı koyalım (Sonsuz döngü olmasın)
        while (currentMotivation > 0 && totalGames < 200) {

            // 1. Oyunu Oynat (Tekil)
            // Burada AIPlayer sınıfını ve makeMove metodunu kullanıyoruz (Eski kodlar)
            val player = AIPlayer(profile)
            var simLevel = 1
            var simScore = 0.0
            var simPuan = 0.0
            var isFinished = false

            while (!isFinished) {
                val move = player.makeMove(simLevel, 0.0) // Noise = 0
                val limit = (300 - simLevel) * 10.0
                if (move.isCorrect && move.reactionTimeMs < limit) {
                    val t = (move.reactionTimeMs / 10.0) / 100.0
                    if (simLevel==1) simScore=t else simScore=((simScore*(simLevel-1))+t)/simLevel
                    if(simScore!=0.0) simPuan=simLevel*(1.0/simScore)
                    simLevel++
                } else isFinished=true
                if(simLevel>1000) isFinished=true
            }

            if (simLevel > globalMaxLevel) {
                globalMaxLevel = simLevel // Rekor kırıldıysa kaydet
            }

            // 2. Sonucu Değerlendir
            totalGames++

            // Sadece anlamlı oyunları (Level > 5) tarihe ekle
            if (simLevel > 5) {
                history.add(simPuan)
                totalScore += simPuan
                consecutiveWins++
            } else {
                // Erken elendi (Başarısızlık)
                consecutiveWins = 0
                // Başarısız olduğu için puanı listeye 0 veya düşük ekleyebiliriz ama
                // ortalamayı bozmasın diye eklemiyoruz, sadece ceza vereceğiz.
            }

            // 3. PSİKOLOJİK HESAPLAMA (Senin Formülün)
            val lastScore = if (history.isNotEmpty()) history.last() else 0.0
            val avgScore = if (history.size > 1) history.average() else lastScore

            // Delta: Son performansım ortalamama göre nasıl?
            // Eğer level < 5 ise simPuan 0 gibidir, büyük ceza yer.
            val currentPerformance = if (simLevel > 5) simPuan else (avgScore * 0.5)
            val delta = currentPerformance - avgScore

            if (delta >= 0) {
                // BAŞARI: Motivasyon artar (Azimden bağımsız)
                currentMotivation += 2.0
            } else {
                // BAŞARISIZLIK: Azim devreye girer
                // Azim 1.0 ise (1-1=0) hiç düşmez. Azim 0.1 ise (1-0.1=0.9) çok düşer.
                // Delta zaten eksi, o yüzden += ile ekliyoruz (düşürüyor)
                // Ama delta çok büyük olabilir, onu biraz scale edelim (örn: 0.5 ile çarp)
                val impact = delta * (1.0 - profile.grit) * 0.5
                currentMotivation += impact
            }

            // SIKILMA (Boredom)
            if (consecutiveWins > 5) {
                // Çok sık kazandı, sıkılma eşiği yüksekse ceza yer
                currentMotivation -= (consecutiveWins * profile.boredomThreshold)
                if (currentMotivation <= 0) quitReason = "BOREDOM"
            }

            // YORGUNLUK
            currentMotivation -= profile.fatigueRate

            if (currentMotivation <= 0 && quitReason == "LIMIT") {
                quitReason = if (delta < 0) "GAVE_UP" else "EXHAUSTED"
            }
        }

        val finalAvg = if(history.isNotEmpty()) history.average() else 0.0
        return SimulationSummary(finalAvg, totalGames, quitReason, globalMaxLevel)
    }

    // Yardımcı Sınıf
    data class SimulationSummary(
        val avgScore: Double,
        val gamesPlayed: Int,
        val reason: String,
        val maxLevel: Int
    )
    /**
     * KÜTLESEL DENEY (MASSIVE SIMULATION)
     * Belirtilen sayıda (gameCount) oyunu arka arkaya oynatır ve istatistik çıkarır.
     */
    /**
     * KÜTLESEL DENEY (MASSIVE SIMULATION) - FİLTRELİ VE DÜZELTİLMİŞ
     */
    fun runMassiveSimulation(profile: AIProfile, noiseLevel: Double, gameCount: Int = 100) {
        viewModelScope.launch {

            // 1. UI'a Bilgi Ver: Oyun başladı, Game Over durumu yok
            _gameState.value = _gameState.value?.copy(
                isPlaying = true,
                isGameOver = false,
                targetColorName = "HESAPLANIYOR...\n($gameCount Geçerli Veri)"
            )

            delay(50) // UI güncellensin

            // --- VERİ TOPLAMA HAVUZU ---
            val allScores = mutableListOf<Double>()
            val allLevels = mutableListOf<Int>()

            // DEĞİŞKENLERİ EN ÜSTTE TANIMLIYORUZ (Scope Hatası Olmasın Diye)
            var validGames = 0
            var attempts = 0
            val maxAttempts = gameCount * 10 // Sonsuz döngü koruması (Max 1000 deneme)

            // --- DÖNGÜ BAŞLIYOR ---
            // Hedeflenen geçerli oyun sayısına ulaşana kadar devam et
            while (validGames < gameCount && attempts < maxAttempts) {
                attempts++

                // Tekil Oyun Değişkenleri
                var simLevel = 1
                var simScore = 0.0
                var simPuan = 0.0
                var isGameFinished = false
                val aiPlayer = AIPlayer(profile)

                while (!isGameFinished) {
                    val moveResult = aiPlayer.makeMove(simLevel, noiseLevel)
                    val timeLimitMs = (300 - simLevel) * 10.0

                    if (moveResult.isCorrect && moveResult.reactionTimeMs < timeLimitMs) {
                        val reactionTimeSec = (moveResult.reactionTimeMs / 10.0) / 100.0

                        if (simLevel == 1) {
                            simScore = reactionTimeSec
                        } else {
                            simScore = ((simScore * (simLevel - 1)) + reactionTimeSec) / simLevel
                        }

                        if (simScore != 0.0) {
                            simPuan = simLevel * (1.0 / simScore)
                        }

                        simLevel++
                    } else {
                        isGameFinished = true
                    }

                    if (simLevel > 1000) isGameFinished = true
                }

                // --- FİLTRELEME ---
                // Sadece Level 5'i geçen oyunları "Geçerli" say
                if (simLevel > 5) {
                    allScores.add(simPuan)
                    allLevels.add(simLevel)
                    validGames++ // Sayacı artır
                }
            }
            // --- DÖNGÜ BİTTİ ---

            // --- İSTATİSTİK HESAPLAMA ---
            // Eğer hiç geçerli oyun yoksa (hepsi elendiyse) hata vermesin
            val averagePuan = if (allScores.isNotEmpty()) allScores.average() else 0.0
            val maxLevel = allLevels.maxOrNull() ?: 0
            val bestRunScore = allScores.maxOrNull() ?: 0.0

            // Standart Sapma
            val variance = if (allScores.isNotEmpty()) allScores.map { (it - averagePuan) * (it - averagePuan) }.average() else 0.0
            val stdDev = Math.sqrt(variance)

            // --- SONUCU EKRANA BAS ---
            _gameState.value = GameState(
                score = bestRunScore, // REKOR PUAN
                puan = averagePuan,   // ORTALAMA PUAN
                level = maxLevel,     // EN YÜKSEK LEVEL
                targetColorName = "DENEY TAMAMLANDI",
                gameOverMessage = """
                    Profil: ${profile.typeName}
                    Toplam Deneme: $attempts
                    Geçerli Oyun: $validGames
                    Ortalama: %.2f
                    Std Sapma: %.2f
                    En İyi Skor: %.2f
                """.trimIndent().format(averagePuan, stdDev, bestRunScore),
                isGameOver = true,
                isPlaying = false
            )
        }
    }

}

// Analiz için kaydedilecek veri satırı
data class SimulationRecord(
    val id: Int,
    // Girdiler
    val inputBaseSpeed: Double,
    val inputFocus: Double,
    val inputGrit: Double,
    val inputBoredom: Double,
    // Çıktılar
    val outputAvgScore: Double,
    val outputTotalGames: Int,    // Kaç oyun dayandı?
    val outputQuitReason: String,  // Neden bıraktı?
    val outputMaxLevel: Int
)