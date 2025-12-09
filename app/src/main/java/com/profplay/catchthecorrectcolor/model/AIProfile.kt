package com.profplay.catchthecorrectcolor.model

data class AIProfile(
    val typeName: String,

    // Bilişsel (Cognitive)
    val baseReflexTime: Double,
    val focusStability: Double,
    val noiseResistance: Double,
    val errorProneFactor: Double,

    // Duyuşsal (Affective/Psycho) - YENİ
    val grit: Double,             // Azim (0.0 - 1.0)
    val boredomThreshold: Double, // Sıkılma Eşiği (Yüksekse çabuk sıkılır)
    val fatigueRate: Double,      // Yorulma Hızı (Oyun başına düşen motivasyon)
    val adaptability: Double,     // 0.0 - 1.0 (Yüksekse gürültüye zamanla alışır)
    val description: String = ""
)