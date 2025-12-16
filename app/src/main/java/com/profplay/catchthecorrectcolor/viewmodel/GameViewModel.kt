package com.profplay.catchthecorrectcolor.viewmodel

import android.graphics.Color
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.profplay.catchthecorrectcolor.model.AIProfile
import com.profplay.catchthecorrectcolor.model.AppSettings
import com.profplay.catchthecorrectcolor.model.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.PriorityQueue
import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class GameViewModel : ViewModel() {

    // Artık dışarıdan import edilen doğru GameState'i kullanıyor
    private val _gameState = MutableLiveData(GameState())
    val gameState: LiveData<GameState> = _gameState

    private var timer: CountDownTimer? = null
    private val javaRandom = Random() // Monte Carlo simülasyonu için

    // Başlangıç renkleri (Gri tonları) - UI ilk açıldığında boş kalmasın diye
    private val initialColors = listOf(
        Color.LTGRAY, Color.LTGRAY,
        Color.LTGRAY, Color.LTGRAY
    )

    init {
        // Uygulama ilk açıldığında "Boş/Bekleme" durumu oluştur
        _gameState.value = GameState(
            buttonColors = initialColors,
            isPlaying = false,
            targetColorName = "START",
            level = 1,
            score = 0.0
        )
    }

    // Oyun İçi Değişkenler
    private var currentNumber: Double = 0.0
    private var bestScore: Double? = null
    private var currentLevel: Int = 1
    private var currentPuan: Double = 0.0

    // Simülasyon Verisi (Norm Oluşturma İçin)
    private var normingPopulationScores: List<Double> = emptyList()

    // ==========================================
    // OYUN MOTORU (Logic)
    // ==========================================

    fun startGame() {
        // Değişkenleri Sıfırla
        currentNumber = 0.0
        bestScore = null
        currentLevel = 1
        currentPuan = 0.0

        // Yeni bir tur başlat
        startLevelAction()
    }

    private fun startLevelAction() {
        // 1. Rastgele 4 Renk Seç
        val colors = generateRandomColors()

        // 2. Hedef Rengi Belirle
        val targetColor = colors.random()

        // 3. Süre Limitini Hesapla (Level arttıkça zorlaşır)
        val limitMs = max(500L, (300 - currentLevel) * 10L) // Min 500ms limit

        // 4. State'i Güncelle
        _gameState.value = GameState(
            isPlaying = true,
            level = currentLevel,
            score = bestScore ?: 0.0,
            puan = currentPuan,
            elapsedTime = 0.0,

            buttonColors = colors, // List<Int>
            targetColorName = getColorName(targetColor), // Ekranda yazacak metin

            // DİKKAT: GameState içinde 'targetColorHex' yoksa veya Int değilse,
            // Logic kısmında (processMove) kontrol için bunu State'e eklemelisin.
            // Şimdilik 'targetColorHex' alanını Int olarak kullandığını varsayıyorum.
            // Eğer yoksa GameState data class'ına 'val targetColorInt: Int = 0' ekle.
            // Ben mevcut yapına uyarak targetColorHex (Int) varsayıyorum:
            // targetColorHex = targetColor,

            isGameOver = false
        )

        // 5. Sayaçı Başlat
        startTimer(limitMs)
    }

    fun processMove(selectedColorInt: Int) {
        val currentState = _gameState.value ?: return

        // Oyun oynamıyorsa veya bittiyse tıklamaları yoksay
        if (!currentState.isPlaying || currentState.isGameOver) return

        // Hedef rengi bul (İsimden renge gitmek yerine, oluştururken sakladığımız hedefi kullanmalıyız)
        // Ancak GameState içinde hedef rengin Int değeri yoksa, ismine bakarak bulabiliriz.
        // Güvenli yöntem: targetColorName ile seçilen rengin ismini kıyasla
        val selectedColorName = getColorName(selectedColorInt)

        if (selectedColorName == currentState.targetColorName) {
            // DOĞRU CEVAP
            stopTimer()
            calculateScoreAndLevelUp()
        } else {
            // YANLIŞ CEVAP
            stopTimer()
            endGame("Yanlış Cevap!")
        }
    }

    private fun calculateScoreAndLevelUp() {
        val reactionTime = currentNumber / 100.0 // Saniye cinsinden

        if (bestScore == null) {
            bestScore = reactionTime
        } else {
            // Ağırlıklı Ortalama (Eski skorun etkisi azalır)
            bestScore = ((bestScore!! * (currentLevel - 1)) + reactionTime) / currentLevel
        }

        if (bestScore != 0.0) {
            currentPuan = currentLevel * (1.0 / bestScore!!)
        }

        currentLevel += 1
        currentNumber = 0.0

        // Bir sonraki levele geç
        startLevelAction()
    }

    private fun startTimer(limitMs: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(limitMs, 10) {
            override fun onTick(millisUntilFinished: Long) {
                currentNumber += 1.0 // 10ms'de bir artar

                // State'i güncelle (Süreyi ekrana basmak için)
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
            isPlaying = false,
            gameOverMessage = message
        )
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }

    // =================================================
    // YARDIMCI RENK FONKSİYONLARI
    // =================================================

    private fun generateRandomColors(): List<Int> {
        return listOf(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW
        ).shuffled()
    }

    private fun getColorName(color: Int): String {
        return when (color) {
            Color.RED -> "KIRMIZI"
            Color.GREEN -> "YEŞİL"
            Color.BLUE -> "MAVİ"
            Color.YELLOW -> "SARI"
            else -> "BİLİNMEYEN"
        }
    }

    // =================================================
    // SİMÜLASYON VE AI BÖLÜMÜ (Tez Kodları)
    // =================================================

    // 1. Gifted (High Achiever) Profil
    val giftedProfile = AIProfile(
        typeName = "Gifted (High Achiever)",
        baseReflexTime = 530.0,
        focusStability = 1.5,
        noiseResistance = 0.3,
        errorProneFactor = 0.02,
        grit = 0.85,
        boredomThreshold = 0.7,
        fatigueRate = 0.2,
        adaptability = 0.8
    )

    // Monte Carlo Norm Simülasyonu
    // GameViewModel.kt içinde bu fonksiyonu bul ve değiştir:

    // Hafızadaki son simülasyon verisi (Kaydetmek için bekliyor)
    private var lastGeneratedRecords: List<SimulationRecord> = emptyList()

    // 1. GÜNCELLENMİŞ MONTE CARLO (Veri Toplayan Versiyon)
    fun runMonteCarloNorming(agentCount: Int = 1000) {
        viewModelScope.launch {
            val gson = Gson()
            val config = AppSettings.currentConfig
            val recordsList = mutableListOf<SimulationRecord>() // Kayıt defteri

            _gameState.value = _gameState.value?.copy(
                isPlaying = true,
                targetColorName = "VERİ TOPLANIYOR...\n(Ayarlar: Hız=${config.minSpeed.toInt()}ms)"
            )
            delay(50)

            val populationScores = mutableListOf<Double>()

            for (i in 1..agentCount) {
                // --- PARAMETRE ÜRETİMİ (Aynı kalıyor) ---
                var speed = config.minSpeed + (javaRandom.nextGaussian() * config.speedVariance)
                speed = max(150.0, min(1000.0, speed))
                var error = config.baseErrorRate + (javaRandom.nextGaussian() * config.errorVariance)
                error = max(0.001, min(0.3, error))
                var grit = config.baseGrit + (javaRandom.nextGaussian() * config.gritVariance)
                grit = max(0.1, min(1.0, grit))
                var focus = config.baseFocus + (javaRandom.nextGaussian() * 0.5)
                focus = max(0.5, min(4.0, focus))
                var boredom = config.baseBoredom + (javaRandom.nextGaussian() * 10.0)
                boredom = max(5.0, min(100.0, boredom))
                val fatigue = 0.4
                val adaptability = 0.6

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

                val result = runPsychoSimulation(tempProfile)

                if (result.consistentScore > 0) {
                    populationScores.add(result.consistentScore)

                    // --- BURASI YENİ: KAYIT DEFTERİNE EKLE ---
                    val record = SimulationRecord(
                        agentId = agentName,
                        dna = AgentDNA(speed, focus, error, grit, boredom),
                        performance = AgentPerf(
                            result.avgScore, result.highestScore, result.maxLevel,
                            result.gamesPlayed, result.reason, result.consistentScore
                        )
                    )
                    recordsList.add(record)
                }

                if (i % (agentCount / 10) == 0) {
                    _gameState.value = _gameState.value?.copy(
                        targetColorName = "Analiz: %${(i * 100) / agentCount}"
                    )
                }
            }

            normingPopulationScores = populationScores
            lastGeneratedRecords = recordsList // Veriyi hafızaya al

            if (populationScores.isNotEmpty()) {
                val mean = populationScores.average()
                val variance = populationScores.map { (it - mean).pow(2) }.average()
                val stdDev = sqrt(variance)
                val sigma2Baraj = mean + (2 * stdDev)

                _gameState.value = GameState(
                    score = populationScores.maxOrNull() ?: 0.0,
                    puan = mean,
                    level = agentCount,
                    targetColorName = "ANALİZ TAMAMLANDI", // Bu yazı önemli, UI bunu takip edecek
                    gameOverMessage = """
                        Ortalama Puan: %.2f
                        Baraj (+2SD): %.2f
                        Üretilen Veri: ${recordsList.size} satır
                        
                        Veriyi kaydetmek için butona basın.
                    """.trimIndent().format(mean, sigma2Baraj),
                    isGameOver = true,
                    isPlaying = false
                )
            }
        }
    }

    // 2. YENİ: DOSYAYI TELEFONA KAYDETME FONKSİYONU
    fun saveSimulationToDownloads(context: android.content.Context) {
        if (lastGeneratedRecords.isEmpty()) {
            android.widget.Toast.makeText(context, "Kaydedilecek veri yok!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                val gson = Gson()
                val jsonString = gson.toJson(lastGeneratedRecords)

                // Dosya adı: simulation_data_TIMESTAMP.json
                val fileName = "sim_data_${System.currentTimeMillis()}.json"

                // --- VERSİYON KONTROLÜ BAŞLIYOR ---
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    // YÖNTEM A: Android 10 (Q) ve Üzeri (Senin Yazdığın Kod)
                    // MediaStore.Downloads API'sini kullanır. İzin gerektirmez.

                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    uri?.let {
                        resolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(jsonString.toByteArray())
                        }
                        android.widget.Toast.makeText(context, "💾 Kaydedildi (API 29+): Downloads/$fileName", android.widget.Toast.LENGTH_LONG).show()
                    }

                } else {
                    // YÖNTEM B: Android 9 ve Altı (Eski Yöntem)
                    // Klasik dosya sistemi kullanır.
                    // NOT: Bu yöntem için Manifest'te WRITE_EXTERNAL_STORAGE izni gerekebilir.

                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    val file = java.io.File(downloadsDir, fileName)

                    java.io.FileOutputStream(file).use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }

                    // Dosyayı galeriye/sisteme tanıt (Görünür olsun diye)
                    // (JSON için şart değil ama iyi bir pratiktir)
                    // MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)

                    android.widget.Toast.makeText(context, "💾 Kaydedildi (Eski API): ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
                }


            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Hata: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    // Psycho-Engine Simülasyonu
    // GameViewModel.kt içinde bu fonksiyonu bul ve şu şekilde güncelle:

    // 1. Parametreye 'noiseLevel' eklendi (Varsayılan 0.0)
    private fun runPsychoSimulation(profile: AIProfile, noiseLevel: Double = 0.0): SimulationSummary {
        var currentMotivation = 100.0
        var totalGames = 0
        val history = mutableListOf<Double>()
        var consecutiveWins = 0
        var quitReason = "LIMIT"
        var globalMaxLevel = 0
        var globalMaxScore = 0.0
        val topScores = java.util.PriorityQueue<Double>(5)

        // Max 200 oyun sınırı
        while (currentMotivation > 0 && totalGames < 200) {
            val player = AIPlayer(profile)
            var simLevel = 1
            var simScore = 0.0
            var simPuan = 0.0
            var isFinished = false

            while (!isFinished) {
                // 2. BURASI GÜNCELLENDİ: Artık dışarıdan gelen gürültüyü kullanıyor
                val move = player.makeMove(simLevel, noiseLevel)

                val limit = max(500.0, (300 - simLevel) * 10.0)

                if (move.isCorrect && move.reactionTimeMs < limit) {
                    val t = (move.reactionTimeMs / 10.0) / 100.0
                    if (simLevel == 1) simScore = t else simScore = ((simScore * (simLevel - 1)) + t) / simLevel
                    if (simScore != 0.0) simPuan = simLevel * (1.0 / simScore)

                    // Stres Faktörü
                    val stressFactor = (simLevel * simLevel) / 14000.0
                    if (java.lang.Math.random() < stressFactor) isFinished = true

                    simLevel++
                } else {
                    isFinished = true
                }
                if (simLevel > 1000) isFinished = true
            }

            if (simLevel > globalMaxLevel) globalMaxLevel = simLevel
            if (simPuan > globalMaxScore) globalMaxScore = simPuan

            if (simPuan > 0) {
                if (topScores.size < 5) {
                    topScores.add(simPuan)
                } else if (simPuan > topScores.peek()) {
                    topScores.poll()
                    topScores.add(simPuan)
                }
            }

            totalGames++
            if (simLevel > 5) {
                history.add(simPuan)
                consecutiveWins++
            } else {
                consecutiveWins = 0
            }

            // Motivasyon ve Sıkılma Hesabı (Aynı kalıyor)
            val avgScore = if (history.isNotEmpty()) history.average() else 0.0
            val currentPerformance = if (simLevel > 5) simPuan else (avgScore * 0.5)
            val delta = currentPerformance - avgScore

            if (delta >= 0) {
                currentMotivation += 2.0
            } else {
                val impact = delta * (1.0 - profile.grit) * 0.5
                currentMotivation += impact
            }

            if (consecutiveWins > 5) currentMotivation -= (consecutiveWins * profile.boredomThreshold)
            currentMotivation -= profile.fatigueRate

            if (currentMotivation <= 0) quitReason = if (delta < 0) "GAVE_UP" else "EXHAUSTED"
        }

        var sumTop = 0.0
        for(s in topScores) sumTop += s
        val consistentScore = if (topScores.isNotEmpty()) sumTop / topScores.size else 0.0
        val finalAvg = if (history.isNotEmpty()) history.average() else 0.0

        return SimulationSummary(finalAvg, totalGames, quitReason, globalMaxLevel, globalMaxScore, consistentScore)
    }

    // GameViewModel.kt içinde bu fonksiyonu bul ve değiştir:

    fun runNoiseStressTest(barajPuan: Double = 190.0) {
        // Norm verisi yoksa uyar ama testi engelleme (kullanıcı sadece tekil ajanı görmek istiyor olabilir)
        val hasNorm = normingPopulationScores.isNotEmpty()

        viewModelScope.launch {

            // 1. AYARLARI ÇEK
            val config = AppSettings.currentConfig

            _gameState.value = _gameState.value?.copy(
                isPlaying = true,
                targetColorName = "STRES TESTİ...\n(Hız: ${config.minSpeed.toInt()}ms)"
            )
            delay(100)

            // 2. DİNAMİK PROFİL OLUŞTUR (Slider Değerleri Buraya Giriyor)
            // Bu ajan tam olarak senin ayarladığın özelliklere sahip.
            val testSubjectProfile = AIProfile(
                typeName = "Test Subject (User Defined)",
                baseReflexTime = config.minSpeed,      // Slider'dan gelen Hız
                errorProneFactor = config.baseErrorRate, // Slider'dan gelen Hata
                grit = config.baseGrit,                // Slider'dan gelen Sebat
                focusStability = config.baseFocus,     // Slider'dan gelen Odak
                boredomThreshold = config.baseBoredom, // Slider'dan gelen Sıkılma

                // Varsayılanlar
                noiseResistance = 0.3, // Stres testinde bu önemli
                fatigueRate = 0.2,
                adaptability = 0.6
            )

            val results = StringBuilder()
            results.append("Gürültü | Puan  | Durum\n")
            results.append("-----------------------\n")

            // 0.0'dan 0.8'e kadar test et
            for (noiseInt in 0..80 step 20) {
                val currentNoise = noiseInt / 100.0

                // Oluşturduğumuz dinamik profili koştur
                val summary = runBatchWithPsychology(testSubjectProfile, currentNoise, count = 20)
                val rankStatus = if (hasNorm) calculatePercentileRank(summary.avgScore) else "Veri Yok"
                val line = "%.1f    | %.0f | %s".format(currentNoise, summary.avgScore, rankStatus)
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
                targetColorName = "TEST BİTTİ",
                gameOverMessage = """
                    Ajan Özellikleri:
                    Hız: ${config.minSpeed}, Hata: ${config.baseErrorRate}
                    ---------------------
                    $results
                """.trimIndent(),
                isGameOver = true,
                isPlaying = false
            )
        }
    }
    // GameViewModel.kt içine ekle:

    // Belirli bir profili ve gürültü seviyesini 'count' kadar tekrar edip ortalamasını alır.
    private fun runBatchWithPsychology(profile: AIProfile, noise: Double, count: Int): SimulationSummary {
        var totalScore = 0.0
        var games = 0
        var batchMaxScore = 0.0
        var batchMaxLevel = 0
        var batchBestConsistentScore = 0.0

        repeat(count) {
            // Gürültüyü gönderiyoruz
            val res = runPsychoSimulation(profile, noiseLevel = noise)

            if (res.highestScore > batchMaxScore) batchMaxScore = res.highestScore
            if (res.maxLevel > batchMaxLevel) batchMaxLevel = res.maxLevel
            if (res.consistentScore > batchBestConsistentScore) batchBestConsistentScore = res.consistentScore

            if (res.avgScore > 0) {
                totalScore += res.avgScore
                games++
            }
        }

        val finalAvg = if (games > 0) totalScore / games else 0.0

        return SimulationSummary(
            avgScore = finalAvg,
            gamesPlayed = games,
            reason = "BATCH",
            maxLevel = batchMaxLevel,
            highestScore = batchMaxScore,
            consistentScore = batchBestConsistentScore
        )
    }
    fun hasNormData(): Boolean {
        return normingPopulationScores.isNotEmpty()
    }

    private fun calculatePercentileRank(score: Double): String {
        if (normingPopulationScores.isEmpty()) return "N/A"
        val countBelow = normingPopulationScores.count { it < score }
        val percentile = (countBelow.toDouble() / normingPopulationScores.size) * 100.0
        val topPercent = 100.0 - percentile
        return "Top %% %.2f".format(topPercent) // Çıktı: "Top % 5.23"
    }
}

// ==========================================
// YARDIMCI VERİ SINIFLARI (Model)
// ==========================================

data class SimulationSummary(
    val avgScore: Double,
    val gamesPlayed: Int,
    val reason: String,
    val maxLevel: Int,
    val highestScore: Double,
    val consistentScore: Double
)

data class SimulationRecord(val agentId: String, val dna: AgentDNA, val performance: AgentPerf)
data class AgentDNA(val baseSpeed: Double, val focus: Double, val errorRate: Double, val grit: Double, val boredomThresh: Double)
data class AgentPerf(val avgScore: Double, val maxScore: Double, val maxLevel: Int, val totalGamesPlayed: Int, val quitReason: String, val consistentScore: Double)