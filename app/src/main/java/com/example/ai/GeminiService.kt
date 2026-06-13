package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.abs

data class AiRiskAssessment(
    val score: Int = 55,
    val riskLabel: String = "Medium Risk",
    val confidenceLabel: String = "Medium",
    val reasons: List<String> = listOf(
        "Team form uses available fixture data.",
        "Odds gap is balanced.",
        "Match uncertainty is moderate."
    ),
    val stakeStyle: String = "Balanced",
    val sourceLabel: String = "Offline model"
)

object GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val MODEL = "gemini-2.5-flash"

    private fun getApiKey(): String {
        return try {
            val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
            (field.get(null) as? String).orEmpty().trim().removeSurrounding("\"")
        } catch (_: Exception) {
            ""
        }.takeIf {
            it.isNotBlank() && !it.contains("YOUR_GEMINI", ignoreCase = true)
        }.orEmpty()
    }

    private fun String.normalizeText(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            .matcher(normalized)
            .replaceAll("")
            .lowercase()
            .trim()
    }

    private suspend fun callGemini(prompt: String, fallback: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) return@withContext fallback

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }
            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext fallback
                val json = JSONObject(body)
                json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                    .ifBlank { fallback }
            }
        } catch (_: Exception) {
            fallback
        }
    }

    suspend fun getChatResponse(userInput: String): String {
        val input = userInput.normalizeText()
        return when {
            input.contains("tran dau nao") || input.contains("lich thi dau") || input.contains("co tran gi") -> {
                "Hôm nay FanArena đang hiển thị các trận nổi bật theo dữ liệu trong app. Bạn có thể mở tab Live để lọc Football, Basketball hoặc Tennis."
            }
            input.contains("phan tich") || input.contains("soi keo") -> {
                callGemini(
                    prompt = "Bạn là chuyên gia thể thao FanArena. Phân tích ngắn gọn, cân bằng rủi ro và không khẳng định chắc chắn: $userInput",
                    fallback = "Phân tích nhanh: hãy ưu tiên phong độ gần đây, tình hình lực lượng, lợi thế sân nhà và biến động odds. Không nên all-in nếu dữ liệu trước trận chưa đủ rõ."
                )
            }
            else -> callGemini(
                prompt = "Bạn là trợ lý thể thao FanArena. Trả lời thân thiện, ngắn gọn bằng tiếng Việt: $userInput",
                fallback = "Mình đang chạy ở chế độ offline vì chưa có Gemini API key hợp lệ. Bạn vẫn có thể xem lịch, đặt dự đoán và dùng dữ liệu demo trong app."
            )
        }
    }

    suspend fun getMatchInsight(home: String, away: String, league: String): String =
        callGemini(
            prompt = "Phân tích trận $home vs $away tại $league trong 2 câu tiếng Việt, tập trung phong độ và rủi ro.",
            fallback = "Gemini hiện không phản hồi. App không hiển thị phân tích giả; hãy kiểm tra kết nối hoặc API key rồi thử refresh."
        )

    suspend fun getPredictionExplanation(home: String, away: String, league: String, choice: String): String =
        callGemini(
            prompt = "Tại sao có thể chọn $choice cho trận $home vs $away tại $league? Trả lời cực ngắn bằng tiếng Việt.",
            fallback = "Gemini hiện không phản hồi nên app không tạo giải thích dự đoán giả. Bạn có thể refresh lại khi mạng/API ổn định."
        )

    suspend fun getRiskAssessment(
        home: String,
        away: String,
        league: String,
        sport: String,
        statusText: String,
        homeOdds: String,
        drawOdds: String,
        awayOdds: String
    ): AiRiskAssessment {
        val fallback = buildOfflineRiskAssessment(statusText, homeOdds, drawOdds, awayOdds)
        val response = callGemini(
            prompt = """
                You are FanArena AI. Build a concise prediction risk assessment for $home vs $away in $league ($sport).
                Status: $statusText. Odds: home=$homeOdds, draw=$drawOdds, away=$awayOdds.
                Return only valid JSON with:
                score number 0-100, riskLabel Low Risk/Medium Risk/High Risk,
                confidenceLabel Low/Medium/High, reasons array of exactly 3 short English reasons,
                stakeStyle Conservative/Balanced/Risky.
                Higher score means higher risk. Do not guarantee outcomes.
            """.trimIndent(),
            fallback = riskAssessmentToJson(fallback)
        )
        return parseRiskAssessment(response) ?: fallback
    }

    suspend fun getImprovedPost(content: String, category: String): String {
        return callGemini(
            prompt = "Cải thiện bài viết thể thao chủ đề $category, giữ nguyên ý chính và tiếng Việt tự nhiên: \"$content\"",
            fallback = content.trim().ifBlank { "Chia sẻ góc nhìn của bạn về trận đấu hôm nay." }
        )
    }

    private fun buildOfflineRiskAssessment(
        statusText: String,
        homeOdds: String,
        drawOdds: String,
        awayOdds: String
    ): AiRiskAssessment {
        val home = homeOdds.toCleanDouble()
        val away = awayOdds.toCleanDouble()
        val draw = drawOdds.toCleanDouble()
        val oddsAvailable = home != null && away != null
        val oddsGap = if (home != null && away != null) abs(home - away) else 0.0
        val liveRisk = when {
            statusText.contains("FT", true) || statusText.contains("Finished", true) -> -18
            statusText.contains("LIVE", true) || statusText.contains("'") ||
                statusText.contains("Q", true) || statusText.contains("SET", true) -> 15
            else -> 4
        }
        val oddsRisk = when {
            !oddsAvailable -> 16
            oddsGap < 0.25 -> 22
            oddsGap < 0.75 -> 10
            oddsGap > 1.4 -> -14
            else -> 0
        }
        val drawRisk = when {
            draw == null -> 0
            draw < 3.0 -> 10
            draw > 4.2 -> -4
            else -> 4
        }
        val score = (50 + liveRisk + oddsRisk + drawRisk).coerceIn(5, 95)
        val confidence = when {
            !oddsAvailable -> "Low"
            statusText.contains("LIVE", true) || statusText.contains("'") -> "Medium"
            oddsGap >= 0.75 -> "High"
            else -> "Medium"
        }
        return AiRiskAssessment(
            score = score,
            riskLabel = riskLabelFor(score),
            confidenceLabel = confidence,
            reasons = listOf(
                if (oddsAvailable) "Odds gap is ${if (oddsGap < 0.25) "tight" else "clear"}." else "Odds data is incomplete.",
                if (statusText.isBlank()) "Match status is not confirmed." else "Status signal: $statusText.",
                if (draw != null && draw < 3.0) "Draw price adds uncertainty." else "Market uncertainty is controlled."
            ),
            stakeStyle = stakeStyleFor(score),
            sourceLabel = "Offline model"
        )
    }

    private fun parseRiskAssessment(raw: String): AiRiskAssessment? {
        return try {
            val start = raw.indexOf('{')
            val end = raw.lastIndexOf('}')
            if (start < 0 || end <= start) return null
            val json = JSONObject(raw.substring(start, end + 1))
            val score = json.optInt("score", -1).takeIf { it >= 0 }?.coerceIn(0, 100) ?: return null
            val reasonsJson = json.optJSONArray("reasons")
            val reasons = (0 until (reasonsJson?.length() ?: 0))
                .mapNotNull { reasonsJson?.optString(it)?.takeIf { reason -> reason.isNotBlank() } }
                .take(3)
                .ifEmpty {
                    listOf(
                        "Team form uses available fixture data.",
                        "Odds gap drives the risk score.",
                        "Match status affects confidence."
                    )
                }
            AiRiskAssessment(
                score = score,
                riskLabel = json.optString("riskLabel").takeIf { it in setOf("Low Risk", "Medium Risk", "High Risk") }
                    ?: riskLabelFor(score),
                confidenceLabel = json.optString("confidenceLabel").takeIf { it in setOf("Low", "Medium", "High") }
                    ?: "Medium",
                reasons = reasons,
                stakeStyle = json.optString("stakeStyle").takeIf { it in setOf("Conservative", "Balanced", "Risky") }
                    ?: stakeStyleFor(score),
                sourceLabel = json.optString("sourceLabel").takeIf { it.isNotBlank() } ?: "Gemini"
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun riskAssessmentToJson(assessment: AiRiskAssessment): String {
        return JSONObject().apply {
            put("score", assessment.score)
            put("riskLabel", assessment.riskLabel)
            put("confidenceLabel", assessment.confidenceLabel)
            put("reasons", JSONArray(assessment.reasons))
            put("stakeStyle", assessment.stakeStyle)
            put("sourceLabel", assessment.sourceLabel)
        }.toString()
    }

    private fun riskLabelFor(score: Int): String = when {
        score <= 35 -> "Low Risk"
        score <= 65 -> "Medium Risk"
        else -> "High Risk"
    }

    private fun stakeStyleFor(score: Int): String = when {
        score <= 35 -> "Risky"
        score <= 65 -> "Balanced"
        else -> "Conservative"
    }

    private fun String.toCleanDouble(): Double? = replace("x", "", ignoreCase = true)
        .trim()
        .takeIf { it.isNotBlank() && !it.equals("N/A", true) }
        ?.toDoubleOrNull()
}
