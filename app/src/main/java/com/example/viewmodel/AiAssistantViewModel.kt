package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiAssistantUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("Xin chào! Tôi là trợ lý FanArena AI. Bạn muốn tìm hiểu gì về các trận đấu hôm nay?", false)
    ),
    val inputText: String = "",
    val isLoading: Boolean = false
)

class AiAssistantViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val currentInput = _uiState.value.inputText
        if (currentInput.isBlank() || _uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(currentInput, true),
                inputText = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val responseText = GeminiService.getChatResponse(currentInput)
                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(responseText, false),
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage("Xin lỗi, tôi gặp sự cố khi kết nối AI. Bạn thử lại sau nhé.", false),
                        isLoading = false
                    )
                }
            }
        }
    }
}
