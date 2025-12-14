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
    fun runMonteCarloNorming(agentCount: Int = 10000) { // Test için 10bin
        viewModelScope.launch {

            _gameState.value = _gameState.value?.copy(
                isPlaying = true,
                targetColorName = "PSİKOLOJİK SİMÜLASYON...\n($agentCount Ajan)"
            )
            delay(50)

            val records = mutableListOf<SimulationRecord>()
            val populationScores = mutableListOf<Double>()
            val gson = Gson()

            for (i in 1..agentCount) {

                // 1. GAUSSIAN RANDOM İLE AJAN YARATMA

                // --- CERRAHİ KALİBRASYON (Surgical Tightening) ---
                // Hedef: Mean ~110, SD ~40, +2SD Barajı ~190

                // 1. HIZ (SPEED):
                // 480ms çok hızlıydı, 580ms çok yavaştı. Altın orta: 500ms.
                // Varyansı (60 -> 45) düşürdük. Herkes birbirine daha yakın performans gösterecek.
                var speed = 490.0 + (javaRandom.nextGaussian() * 48.0)
                speed = max(350.0, min(850.0, speed))

                // 2. ODAK (FOCUS):
                // Varyansı biraz kıstık (0.45 -> 0.35).
                var focus = 2.0 + (javaRandom.nextGaussian() * 0.42)
                focus = max(1.0, min(4.0, focus))

                // 3. HATA ORANI (EN ÖNEMLİ KISIM):
                // Varyansı çok kıstık (0.008 -> 0.005). Tutarlılık artacak.
                // Taban hatayı (min) 0.015'ten 0.020'ye çektik!
                // Bu %0.5'lik fark, o 900 puanlık uçuk skorları bıçak gibi kesecek.
                var error = 0.035 + (javaRandom.nextGaussian() * 0.005)
                error = max(0.020, min(0.06, error)) // Taban %2.0 Hata (Süpermen Yok)

                // 4. SEBAT (GRIT):
                // Biraz düşürdük (0.65 -> 0.60). Çok uzayan oyunları engellemek için.
                var grit = 0.60 + (javaRandom.nextGaussian() * 0.10)
                grit = max(0.3, min(0.9, grit))

                // Sıkılma ve Yorulma
                var boredom = 0.3 + (javaRandom.nextGaussian() * 0.1)
                boredom = max(0.0, min(0.8, boredom))

                var fatigue = 0.4 + (javaRandom.nextGaussian() * 0.1) // Biraz daha dayanıklı (0.5 -> 0.4)
                fatigue = max(0.1, min(2.0, fatigue))

                var adaptability = 0.6 + (javaRandom.nextGaussian() * 0.15) // Daha esnek (0.5 -> 0.6)
                adaptability = max(0.1, min(1.0, adaptability))

                // Profili Oluştur
                val tempProfile = AIProfile(
                    typeName = "MC-$i",
                    baseReflexTime = speed,
                    focusStability = focus,
                    noiseResistance = 0.0,
                    errorProneFactor = error,
                    grit = grit,
                    boredomThreshold = boredom,
                    fatigueRate = fatigue,
                    adaptability = adaptability
                )

                // 2. PSİKOLOJİK SİMÜLASYON (Sabit 50 oyun değil, pes edene kadar!)
                val result = runPsychoSimulation(tempProfile)

                // 3. KAYIT VE LOGLAMA (NDJSON FORMATI)
                if (result.avgScore > 0) {
                    populationScores.add(result.avgScore)

                    val record = SimulationRecord(
                        id = i,
                        inputBaseSpeed = speed,
                        inputFocus = focus,
                        inputGrit = grit,
                        inputBoredom = boredom,
                        outputAvgScore = result.avgScore,
                        outputTotalGames = result.gamesPlayed,
                        outputQuitReason = result.reason,
                        outputMaxLevel = result.maxLevel
                    )

                    // LOGCAT'e TEK SATIR BAS (Tamponu patlatmaz)
                    // ÖNEMLİ: Etiketi 'MC_DATA' yapıyoruz ki Python kodumuzla uyumlu olsun.
                    android.util.Log.d("MC_DATA", gson.toJson(record))
                }
                // UI güncellemesi (Her 100 ajanda bir)
                if (i % 100 == 0) {
                    _gameState.value = _gameState.value?.copy(
                        targetColorName = "Analiz: %${(i * 100) / agentCount}"
                    )
                    delay(5) // UI nefes alsın
                }
            }

            // --- ANALİZ VE SONUÇ (PERCENTILE YÖNTEMİ) ---
            // 1. Önce Ortalamayı Bul
            val mean = populationScores.average()

            // 2. Sonra Varyansı ve Standart Sapmayı (stdDev) Bul
            val variance = populationScores.map { (it - mean) * (it - mean) }.average()
            val stdDev = Math.sqrt(variance) // <--- BU SATIR KESİN OLMALI

            // 3. Barajları Hesapla
            val sortedScores = populationScores.sorted()
            normingPopulationScores = sortedScores
            val totalSize = sortedScores.size
            // TOP %2 (Dahi / Genius)
            val indexTop2 = (totalSize * 0.98).toInt().coerceAtMost(totalSize - 1)
            val barajTop2 = if (totalSize > 0) sortedScores[indexTop2] else 0.0

            // TOP %5 (Üstün Yetenekli / Gifted) -> Genelde kullanılan budur
            val indexTop5 = (totalSize * 0.95).toInt().coerceAtMost(totalSize - 1)
            val barajTop5 = if (totalSize > 0) sortedScores[indexTop5] else 0.0

            // TOP %15 (Parlak / High Achiever) -> Geniş havuz
            val indexTop15 = (totalSize * 0.85).toInt().coerceAtMost(totalSize - 1)
            val barajTop15 = if (totalSize > 0) sortedScores[indexTop15] else 0.0

            val bottom2PercentIndex = (totalSize * 0.02).toInt().coerceAtLeast(0)
            val lowerBaraj = if (totalSize > 0) sortedScores[bottom2PercentIndex] else 0.0

            val actualMaxScore = sortedScores.maxOrNull() ?: 0.0
            val actualMaxLevel = records.maxOfOrNull { it.outputMaxLevel } ?: 0

            // 4. ŞİMDİ String'e Çevir (stdDev artık tanımlı olduğu için hata vermez)
            val strMean = "%.2f".format(mean)
            val strStd = "%.2f".format(stdDev) // <--- HATA BURADAYDI
            val strLower = "%.2f".format(lowerBaraj)
            val strMaxScore = "%.2f".format(actualMaxScore)
            val strBaraj2 = "%.2f".format(barajTop2)
            val strBaraj5 = "%.2f".format(barajTop5)
            val strBaraj15 = "%.2f".format(barajTop15)

            // JSON Loglama
            //val gson = Gson()
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

                gameOverMessage = """
                    Evren: $agentCount Ajan
                    Ortalama: $strMean
                    Std Sapma: $strStd
                    -----------------
                    BARAJLAR:
                    Top %%2 (Dahi): $strBaraj2
                    Top %%5 (Üstün): $strBaraj5
                    Top %%15 (Parlak): $strBaraj15
                    ALT SINIR (Bottom %2): $strLower
                    -----------------
                    EN YÜKSEK SKOR: $strMaxScore
                    EN YÜKSEK LEVEL: $actualMaxLevel
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

        repeat(count) {
            val res = runPsychoSimulation(profile) // Mevcut fonksiyonunu kullanır
            // Sadece geçerli oyunları alalım ki ortalama sapmasın
            if (res.avgScore > 0) {
                totalScore += res.avgScore
                games++
            }
        }

        val finalAvg = if(games > 0) totalScore / games else 0.0
        return SimulationSummary(finalAvg, games, "BATCH", 0)
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