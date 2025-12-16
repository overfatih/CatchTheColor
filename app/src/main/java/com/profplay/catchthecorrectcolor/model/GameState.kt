package com.profplay.catchthecorrectcolor.model

data class GameState(
    // Bilişsel Performans Verileri
    val score: Double = 0.0,         // Anlık Skor (Double: Daha hassas ölçüm için)
    val puan: Double = 0.0,          // Kümülatif/Ağırlıklı Puan
    val level: Int = 1,

    // Zamanlama
    val elapsedTime: Double = 0.0,   // Geçen süre (cs cinsinden olabilir)
    val timeLeftMs: Long = 0,        // Geri sayım (Eğer kullanılıyorsa)

    // UI Durumu
    val targetColorName: String = "Hazır mısın?", // Ekranda yazan "Kırmızı", "Mavi" vb.
    val buttonColors: List<Int> = emptyList(),    // DİKKAT: Int (Android Color kodu) olmalı

    // Oyun Akışı
    val isPlaying: Boolean = false,    // Oyun şu an oynanıyor mu? (Compose buna bakıyor)
    val isGameOver: Boolean = false,   // Oyun bitti mi?
    val gameOverMessage: String = ""   // Bitiş mesajı
)