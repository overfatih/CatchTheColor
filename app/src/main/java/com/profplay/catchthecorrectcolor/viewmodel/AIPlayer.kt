package com.profplay.catchthecorrectcolor.viewmodel

import com.profplay.catchthecorrectcolor.model.AIProfile
import java.util.Random

class AIPlayer(val profile: AIProfile) {

    /**
     * Hamle Yap (Simülasyonun Kalbi)
     * @param currentLevel: Oyunun zorluk seviyesi
     * @param environmentNoise: Ortam gürültüsü (0.0 ile 1.0 arası)
     * @return Pair<GeçenSüre, DoğruMu>
     */
    fun makeMove(currentLevel: Int, environmentNoise: Double): SimulationMoveResult {

        // Gaussian dağılım (Çan eğrisi) için Java Random kullanıyoruz
        // Çünkü Kotlin.Random'da standart nextGaussian() yoktur.
        val javaRandom = Random()

        // 1. ADAPTASYON (Bilişsel Esneklik)
        // Seviye arttıkça ve "adaptability" yüksekse gürültü etkisi azalır (Alışkanlık kazanma)
        val adaptationBonus = (currentLevel * 0.01 * profile.adaptability).coerceAtMost(0.5)
        val effectiveNoise = (environmentNoise * (1.0 - adaptationBonus)).coerceAtLeast(0.0)

        // 2. TEPKİ SÜRESİ HESAPLAMA (Cognitive Processing Speed Model)

        // A. Odaklanma Etkisi (Flow State)
        // Seviye arttıkça hızlanır ama senin kodundaki * 2.0 çok azdı, biraz artırdık (* 4.0)
        // Ancak bir sınır koyduk (max 250ms hızlanabilir) ki süre 0'a inmesin.
        val focusEffect = (currentLevel * profile.focusStability * 4.0).coerceAtMost(250.0)

        // B. Gürültü Gecikmesi (Cognitive Load Penalty) - KRİTİK BİLİMSEL KISIM
        // Literatür: Gürültü sadece hata yaptırmaz, beyni yavaşlatır (Latency).
        // Noise 1.0 ise ve direnç 0 ise süreye 300ms eklenir.
        val noiseDelay = effectiveNoise * 300.0 * (1.0 - profile.noiseResistance)

        // C. İnsan Varyasyonu (Intra-Individual Variability - IIV)
        // ESKİSİ: Random.nextDouble(-20.0, 20.0) -> Bu "Uniform" dağılımdır (Robot gibi).
        // YENİSİ: Gaussian (Normal) Dağılım -> İnsan doğasına uygun.
        // nextGaussian() ortalama 0.0, sapma 1.0 verir. Biz bunu 60 ile çarpıp +/- 60ms sapma yaratıyoruz.
        val humanVariation = javaRandom.nextGaussian() * 60.0

        // TOTAL SÜRE FORMÜLÜ
        var calculatedTime = profile.baseReflexTime - focusEffect + noiseDelay + humanVariation

        // Fizyolojik Sınırlar (İnsan 150ms'den hızlı olamaz)
        calculatedTime = calculatedTime.coerceIn(150.0, 3000.0)

        // 3. DOĞRU KARAR VERME OLASILIĞI (Accuracy)
        // Gürültü arttıkça hata riski artar
        val errorChance = profile.errorProneFactor + (effectiveNoise * 0.15)

        // Hata kontrolü
        val isCorrectMove = javaRandom.nextDouble() > errorChance

        return SimulationMoveResult(
            reactionTimeMs = calculatedTime,
            isCorrect = isCorrectMove
        )
    }
}

// Sonucu döndürmek için basit bir veri sınıfı
data class SimulationMoveResult(
    val reactionTimeMs: Double,
    val isCorrect: Boolean
)