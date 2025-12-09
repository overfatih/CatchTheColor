package com.profplay.catchthecorrectcolor.viewmodel

import com.profplay.catchthecorrectcolor.model.AIProfile
import kotlin.random.Random

class AIPlayer(val profile: AIProfile) {

    /**
     * Hamle Yap (Simülasyonun Kalbi)
     * @param currentLevel: Oyunun zorluk seviyesi
     * @param environmentNoise: Ortam gürültüsü (0.0 ile 1.0 arası)
     * @return Pair<GeçenSüre, DoğruMu>
     */
    fun makeMove(currentLevel: Int, environmentNoise: Double): SimulationMoveResult {

        // 1. TEPKİ SÜRESİ HESAPLAMA (Matematiksel Modelleme)
        // Formül: T = Base - (Level * Focus) + (Noise * Resistance) + RandomVariation

        // Seviye arttıkça hızlanmalı (Learning Curve) ama profile göre değişir
        val levelEffect = currentLevel * profile.focusStability * 2.0 // Her level ~2ms hızlandırır/yavaşlatır

        val adaptationBonus = (currentLevel * 0.01 * profile.adaptability).coerceAtMost(0.5)
        // Örnek: Level 50'de, adaptability 0.8 ise -> %40 gürültü azaltma kazanır.

        val effectiveNoise = environmentNoise * (1.0 - adaptationBonus)

        // Gürültü etkisi (Stres) artık effectiveNoise üzerinden hesaplanır
        val noiseEffect = effectiveNoise * profile.noiseResistance * 100.0

        // İnsan doğası gereği rastgele varyasyon (Gaussian Noise - Standart Sapma)
        val humanVariation = Random.nextDouble(-20.0, 20.0) // +/- 20ms sapma

        // Sonuç Süre (ms cinsinden)
        var calculatedTime = profile.baseReflexTime - levelEffect + noiseEffect + humanVariation

        // Süre asla 100ms'in altına inemez (İnsan fizyolojisi sınırı)
        if (calculatedTime < 100.0) calculatedTime = 100.0

        // 2. DOĞRU KARAR VERME OLASILIĞI
        // Hata yapma şansı: Profilin hataya yatkınlığı + Gürültü stresi
        val errorChance = profile.errorProneFactor + (effectiveNoise * 0.1)

        // Zar atıyoruz: Gelen sayı errorChance'ten büyükse DOĞRU, küçükse YANLIŞ basar
        val isCorrectMove = Random.nextDouble(0.0, 1.0) > errorChance

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