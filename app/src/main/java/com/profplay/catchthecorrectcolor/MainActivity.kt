package com.profplay.catchthecorrectcolor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.profplay.catchthecorrectcolor.view.AppNavigation
import com.profplay.catchthecorrectcolor.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    // ViewModel burada oluşturulur ve alt ekranlara dağıtılır
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Tüm yönetim AppNavigation'da
                    AppNavigation(viewModel)
                }
            }
        }
    }
}