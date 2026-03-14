package dev.korryr.epesa.feature.payment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.korryr.epesa.feature.payment.domain.repository.MpesaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: MpesaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun initiatePayment(phoneNumber: String, amount: String) {
        if (!isValidPhone(phoneNumber)) {
            _uiState.value = PaymentUiState.Error("Enter a valid Safaricom number.")
            return
        }
        if (!isValidAmount(amount)) {
            _uiState.value = PaymentUiState.Error("Enter a valid amount greater than 0.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            repository.initiateStkPush(phoneNumber, amount)
                .onSuccess { result ->
                    _uiState.value = PaymentUiState.Success(
                        message       = result.customerMessage,
                        transactionId = result.transactionId
                    )
                }
                .onFailure { error ->
                    _uiState.value = PaymentUiState.Error(
                        error.message ?: "Something went wrong. Please try again."
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = PaymentUiState.Idle
    }

    private fun isValidPhone(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.startsWith("254") -> digits.length == 12
            digits.startsWith("07") || digits.startsWith("01") -> digits.length == 10
            digits.startsWith("7")  || digits.startsWith("1")  -> digits.length == 9
            else -> false
        }
    }

    private fun isValidAmount(amount: String): Boolean =
        amount.toDoubleOrNull()?.let { it > 0 } ?: false
}