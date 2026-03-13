package dev.korryr.epesa.feature.payment.presentation

sealed interface PaymentUiState {
    data object Idle    : PaymentUiState
    data object Loading : PaymentUiState
    data class  Success(val message: String, val transactionId: String) : PaymentUiState
    data class  Error(val message: String) : PaymentUiState
}
