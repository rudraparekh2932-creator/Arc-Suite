package com.example.data.ai

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val detectedFormula: String? = null,
    val isOfflineResponse: Boolean = false
)

enum class MessageSender {
    USER,
    GEMINI_ASSISTANT
}

data class QuickPrompt(
    val title: String,
    val prompt: String,
    val category: PromptCategory
)

enum class PromptCategory {
    EXCEL_FORMULA,
    WORK_EMAIL,
    DOCUMENT_EDIT,
    OFFLINE_TOOL
}
