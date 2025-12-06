package com.profplay.catchthecorrectcolor.model

data class GameState(
    val score: Int = 0,
    val level: Int = 1,
    val timeLeftMs: Long = 10000, // Başlangıç süresi (örn: 10sn)
    val targetColorHex: String = "#FFFFFF", // Hedef renk
    val targetColorName: String = "White",  // Ekranda yazacak isim
    val buttonColors: List<String> = emptyList(), // Butonlardaki renkler
    val isGameOver: Boolean = false
)