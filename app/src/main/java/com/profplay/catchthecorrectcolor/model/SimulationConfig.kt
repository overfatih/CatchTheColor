package com.profplay.catchthecorrectcolor.model

// Tüm ayarların tutulduğu veri sınıfı (Varsayılan değerler senin Final Ayarların olsun)
data class SimulationConfig(
    // Popülasyon Ayarları
    var agentCount: Int = 10000,

    // DNA Sınırları (Sliderlar burayı değiştirecek)
    var minSpeed: Double = 300.0,
    var maxSpeed: Double = 800.0,
    var speedVariance: Double = 75.0,

    var baseErrorRate: Double = 0.040,
    var errorVariance: Double = 0.020,

    var baseGrit: Double = 0.55,
    var gritVariance: Double = 0.18,

    var baseFocus: Double = 0.85,       // Odaklanma (0.0 - 1.0)
    var baseBoredom: Double = 40.0,     // Sıkılma Eşiği
    // Simülasyon Zorluğu
    var stressDivisor: Double = 14000.0, // Duvar yüksekliği
    var isTurboMode: Boolean = true      // Grand Master Modu
)

// Uygulama genelinde erişilebilecek Tekil Nesne (Singleton)
object AppSettings {
    var currentConfig = SimulationConfig()
    var loadedPopulation: List<Agent>? = null // JSON'dan yüklenen veriyi burada tutacağız
}