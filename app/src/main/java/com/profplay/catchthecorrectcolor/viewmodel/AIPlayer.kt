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

        // Adaptasyon gürültüyü tamamen yok edememeli, sadece azaltmalı.
        val adaptationBonus = (currentLevel * 0.005 * profile.adaptability).coerceAtMost(0.3)
        val effectiveNoise = (environmentNoise * (1.0 - adaptationBonus)).coerceAtLeast(0.0)

        // Panik Etkisi (Önceki hataların birikimi)
        // Panik arttıkça beyin kilitlenir (Freeze) -> Süre uzar
        val panicDelay = currentPanicLevel * 400.0

        // 2. TEPKİ SÜRESİ HESAPLAMA

        // Odaklanma (Flow)
        val focusEffect = (currentLevel * profile.focusStability * 4.0).coerceAtMost(250.0)

        // Gürültü Gecikmesi (Cognitive Load)
        val noiseDelay = effectiveNoise * 600.0 * (1.0 - profile.noiseResistance)

        // İnsan Varyasyonu (Gaussian)
        val humanVariation = javaRandom.nextGaussian() * 60.0

        // TOTAL SÜRE
        var calculatedTime = profile.baseReflexTime - focusEffect + noiseDelay + panicDelay + humanVariation

        // Fizyolojik Sınırlar (Min 150ms, Max 3000ms)
        calculatedTime = calculatedTime.coerceIn(150.0, 3000.0)

        // 3. DOĞRU KARAR VERME VE SONRAKİ HAMLE İÇİN PSİKOLOJİ GÜNCELLEME

        // Hata Riski: Profil + Gürültü + MEVCUT PANİK
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
            val stressLoad = 0.1 + (0.25 * (1.0 - profile.adaptability))
            currentPanicLevel += stressLoad
        } else {
            // DOĞRU YAPTI! Toparlanıyor (Recovery).
            val recoveryRate = 0.05 + (0.15 * profile.adaptability)
            currentPanicLevel -= recoveryRate
        }

        // Sınırları koru
        currentPanicLevel = currentPanicLevel.coerceIn(0.0, 1.0)
    }
}

// Sonucu döndürmek için veri sınıfı (Bunu da buraya ekledik ki hata vermesin)
data class SimulationMoveResult(
    val reactionTimeMs: Double,
    val isCorrect: Boolean
)