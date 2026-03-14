package dev.korryr.epesa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.korryr.epesa.feature.payment.presentation.PaymentScreen
import dev.korryr.epesa.ui.theme.EpesaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EpesaTheme {
                PaymentScreen()
            }

        }
    }
}
