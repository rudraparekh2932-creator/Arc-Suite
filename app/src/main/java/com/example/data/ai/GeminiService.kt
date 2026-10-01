package com.example.data.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GeminiService(private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private var customApiKey: String? = null

    fun setCustomApiKey(key: String?) {
        customApiKey = key?.trim()?.ifBlank { null }
    }

    private fun getActiveApiKey(): String {
        customApiKey?.let { if (it.isNotBlank()) return it }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    fun isOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    suspend fun generateWorkAssistantResponse(
        prompt: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        forceOffline: Boolean = false
    ): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey()
        val online = isOnline() && !forceOffline

        // If offline or no API key, or force offline mode, use the offline assistant engine
        if (!online || apiKey.isBlank()) {
            val offlineResult = OfflineAssistantEngine.processQuery(prompt)
            val note = if (!online) " [Offline Mode • Room Local Cache]" else " [Local Knowledge Engine • Add Gemini API Key to connect live]"
            return@withContext ChatMessage(
                sender = MessageSender.GEMINI_ASSISTANT,
                text = offlineResult.reply + "\n\n$note",
                detectedFormula = offlineResult.formula ?: extractFormula(offlineResult.reply),
                isOfflineResponse = true
            )
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemInstruction = "You are Arc Copilot, the intelligent everyday work and Excel/Sheets formula assistant built into the Arc 360 Enterprise Suite. " +
                    "Your duties: " +
                    "1. EXCEL / GOOGLE SHEETS FORMULAS: Provide exact, working formulas (e.g., =XLOOKUP(...), =SUMIFS(...), =INDEX(MATCH()), =IFERROR(...), =COUNTIFS(...), financial formulas, and date math). " +
                    "Always display the formula clearly on its own line starting with '=' so it can be easily copied and applied. Explain each parameter in simple terms. " +
                    "2. EVERYDAY WORK ASSISTANT: Draft polite, professional emails, write meeting agendas, summarize documents, rewrite for executive tone, and provide actionable next steps. " +
                    "Keep answers crisp, high-impact, and directly usable in workplace spreadsheets and emails."

            val jsonPayload = JSONObject().apply {
                val contentsArray = JSONArray()

                // Recent conversation context (last 4 messages)
                val recentHistory = conversationHistory.takeLast(4)
                for (msg in recentHistory) {
                    val role = if (msg.sender == MessageSender.USER) "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", msg.text) })
                        })
                    })
                }

                // Add current prompt
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })

                put("contents", contentsArray)

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("topP", 0.9)
                })
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.w("GeminiService", "Gemini API HTTP ${response.code}: $responseString")
                // Fall back gracefully to offline engine
                val offlineResult = OfflineAssistantEngine.processQuery(prompt)
                return@withContext ChatMessage(
                    sender = MessageSender.GEMINI_ASSISTANT,
                    text = offlineResult.reply + "\n\n[Switched to Local Offline Engine • Live service returned ${response.code}]",
                    detectedFormula = offlineResult.formula ?: extractFormula(offlineResult.reply),
                    isOfflineResponse = true
                )
            }

            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val contentObj = candidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val replyText = parts?.optJSONObject(0)?.optString("text")

            if (replyText.isNullOrBlank()) {
                val offlineResult = OfflineAssistantEngine.processQuery(prompt)
                return@withContext ChatMessage(
                    sender = MessageSender.GEMINI_ASSISTANT,
                    text = offlineResult.reply,
                    detectedFormula = offlineResult.formula,
                    isOfflineResponse = true
                )
            }

            val extractedFormula = extractFormula(replyText)
            ChatMessage(
                sender = MessageSender.GEMINI_ASSISTANT,
                text = replyText,
                detectedFormula = extractedFormula,
                isOfflineResponse = false
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Error contacting Gemini: ${e.message}", e)
            val offlineResult = OfflineAssistantEngine.processQuery(prompt)
            ChatMessage(
                sender = MessageSender.GEMINI_ASSISTANT,
                text = offlineResult.reply + "\n\n[Network unavailable • Served from Local Room Offline Engine]",
                detectedFormula = offlineResult.formula ?: extractFormula(offlineResult.reply),
                isOfflineResponse = true
            )
        }
    }

    private fun extractFormula(text: String): String? {
        // Look for `=FUNCTION(...)` pattern
        val pattern = Pattern.compile("(=[A-Z_]+[A-Z0-9_]*\\([^\\n\\r]+\\))")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }

        // Look for formula inside backticks `=...`
        val backtickPattern = Pattern.compile("`(=[^`]+)`")
        val btMatcher = backtickPattern.matcher(text)
        if (btMatcher.find()) {
            return btMatcher.group(1)?.trim()
        }

        return null
    }
}
