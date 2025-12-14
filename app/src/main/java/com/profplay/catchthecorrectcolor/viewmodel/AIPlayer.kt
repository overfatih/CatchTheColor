package com.profplay.catchthecorrectcolor.viewmodel

import com.profplay.catchthecorrectcolor.model.AIProfile
import java.util.Random

class AIPlayer(val profile: AIProfile) {

    private val javaRandom = Random()

    // YENİ: Ajanın anlık stres seviyesi (0.0 - 1.0 arası)
    // Oyun boyunca hata yaptıkça artar, doğru bildikçe azalır.
    private var currentPanicLevel: Double = 0.0

    // Her yeni oyun başladığında paniği sıfırlamak için fonksiyon
    fun resetState() {
        currentPanicLevel = 0.0
    }

    fun makeMove(currentLevel: Int, environmentNoise: Double): SimulationMoveResult {

        // 1. ADAPTASYON VE PANİK HESABI (Sebat Mekanizması)

        // 1. DÜZELTME: Adaptasyon gürültüyü tamamen yok edememeli, sadece azaltmalı.
        // Max %30 koruma sağlasın (Eskiden %50 idi)
        val adaptationBonus = (currentLevel * 0.005 * profile.adaptability).coerceAtMost(0.3)
        val effectiveNoise = (environmentNoise * (1.0 - adaptationBonus)).coerceAtLeast(0.0)

        // B. Panik Etkisi (Önceki hataların birikimi)
        // Panik arttıkça beyin kilitlenir (Freeze) -> Süre uzar
        val panicDelay = currentPanicLevel * 400.0 // Max panikte 400ms gecikme ekler!

        // 2. TEPKİ SÜRESİ HESAPLAMA

        // Odaklanma (Flow)
        val focusEffect = (currentLevel * profile.focusStability * 4.0).coerceAtMost(250.0)

        // Gürültü Gecikmesi (Cognitive Load) 300 -> 600
        // Gürültü %50 ise ve direnç yoksa, 300ms eklenir. Bu ciddi bir farktır.
        val noiseDelay = effectiveNoise * 600.0 * (1.0 - profile.noiseResistance)
        // İnsan Varyasyonu (Gaussian)
        val humanVariation = javaRandom.nextGaussian() * 60.0

        // TOTAL SÜRE
        var calculatedTime = profile.baseReflexTime - focusEffect + noiseDelay + panicDelay + humanVariation

        // Fizyolojik Sınırlar
        calculatedTime = calculatedTime.coerceIn(150.0, 3000.0)

        // 3. DOĞRU KARAR VERME VE SONRAKİ HAMLE İÇİN PSİKOLOJİ GÜNCELLEME

        // Hata Riski: Profil + Gürültü + MEVCUT PANİK
        // Panikleyen çocuk daha çok hata yapar (Death Spiral)
        val errorChance = profile.errorProneFactor + (effectiveNoise * 0.2) + (currentPanicLevel * 0.2)

        val isCorrectMove = javaRandom.nextDouble() > errorChance

        // --- KRİTİK KISIM: Hata Sonrası Toparlanma (Resilience) ---
        updatePanicState(isCorrectMove)

        return SimulationMoveResult(
            reactionTimeMs = calculatedTime,
            isCorrect = isCorrectMove
        )
    }

    // Paniği güncelleyen iç fonksiyon
    private fun updatePanicState(isCorrect: Boolean) {
        if (!isCorrect) {
            // HATA YAPTI! Stres artıyor.
            // Adaptability yüksekse daha az stres yüklenir, ama YİNE DE yüklenir.
            // Örnek: Gifted (0.9) -> 0.15 artar. Normal (0.4) -> 0.25 artar.
            val stressLoad = 0.1 + (0.25 * (1.0 - profile.adaptability))
            currentPanicLevel += stressLoad
        } else {
            // DOĞRU YAPTI! Toparlanıyor (Recovery).
            // Adaptability yüksekse daha hızlı sakinleşir.
            // Ama anında 0.0 olmaz, yavaş yavaş düşer.
            val recoveryRate = 0.05 + (0.15 * profile.adaptability)
            currentPanicLevel -= recoveryRate
        }

        // Sınırları koru (Asla 0'ın altına inmez, 1'in üstüne çıkmaz)
        currentPanicLevel = currentPanicLevel.coerceIn(0.0, 1.0)
    }
}

// Sonucu döndürmek için basit bir veri sınıfı
data class SimulationMoveResult(
    val reactionTimeMs: Double,
    val isCorrect: Boolean
)