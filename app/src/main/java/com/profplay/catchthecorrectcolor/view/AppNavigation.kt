package com.profplay.catchthecorrectcolor.view

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.profplay.catchthecorrectcolor.model.AppSettings
import com.profplay.catchthecorrectcolor.viewmodel.GameViewModel

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "menu") {

        // 1. ANA MENÜ
        composable("menu") {
            MainMenuScreen(
                viewModel = viewModel, // ViewModel'i içeri gönderiyoruz (JSON için lazım)
                onStartNewSim = {
                    // DÜZELTME: Artık İnsan Oyunu DEĞİL, Monte Carlo Simülasyonu başlıyor
                    viewModel.runMonteCarloNorming(1000) // Test için 1000 ajan yeterli
                    navController.navigate("game")
                    Toast.makeText(context, "Monte Carlo Başlatıldı...", Toast.LENGTH_SHORT).show()
                },
                onLoadJson = {
                    // Bu butonun işlevi MainMenuScreen içinde tanımlı (Launcher orada)
                    // Buraya boş bırakıyoruz
                },
                onSingleAgentTest = {
                    // DÜZELTME: Stres Testi Başlatılıyor
                    if (viewModel.hasNormData() || AppSettings.loadedPopulation != null) {
                        viewModel.runNoiseStressTest() // Parametreleri Config'den alır
                        navController.navigate("game")
                        Toast.makeText(context, "Stres Testi Başlıyor...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Önce Veri Üretin veya Yükleyin!", Toast.LENGTH_LONG).show()
                    }
                },
                onSettings = {
                    navController.navigate("settings")
                },
                onStartHumanGame = {
                    // İnsan modu için yeni bir callback ekledim (MainMenuScreen'de kullanacağız)
                    viewModel.startGame()
                    navController.navigate("game")
                }
            )
        }

        // 2. OYUN EKRANI (Simülasyonu İzlediğimiz Yer)
        composable("game") {
            GameScreen(
                viewModel = viewModel,
                onBackToMenu = {
                    navController.popBackStack()
                }
            )
        }

        // 3. AYARLAR EKRANI
        composable("settings") {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}