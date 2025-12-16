package com.profplay.catchthecorrectcolor.viewmodel

import com.profplay.catchthecorrectcolor.model.Agent
import com.profplay.catchthecorrectcolor.model.AgentDNA
import com.profplay.catchthecorrectcolor.model.AgentPerformance
import com.profplay.catchthecorrectcolor.model.AppSettings

// SingleAgentViewModel.kt
// SingleAgentViewModel.kt veya ilgili fonksiyonun içi

fun runSingleAgentTest() {
    // 1. Güncel Ayarları Çek
    val config = AppSettings.currentConfig

    // 2. DNA Oluştur (Eksik parametreleri Config'den tamamladık)
    val testDna = AgentDNA(
        baseSpeed = config.minSpeed,      // Slider'dan gelen Hız değeri
        grit = config.baseGrit,           // Slider'dan gelen Sebat değeri
        errorRate = config.baseErrorRate, // Slider'dan gelen Hata değeri
        focus = config.baseFocus,         // Slider'dan gelen Odak değeri
        boredomThresh = config.baseBoredom // Slider'dan gelen Sıkılma değeri
    )

    // 3. Ajan Nesnesi Yarat
    val testAgent = Agent(
        id = "TEST-SUBJECT-001",
        dna = testDna,
        performance = AgentPerformance(0, 0.0, 0.0) // Test olduğu için performans sıfır
    )

    // 4. Oyuna Gönderilecek Profile Dönüştür
    val gameProfile = testAgent.toAIProfile()

    // 5. Oyunu Başlat (Burada kendi navigasyon kodun olacak)
    // startGameWithProfile(gameProfile)
    println("Test Ajanı Hazır: $gameProfile")
}