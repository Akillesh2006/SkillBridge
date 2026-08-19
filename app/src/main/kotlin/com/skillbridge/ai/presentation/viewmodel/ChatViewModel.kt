package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Hi Akillesh 👋\nI'm your personal SkillBridge AI career assistant.\n\nI can help you with:\n• Your skill gaps\n• Your personalized learning roadmap\n• Technical interview preparation\n• Coding questions & career guidance",
            isUser = false
        )
    ),
    val isThinking: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _uiState.value.isThinking) return

        val userMessage = ChatMessage(text = trimmed, isUser = true)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isThinking = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(trimmed)
            result.onSuccess { reply ->
                val aiMessage = ChatMessage(text = reply, isUser = false)
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages + aiMessage,
                        isThinking = false,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update { state ->
                    state.copy(
                        isThinking = false,
                        errorMessage = err.message ?: "SkillBridge AI is temporarily unavailable. Please try again."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
