package com.profplay.catchthecorrectcolor.model

import com.google.gson.annotations.SerializedName

// 1. Ana Taşıyıcı Sınıf (Python'daki her bir {} bloğu)
data class Agent(
    @SerializedName("id") val id: String,
    @SerializedName("dna") val dna: AgentDNA,
    @SerializedName("performance") val performance: AgentPerformance
) {
    // Bu fonksiyon, yüklenen JSON verisini senin oyun motorunun
    // anlayacağı 'AIProfile' nesnesine dönüştürür.
    fun toAIProfile(): AIProfile {
        return AIProfile(
            typeName = "Simulated Agent ($id)", // İsim yerine ID

            // Mapping (Eşleştirme) Burada Yapılıyor:
            baseReflexTime = dna.baseSpeed,
            errorProneFactor = dna.errorRate,
            grit = dna.grit,
            focusStability = dna.focus,
            boredomThreshold = dna.boredomThresh,

            // Python'da olmayan ama oyunda zorunlu olan alanlar için varsayılanlar:
            noiseResistance = 0.5,
            fatigueRate = 0.05,
            adaptability = 0.5,
            description = "Imported from Thesis Simulation"
        )
    }
}

// 2. DNA Yapısı (Python JSON'ındaki isimlerle birebir aynı olmalı)
data class AgentDNA(
    @SerializedName("baseSpeed") val baseSpeed: Double,
    @SerializedName("grit") val grit: Double,
    @SerializedName("focus") val focus: Double,
    @SerializedName("errorRate") val errorRate: Double,
    @SerializedName("boredomThresh") val boredomThresh: Double
)

// 3. Performans Verisi (Eğer oyunda 'Ajanın geçmişi'ni göstermek istersen)
data class AgentPerformance(
    @SerializedName("totalGamesPlayed") val totalGamesPlayed: Int,
    @SerializedName("consistentScore") val consistentScore: Double,
    @SerializedName("peakScore") val peakScore: Double
)