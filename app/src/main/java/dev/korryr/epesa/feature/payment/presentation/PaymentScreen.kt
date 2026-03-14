package dev.korryr.epesa.feature.payment.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.korryr.epesa.feature.payment.presentation.components.PaymentResultDialog
import dev.korryr.epesa.feature.payment.presentation.components.StkPushDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showPayDialog by remember { mutableStateOf(false) }
    var phoneNumber   by remember { mutableStateOf("") }

    val resultMessage: String? = when (val s = uiState) {
        is PaymentUiState.Success -> "✅ ${s.message}\n\nTransaction ID: ${s.transactionId}"
        is PaymentUiState.Error   -> "❌ ${s.message}"
        else                      -> null
    }

    if (showPayDialog) {
        StkPushDialog(
            phoneNumber   = phoneNumber,
            onPhoneChange = { phoneNumber = it },
            isLoading     = uiState is PaymentUiState.Loading,
            onConfirm     = { amount -> viewModel.initiatePayment(phoneNumber, amount) },
            onDismiss     = {
                showPayDialog = false
                viewModel.resetState()
            }
        )
    }

    resultMessage?.let { msg ->
        PaymentResultDialog(
            message   = msg,
            onDismiss = {
                showPayDialog = false
                viewModel.resetState()
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("M-Pesa") }) }
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Button(
                onClick  = { showPayDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            ) {
                Text("Pay with M-Pesa")
            }
        }
    }
}