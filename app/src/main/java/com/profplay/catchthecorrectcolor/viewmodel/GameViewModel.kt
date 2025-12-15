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
import kotlin.math.pow
import kotlin.math.sqrt

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
    private var normingPopulationScores: List<Double> = emptyList()

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

    // 1. Gifted (High Achiever)
    // Hızlı işlemleme, Yüksek Odak, Yüksek Azim, İyi Adaptasyon
    val giftedProfile = AIProfile(
        typeName = "Gifted (High Achiever)",
        baseReflexTime = 530.0,   // 520 -> 550ms (Biraz yavaşlattık)
        focusStability = 1.5,     // 1.7 -> 1.5 (Süper hızlanma azaldı)
        noiseResistance = 0.3,    // 0.25 -> 0.3 (Gürültüye biraz daha açık)
        errorProneFactor = 0.02,  // %1 -> %2 (Her 50 soruda 1 hata yapabilir)
        grit = 0.85,              // 0.9 -> 0.85
        boredomThreshold = 0.7,
        fatigueRate = 0.2,
        adaptability = 0.8        // 0.85 -> 0.80
    )

    // 2. Typical (Normal Gelişim) - Biraz "Güçlendi"
    val averageProfile = AIProfile(
        typeName = "Typical Child",
        baseReflexTime = 700.0,   // 750 -> 700ms (Biraz hızlandı)
        focusStability = 0.9,     // 0.8 -> 0.9 (Biraz daha iyi odaklanıyor)
        noiseResistance = 0.5,
        errorProneFactor = 0.04,  // %8 -> %4 (KRİTİK DEĞİŞİKLİK: Artık 25 soruda 1 hata yapıyor)
        grit = 0.6,               // 0.5 -> 0.6 (Hemen pes etmiyor)
        boredomThreshold = 0.2,
        fatigueRate = 0.5,
        adaptability = 0.5        // 0.4 -> 0.5
    )

    // 3. Gifted (Sensory / 2e)
    // Çok hızlı ama çok kırılgan
    val giftedSensoryProfile = AIProfile(
        typeName = "Gifted (Sensory Sensitive)",
        baseReflexTime = 420.0,   // EN HIZLI (Nöral iletim çok yüksek)
        focusStability = 2.5,     // Hiper odak
        noiseResistance = 0.9,    // DİKKAT: Gürültüye direnci yok (Çok hassas)
        errorProneFactor = 0.03,
        grit = 0.6,
        boredomThreshold = 0.9,
        fatigueRate = 0.6,        // Çabuk yorulur (Mental enerji tüketimi fazla)
        adaptability = 0.6        // Zeki ama ortam kötüyse adapte olamaz
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
    fun runMonteCarloNorming(agentCount: Int = 10000) {
        viewModelScope.launch {
            val gson = Gson() // Döngü dışında bir kez oluşturulmalı

            _gameState.value = _gameState.value?.copy(
                isPlaying = true,
                targetColorName = "VERİ TOPLANIYOR...\n($agentCount Ajan)"
            )
            delay(50) // UI thread nefes alsın

            val populationScores = mutableListOf<Double>()
            // Not: 'records' listesini hafızada tutmuyoruz, direkt Logcat'e basacağız.

            for (i in 1..agentCount) {
                // --- 1. PARAMETRE ÜRETİMİ (User Tuned + Micro Adjust) ---

                // --- AYRIŞTIRMA MODU (Separation Mode) ---
                // Hedef: Kurtosis'i artırmak (Sivrilik).
                // Yöntem: Oyunu zorlaştır (Ortalama düşsün), Makası aç (Dahiler kaçsın).

                // 1. HIZ (SPEED):
                // 680ms -> 520ms. (Daha seri oynasınlar).
                // Varyans: 60.0 (Yetenek farkı korunsun).
                var speed = 440.0 + (javaRandom.nextGaussian() * 75.0)
                speed = max(300.0, min(800.0, speed))

                // 2. ODAK (FOCUS):
                // 1.8 -> 2.0. Odaklanma arttı.
                var focus = 2.0 + (javaRandom.nextGaussian() * 0.50)
                focus = max(1.0, min(4.5, focus))

                // 3. HATA (ERROR):
                // %5.0 -> %4.0 (0.040).
                // Taban hata düştü (Ortalama yükselsin diye).
                // AMA VARYANS 0.020 (Çok Yüksek).
                // Bu sayede "Hatasız Kul Olmaz" ama "Dahi Ajan" olur ayrımı netleşecek.
                var error = 0.040 + (javaRandom.nextGaussian() * 0.020)
                error = max(0.015, min(0.12, error))

                // 4. SEBAT (GRIT):
                // Korelasyonumuz (0.30) gayet iyi, bunu koruyalım.
                var grit = 0.55 + (javaRandom.nextGaussian() * 0.18)
                grit = max(0.2, min(0.95, grit))

                // Yan Parametreler
                var boredom = 0.3 + (javaRandom.nextGaussian() * 0.1)
                boredom = max(0.0, min(0.8, boredom))

                var fatigue = 0.4 + (javaRandom.nextGaussian() * 0.1)
                fatigue = max(0.1, min(2.0, fatigue))

                var adaptability = 0.6 + (javaRandom.nextGaussian() * 0.15)
                adaptability = max(0.1, min(1.0, adaptability))

                // --- 2. PROFİL OLUŞTUR ---
                val agentName = "Agent-$i"
                val tempProfile = AIProfile(
                    typeName = agentName,
                    baseReflexTime = speed,
                    focusStability = focus,
                    noiseResistance = 0.0,
                    errorProneFactor = error,
                    grit = grit,
                    boredomThreshold = boredom,
                    fatigueRate = fatigue,
                    adaptability = adaptability
                )

                // --- 3. SİMÜLASYONU KOŞ ---
                val result = runPsychoSimulation(tempProfile)

                // --- 4. KAYIT VE LOGLAMA (HİYERARŞİK) ---
                if (result.consistentScore > 0) {
                    populationScores.add(result.consistentScore)

                    // 1. DNA (Girdiler)
                    val dnaData = AgentDNA(
                        baseSpeed = speed,
                        focus = focus,
                        errorRate = error,
                        grit = grit,
                        boredomThresh = boredom
                    )

                    // 2. Performans (Çıktılar)
                    val perfData = AgentPerf(
                        avgScore = result.avgScore,
                        maxScore = result.highestScore, // <-- Bunu simülasyondan döndürmelisin
                        maxLevel = result.maxLevel,
                        totalGamesPlayed = result.gamesPlayed,
                        quitReason = result.reason,
                        consistentScore = result.consistentScore
                    )

                    // 3. Ana Kayıt
                    val record = SimulationRecord(
                        agentId = agentName,
                        dna = dnaData,
                        performance = perfData
                    )

                    // Logcat'e Bas
                    android.util.Log.d("MC_DATA", gson.toJson(record))
                }

                // UI Güncelleme (Her 100 ajanda bir)
                if (i % 100 == 0) {
                    _gameState.value = _gameState.value?.copy(
                        targetColorName = "Analiz: %${(i * 100) / agentCount}"
                    )
                    delay(5)
                }
            }

            // --- 5. OYUN SONU İSTATİSTİKLERİ (Sadece Bilgi Amaçlı) ---
            if (populationScores.isNotEmpty()) {
                val mean = populationScores.average()
                val variance = populationScores.map { (it - mean).pow(2) }.average()
                val stdDev = sqrt(variance)

                // +2 Sigma (Üstün Yetenek) Barajı Hesabı
                val sigma2Baraj = mean + (2 * stdDev)

                // Formatlama
                val strMean = "%.2f".format(mean)
                val strStd = "%.2f".format(stdDev)
                val strBaraj2SD = "%.2f".format(sigma2Baraj)
                val maxScore = populationScores.maxOrNull() ?: 0.0

                _gameState.value = GameState(
                    score = maxScore,
                    puan = mean,
                    level = agentCount,
                    targetColorName = "ANALİZ TAMAMLANDI",
                    gameOverMessage = """
                        Evren: $agentCount Ajan
                        Ortalama: $strMean
                        Std Sapma: $strStd
                        -----------------
                        HEDEF BARAJ (+2SD):
                        $strBaraj2SD Puan
                        -----------------
                        Veriler 'MC_DATA' etiketiyle
                        Logcat'e basıldı.
                    """.trimIndent(),
                    isGameOver = true,
                    isPlaying = false
                )
            }
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
        var globalMaxScore = 0.0

        val topScores = java.util.PriorityQueue<Double>(5)

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
                val move = player.makeMove(simLevel, 0.0)
                val limit = (300 - simLevel) * 10.0

                if (move.isCorrect && move.reactionTimeMs < limit) {
                    val t = (move.reactionTimeMs / 10.0) / 100.0
                    if (simLevel == 1) simScore = t else simScore = ((simScore * (simLevel - 1)) + t) / simLevel
                    if (simScore != 0.0) simPuan = simLevel * (1.0 / simScore)
                    // --- YENİ EKLENEN KOD: KADEMELİ STRES (Yığılmayı Önler) ---
                    // Level arttıkça "Bilişsel Yük" biner.
                    // Level 20'de binde 4, Level 50'de %2.5 ek hata riski doğar.
                    // Bu, "İyi" ajanları Level 30-40 bandında eler, sadece "Dahileri" geçirir.
                    // Hedef: +2SD oranını %3.37'den %2.2'ye indirmek.
                    // Yöntem: Duvarı dikleştirmek (25000 -> 15000 -> 22000 -> 11000 -> 14000).
                    val stressFactor = (simLevel * simLevel) / 14000.0
                    if (java.lang.Math.random() < stressFactor) {
                        isFinished = true
                    }
                    // ----------------------------------------------------------

                    simLevel++
                } else {
                    isFinished = true
                }
                // Sonsuz döngü önlemi (Zaten stres faktörü buraya gelmeyi zorlaştırır)
                if (simLevel > 1000) isFinished = true
            }

            if (simLevel > globalMaxLevel) {
                globalMaxLevel = simLevel // Rekor kırıldıysa kaydet
            }
            if (simPuan > globalMaxScore) { // Skor rekoru kırdı mı?
                globalMaxScore = simPuan
            }

            // --- TOP 5 MANTIĞI ---
            if (simPuan > 0) { // Sadece geçerli puanlar
                if (topScores.size < 5) {
                    topScores.add(simPuan)
                } else {
                    // Eğer yeni puan, listedeki en düşük puandan büyükse
                    // En düşüğü at, yeniyi ekle.
                    if (simPuan > topScores.peek()) {
                        topScores.poll() // En küçüğü at
                        topScores.add(simPuan) // Yeniyi ekle
                    }
                }
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

        var sumTop = 0.0
        val countTop = topScores.size
        for (s in topScores) sumTop += s

        val consistentScore = if (countTop > 0) sumTop / countTop else 0.0
        val finalAvg = if (history.isNotEmpty()) history.average() else 0.0

        return SimulationSummary(finalAvg, totalGames, quitReason, globalMaxLevel, globalMaxScore, consistentScore)
    }

    // Yardımcı Sınıf
    data class SimulationSummary(
        val avgScore: Double,      // Genel Ortalama (İstatistik için kalsın)
        val gamesPlayed: Int,
        val reason: String,
        val maxLevel: Int,
        val highestScore: Double,  // Mutlak rekor (Meraklısına)
        val consistentScore: Double // (Top 5 Avg)
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

    /**
     * HİPOTEZ TESTİ: Gürültü Stres Analizi
     * Gifted profilini artan gürültü seviyelerinde test eder ve
     * barajın altına düştüğü "Kırılma Noktasını" bulur.
     */
    fun runNoiseStressTest(barajPuan: Double = 190.0) {
        if (normingPopulationScores.isEmpty()) {
            _gameState.value = _gameState.value?.copy(
                gameOverMessage = "Lütfen önce 'NORM OLUŞTUR' (Monte Carlo) butonuna basarak veriyi toplayın!",
                isGameOver = true
            )
            return
        }

        viewModelScope.launch {
            _gameState.value = _gameState.value?.copy(
                isPlaying = true,
                targetColorName = "STRES TESTİ...\n(Percentile Analizi)"
            )
            delay(100)

            val results = StringBuilder()
            results.append("Gürültü | Puan  | Konum (Rank)\n")
            results.append("-----------------------------\n")

            // 0.0'dan 0.6'ya kadar test etsek yeterli (Zaten düşecek)
            for (noiseInt in 0..100 step 10) {

                val currentNoise = noiseInt / 100.0

                // Gifted Profilini Test Et (50 Oyunluk Ortalama)
                val summary = runBatchWithPsychology(giftedProfile, currentNoise, count = 50)
                //val summary = runBatchWithPsychology(averageProfile, currentNoise, count = 50)
                //val summary = runBatchWithPsychology(giftedSensoryProfile, currentNoise, count = 50)

                // YENİ: Geçti/Kaldı yok -> Yüzdelik Dilim Var
                val rankStatus = calculatePercentileRank(summary.avgScore)

                val line = "%.1f    | %.0f | $rankStatus".format(currentNoise, summary.avgScore)
                results.append(line + "\n")

                _gameState.value = _gameState.value?.copy(
                    targetColorName = "Gürültü: $currentNoise"
                )
                delay(50)
            }

            _gameState.value = GameState(
                score = 0.0,
                puan = 0.0,
                level = 0,
                targetColorName = "HİPOTEZ TESTİ BİTTİ",
                gameOverMessage = """
                    Dayanıklılık Raporu:
                    
                    ${results.toString()}
                    ---------------------
                    * Rank: Popülasyon içindeki sıralaması.
                    (Top %2 = En iyi %2'lik dilim)
                """.trimIndent(),
                isGameOver = true,
                isPlaying = false
            )
        }
    }

    // Yardımcı: Belirli bir gürültüde 50 oyun oynatıp ortalamasını döner
    // runMonteCarlo içinde kullandığımız runPsychoSimulation'ı çağırır
    private fun runBatchWithPsychology(profile: AIProfile, noise: Double, count: Int): SimulationSummary {
        var totalScore = 0.0
        var games = 0

        // En iyilerin iyisini tutacak değişkenler
        var batchMaxScore = 0.0
        var batchMaxLevel = 0

        // YENİ: Batch içindeki en yüksek "Kararlı Skor" (Top 5 Avg) rekoru
        var batchBestConsistentScore = 0.0

        repeat(count) {
            val res = runPsychoSimulation(profile)

            // 1. Max Score Rekor Kontrolü
            if (res.highestScore > batchMaxScore) {
                batchMaxScore = res.highestScore
            }

            // 2. Level Rekor Kontrolü
            if (res.maxLevel > batchMaxLevel) {
                batchMaxLevel = res.maxLevel
            }

            // 3. YENİ: Consistent Score (Kararlı Skor) Rekor Kontrolü
            // Hangi turda "En İyi 5 Ortalaması" daha yüksekse onu "Ajanın Kapasitesi" olarak kabul ediyoruz.
            if (res.consistentScore > batchBestConsistentScore) {
                batchBestConsistentScore = res.consistentScore
            }

            // Genel Ortalama (İstatistik için)
            if (res.avgScore > 0) {
                totalScore += res.avgScore
                games++
            }
        }

        val finalAvg = if(games > 0) totalScore / games else 0.0

        // GÜNCEL RETURN: Artık 6 Parametre var
        return SimulationSummary(
            avgScore = finalAvg,
            gamesPlayed = games,
            reason = "BATCH",
            maxLevel = batchMaxLevel,
            highestScore = batchMaxScore,
            consistentScore = batchBestConsistentScore
        )
    }

    // Verilen puanın yüzdelik dilimini hesaplar (Örn: "Top %5")
    private fun calculatePercentileRank(score: Double): String {
        if (normingPopulationScores.isEmpty()) return "Veri Yok"

        // Puanın, popülasyonun kaçından daha yüksek olduğunu bul
        // binarySearch yaklaşık konumu verir, biz count kullanıp tam yerini bulalım (biraz yavaş ama kesin)
        val countBelow = normingPopulationScores.count { it < score }

        // Yüzdelik Sıra (Percentile Rank)
        // Örn: 100 kişiden 95'ini geçtiyse -> %95 (Daha iyisi %5)
        val percentile = (countBelow.toDouble() / normingPopulationScores.size) * 100.0

        // Biz "Top X%" formatında istiyoruz (En iyi % kaçta?)
        val topPercent = 100.0 - percentile

        return "Top %%${"%.2f".format(topPercent)}" // Örn: Top %4.12
    }

    // Norm verisini dışarıdan set edebileceğimiz veya kontrol edebileceğimiz yapı
    fun hasNormData(): Boolean = normingPopulationScores.isNotEmpty()

    // Eğer veri varsa direkt Stres Testine geç, yoksa uyar
    fun runSmartStressTest() {
        if (!hasNormData()) {
            // Veri yoksa otomatik oluştur (veya kullanıcıya buton çıkart)
            runMonteCarloNorming(agentCount = 10000)
            // Not: Monte Carlo bitince otomatik stres testini tetikletebilirsin
        } else {
            // Veri zaten var, direkt test et
            runNoiseStressTest()
        }
    }

}

// JSON Çıktısı için Veri Modeli (Gelişmiş Kimlik Kartı)
// --- YENİ HİYERARŞİK VERİ MODELİ ---

data class SimulationRecord(
    val agentId: String,
    val dna: AgentDNA,          // Girdiler ayrı obje
    val performance: AgentPerf  // Çıktılar ayrı obje
)

data class AgentDNA(
    val baseSpeed: Double,
    val focus: Double,
    val errorRate: Double,
    val grit: Double,
    val boredomThresh: Double
)

data class AgentPerf(
    val avgScore: Double,
    val maxScore: Double,
    val maxLevel: Int,
    val totalGamesPlayed: Int,
    val quitReason: String,
    val consistentScore: Double
)