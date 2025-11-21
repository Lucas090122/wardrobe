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

// Allowed clothing categories that the model is expected to output
private val ALLOWED_CATEGORIES = setOf("TOP", "PANTS", "SHOES", "HAT")

// Allowed color palette (kept in sync with the rest of the project)
private val ALLOWED_COLORS = listOf(
    "#FFFFFF", "#000000", "#FF0000", "#FFA500",
    "#FFFF00", "#00FF00", "#00FFFF", "#0000FF",
    "#800080", "#A52A2A"
)

data class GeminiClothingResult(
    val category: String,   // "TOP" / "PANTS" / "SHOES" / "HAT"
    val description: String,
    val warmthLevel: Int,   // 1..5
    val colorHex: String    // Hex color chosen from ALLOWED_COLORS
)

/**
 * Simple singleton wrapper around the Gemini model used for clothing analysis.
 *
 * This class is intentionally stateless from the outside: callers only pass a Bitmap
 * and receive a structured [GeminiClothingResult] or null on failure.
 */
object GeminiAiService {

    // Lazily create a Gemini model instance.
    // Using lazy means the model is only created when the feature is actually used.
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    /**
     * Sends a clothing photo to Gemini and asks it to extract structured information:
     *
     * - category: TOP / PANTS / SHOES / HAT
     * - description: short English description
     * - warmthLevel: integer 1..5
     * - colorHex: closest color from [ALLOWED_COLORS]
     *
     * All heavy work (model call + JSON parsing) runs on [Dispatchers.IO].
     * Returns `null` if anything goes wrong (network error, JSON parsing, etc.).
     */
    suspend fun analyzeClothing(bitmap: Bitmap): GeminiClothingResult? =
        withContext(Dispatchers.IO) {
            try {
                val prompt = content {
                    // Provide the image for multimodal analysis
                    image(bitmap)

                    // Ask very explicitly for a single JSON object with a strict schema
                    text(
                        """
                        You are an assistant for a wardrobe app.
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

                // 1. Extract the first JSON object {...} from the response text
                val jsonText = extractFirstJsonObject(raw) ?: run {
                    Log.w(TAG, "Failed to extract JSON object from response")
                    return@withContext null
                }

                Log.d(TAG, "Parsed JSON text: $jsonText")

                // 2. Parse JSON and normalize individual fields
                val obj = JSONObject(jsonText)

                val rawDescription = obj.optString("description").ifBlank { "Clothing item" }

                // Normalize category and provide a reasonable fallback
                val rawCategory = obj.optString("category")
                val category = normalizeCategory(rawCategory, rawDescription)

                // Ensure warmthLevel is in the expected range 1..5
                val warmth = obj.optInt("warmthLevel", 3).let {
                    if (it in 1..5) it else 3
                }

                // Normalize color: if the model returns an unknown color,
                // map it to the closest one from ALLOWED_COLORS.
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
     * Try to safely extract the first `{ ... }` JSON object from the model's raw response.
     *
     * The model might sometimes wrap the JSON in ```json ...``` fences or add extra text.
     * This helper:
     *  1. Strips obvious fences.
     *  2. Scans for the first balanced `{ ... }` pair and returns that substring.
     */
    private fun extractFirstJsonObject(raw: String): String? {
        // Strip leading/trailing code fences such as ```json ... ```
        val cleaned = raw
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        // If the cleaned text already looks like a single JSON object, use it directly
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            return cleaned
        }

        // Otherwise, scan the string and find the first balanced {...} region
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
     * Normalize the category returned by the model.
     *
     * Strategy:
     *  1. If the raw category is already one of the allowed values, use it.
     *  2. Otherwise, inspect both the raw category and description text to guess
     *     whether it is shoes/pants/hat using keyword matching.
     *  3. If we still cannot guess, fall back to "TOP".
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
     * Normalize the color returned by the model.
     *
     * - If the original string is already in [ALLOWED_COLORS], return it.
     * - If it is another valid HEX color (#RRGGBB), compute the distance to each allowed color
     *   in RGB space and pick the closest one.
     * - If parsing fails, default to white ("#FFFFFF").
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
     * Parse a `#RRGGBB` hex string into a Triple(r, g, b).
     * Returns null if the format is invalid.
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
     * Squared Euclidean distance between two colors in RGB space.
     *
     * We use the squared distance instead of the real Euclidean distance
     * because the ordering is the same and avoids calling `sqrt`.
     */
    private fun colorDistanceSq(a: Triple<Int, Int, Int>, b: Triple<Int, Int, Int>): Double {
        val dr = (a.first - b.first).toDouble()
        val dg = (a.second - b.second).toDouble()
        val db = (a.third - b.third).toDouble()
        return dr.pow(2) + dg.pow(2) + db.pow(2)
    }
}