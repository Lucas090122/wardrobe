package com.example.wardrobe.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.example.wardrobe.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.pow

private const val TAG = "GeminiAiService"

// 模型允许的类别集合
private val ALLOWED_CATEGORIES = setOf("TOP", "PANTS", "SHOES", "HAT")

// 允许的颜色列表（和项目里保持一致）
private val ALLOWED_COLORS = listOf(
    "#FFFFFF", "#000000", "#FF0000", "#FFA500",
    "#FFFF00", "#00FF00", "#00FFFF", "#0000FF",
    "#800080", "#A52A2A"
)

data class GeminiClothingResult(
    val category: String,   // "TOP" / "PANTS" / "SHOES" / "HAT"
    val description: String,
    val warmthLevel: Int,   // 1..5
    val colorHex: String    // 从 ALLOWED_COLORS 中选一个
)

object GeminiAiService {

    // 创建一个 Gemini 模型实例
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    /**
     * 把衣服照片发给 Gemini，让它帮我们识别：
     * - category: TOP / PANTS / SHOES / HAT
     * - description: 英文简短描述
     * - warmthLevel: 1..5
     * - colorHex: 从 ALLOWED_COLORS 里选一个最接近的
     *
     * 失败时返回 null
     */
    suspend fun analyzeClothing(bitmap: Bitmap): GeminiClothingResult? =
        withContext(Dispatchers.IO) {
            try {
                val prompt = content {
                    // 图片
                    image(bitmap)

                    // 非常明确地要求只返回 JSON
                    text(
                        """
                        You are an assistant for a kids' wardrobe app.
                        Look at the clothing item in the photo and extract structured information.

                        Return ONLY a single strict JSON object.
                        - No markdown.
                        - No ``` fences.
                        - No comments.
                        - No explanations.
                        - No extra keys.

                        JSON fields:
                        - category: one of ["TOP", "PANTS", "SHOES", "HAT"]
                        - description: a short English description of the main clothing item
                        - warmthLevel: an integer from 1 to 5 
                          (1 = very thin / summer, 5 = very thick / winter)
                        - colorHex: the closest color from this list:
                          ${ALLOWED_COLORS.joinToString(", ", "[", "]")}

                        Example of the EXACT format:
                        {
                          "category": "TOP",
                          "description": "Blue down jacket 110cm",
                          "warmthLevel": 4,
                          "colorHex": "#0000FF"
                        }
                        """.trimIndent()
                    )
                }

                val response = model.generateContent(prompt)
                val raw = response.text?.trim() ?: run {
                    Log.w(TAG, "Gemini response text is null")
                    return@withContext null
                }

                Log.d(TAG, "Raw Gemini response: $raw")

                // 1️⃣ 先从返回文本中“抠出”第一段 JSON 对象
                val jsonText = extractFirstJsonObject(raw) ?: run {
                    Log.w(TAG, "Failed to extract JSON object from response")
                    return@withContext null
                }

                Log.d(TAG, "Parsed JSON text: $jsonText")

                // 2️⃣ 解析 JSON
                val obj = JSONObject(jsonText)

                val rawDescription = obj.optString("description").ifBlank { "Clothing item" }

                // 类别规范化 + 兜底逻辑
                val rawCategory = obj.optString("category")
                val category = normalizeCategory(rawCategory, rawDescription)

                // 保证 warmthLevel 为 1..5
                val warmth = obj.optInt("warmthLevel", 3).let {
                    if (it in 1..5) it else 3
                }

                // 颜色：如果模型给了奇怪的颜色，自动映射到最近的那个
                val rawColor = obj.optString("colorHex").uppercase()
                val color = normalizeColor(rawColor)

                GeminiClothingResult(
                    category = category,
                    description = rawDescription,
                    warmthLevel = warmth,
                    colorHex = color
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error calling Gemini", e)
                null
            }
        }

    /**
     * 从模型返回的字符串中，尽量安全地提取出第一段 {...} JSON 对象。
     */
    private fun extractFirstJsonObject(raw: String): String? {
        // 先去掉 ```json ``` 之类的包裹
        val cleaned = raw
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        // 如果本身就是以 { 开头、以 } 结尾，就直接用
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            return cleaned
        }

        // 否则就从整段文本里找到第一个 { ... } 的平衡区域
        var depth = 0
        var startIndex = -1
        for (i in cleaned.indices) {
            when (cleaned[i]) {
                '{' -> {
                    if (depth == 0) {
                        startIndex = i
                    }
                    depth++
                }

                '}' -> {
                    if (depth > 0) {
                        depth--
                        if (depth == 0 && startIndex != -1) {
                            return cleaned.substring(startIndex, i + 1).trim()
                        }
                    }
                }
            }
        }

        return null
    }

    /**
     * 把模型返回的 category 做一层“纠错 + 推断”：
     * - 先看有没有刚好是 TOP/PANTS/SHOES/HAT
     * - 如果不是，就根据文本里是不是出现了 shoes/pants/hat 等词来猜
     * - 实在猜不到，就当 TOP
     */
    private fun normalizeCategory(rawCategory: String?, description: String): String {
        val up = rawCategory.orEmpty().trim().uppercase()
        if (up in ALLOWED_CATEGORIES) return up

        val text = (rawCategory.orEmpty() + " " + description).lowercase()

        return when {
            listOf("shoe", "sneaker", "boot", "sandals", "slipper").any { it in text } -> "SHOES"
            listOf("pant", "trouser", "jean", "leggings", "shorts").any { it in text } -> "PANTS"
            listOf("hat", "cap", "beanie", "hood").any { it in text } -> "HAT"
            else -> "TOP"
        }
    }

    /**
     * 颜色校正：
     * - 如果本来就在 ALLOWED_COLORS 里，直接用
     * - 如果是别的 HEX 颜色（#RRGGBB），算和每个允许颜色的距离，选最近的
     * - 解析失败就默认白色
     */
    private fun normalizeColor(rawColor: String?): String {
        val color = rawColor.orEmpty().uppercase().trim()
        if (color in ALLOWED_COLORS) return color

        val src = parseHexColor(color) ?: return "#FFFFFF"

        var bestColor = "#FFFFFF"
        var bestDist = Double.MAX_VALUE

        for (c in ALLOWED_COLORS) {
            val target = parseHexColor(c) ?: continue
            val dist = colorDistanceSq(src, target)
            if (dist < bestDist) {
                bestDist = dist
                bestColor = c
            }
        }

        return bestColor
    }

    /**
     * 把 #RRGGBB 转成 Triple(r, g, b)，解析失败返回 null
     */
    private fun parseHexColor(hex: String): Triple<Int, Int, Int>? {
        val cleaned = hex.removePrefix("#")
        if (cleaned.length != 6) return null
        return try {
            val r = cleaned.substring(0, 2).toInt(16)
            val g = cleaned.substring(2, 4).toInt(16)
            val b = cleaned.substring(4, 6).toInt(16)
            Triple(r, g, b)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 颜色欧式距离的平方（不需要开方，比较相对大小即可）
     */
    private fun colorDistanceSq(a: Triple<Int, Int, Int>, b: Triple<Int, Int, Int>): Double {
        val dr = (a.first - b.first).toDouble()
        val dg = (a.second - b.second).toDouble()
        val db = (a.third - b.third).toDouble()
        return dr.pow(2) + dg.pow(2) + db.pow(2)
    }
}