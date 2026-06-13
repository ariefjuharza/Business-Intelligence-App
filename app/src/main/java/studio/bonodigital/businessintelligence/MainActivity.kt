package studio.bonodigital.businessintelligence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import studio.bonodigital.businessintelligence.data.repository.BiRepository
import studio.bonodigital.businessintelligence.ui.navigation.MainNavigation
import studio.bonodigital.businessintelligence.ui.theme.BusinessIntelligenceTheme

/**
 * Titik masuk utama aplikasi Business Intelligence.
 *
 * Activity ini bertanggung jawab untuk menginisialisasi repositori inti aplikasi,
 * mengonfigurasi pengaturan tampilan edge-to-edge, dan menampung struktur UI
 * Jetpack Compose termasuk tema global dan grafik navigasi utama.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = BiRepository(applicationContext)

        setContent {
            BusinessIntelligenceTheme {
                MainNavigation(repository = repository)
            }
        }
    }
}