package ro.touristsoara.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository pentru generarea de conținut turistic cu Google Gemini API (REST).
 * Respectă cerințele MLH "Best use of Gemini" și TOURISTSOARA_PLAN.md:
 * - Grounding strict pe descrierea oficială oferită de Primărie (fără halucinații)
 * - Răspuns structurat (JSON schema: history, funFact, mustSee)
 * - Cache în memorie (evită apeluri repetate)
 * - Timeout 8 secunde cu fallback automat pe descrierea oficială la offline / eroare / 429
 * - Pre-generare în fundal la confirmarea traseului pentru latență 0 la sosire
 */
class GeminiRepository(
    private var apiKey: String = DEFAULT_API_KEY,
    private val model: String = DEFAULT_MODEL
) {
    companion object {
        private const val TAG = "GeminiRepository"

        // Model recomandat pentru viteză și eficiență în generare structurată
        const val DEFAULT_MODEL = "gemini-1.5-flash"

        // În producție/hackathon, cheia se configurează din local.properties -> BuildConfig
        var DEFAULT_API_KEY: String = ""

        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val TIMEOUT_MS = 8000
    }

    // Cache cu cheia: poiId + "_" + lang + "_" + interests
    private val cache = ConcurrentHashMap<String, AiText>()

    fun setApiKey(newKey: String) {
        this.apiKey = newKey
    }

    /**
     * Obține rezumatul AI pentru un POI.
     * Caută mai întâi în cache; dacă nu există, apelează Gemini API.
     * Dacă eșuează (timeout, offline, rată depășită), returnează un fallback sigur.
     */
    suspend fun getPoiSummary(
        poi: Poi,
        interests: String = "istorie, arhitectură",
        language: String = "ro"
    ): AiText = withContext(Dispatchers.IO) {
        val cacheKey = "${poi.id}_${language}_$interests"
        cache[cacheKey]?.let { return@withContext it }

        // Dacă obiectivul nu are descriere oficială (de ex. adăugat de utilizator)
        val description = if (language.equals("en", ignoreCase = true)) {
            poi.descriptionEn ?: poi.description
        } else {
            poi.description
        }

        if (description.isNullOrBlank()) {
            val ungroundedText = generateUngroundedSummary(poi, interests, language)
            cache[cacheKey] = ungroundedText
            poi.aiSummary = ungroundedText
            return@withContext ungroundedText
        }

        // Apel grounded către Gemini
        try {
            if (apiKey.isBlank()) {
                Log.w(TAG, "Cheia Gemini API nu este setată. Folosim fallback din descrierea oficială.")
                val fallback = buildOfficialFallback(poi, isGrounded = true)
                cache[cacheKey] = fallback
                poi.aiSummary = fallback
                return@withContext fallback
            }

            val aiText = fetchGeminiSummaryFromApi(poi, description, interests, language)
            cache[cacheKey] = aiText
            poi.aiSummary = aiText
            return@withContext aiText
        } catch (e: Exception) {
            Log.e(TAG, "Eroare la apelul Gemini API pentru ${poi.id}: ${e.message}. Activăm fallback.", e)
            val fallback = buildOfficialFallback(poi, isGrounded = true)
            cache[cacheKey] = fallback
            poi.aiSummary = fallback
            return@withContext fallback
        }
    }

    /**
     * Pre-generează rezumatele pentru toate POI-urile dintr-un traseu.
     * Asigură că la sosire (<50m), cardul este afișat instantaneu.
     */
    suspend fun pregenerateSummaries(
        pois: List<Poi>,
        interests: String = "istorie, arhitectură",
        language: String = "ro"
    ) = withContext(Dispatchers.IO) {
        for (poi in pois) {
            try {
                getPoiSummary(poi, interests, language)
            } catch (e: Exception) {
                Log.w(TAG, "Pre-generare eșuată pentru ${poi.id}: ${e.message}")
            }
        }
    }

    /**
     * Efectuează apelul HTTP REST către endpoint-ul Gemini generateContent.
     */
    private fun fetchGeminiSummaryFromApi(
        poi: Poi,
        sourceDescription: String,
        interests: String,
        language: String
    ): AiText {
        val endpointUrl = "$BASE_URL/$model:generateContent"
        val url = URL(endpointUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("x-goog-api-key", apiKey)
            setRequestProperty("User-Agent", "TOURISTSOARA-UniHack8")
        }

        val prompt = buildGroundedPrompt(
            name = if (language.equals("en", ignoreCase = true)) (poi.nameEn ?: poi.name) else poi.name,
            sourceDescription = sourceDescription,
            interests = interests,
            language = if (language.equals("en", ignoreCase = true)) "English" else "Română"
        )

        val requestBodyJson = buildRequestBodyJson(prompt)

        conn.outputStream.use { os ->
            OutputStreamWriter(os, "UTF-8").use { writer ->
                writer.write(requestBodyJson.toString())
                writer.flush()
            }
        }

        val responseCode = conn.responseCode
        if (responseCode == 200) {
            val responseText = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            return parseGeminiJsonResponse(responseText, isGrounded = true)
        } else {
            val errorText = try {
                conn.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
            } catch (_: Exception) { "" }
            throw RuntimeException("Gemini API error code $responseCode: $errorText")
        }
    }

    /**
     * Construiește promptul grounded conform Secțiunii 11.3 din plan.
     */
    private fun buildGroundedPrompt(
        name: String,
        sourceDescription: String,
        interests: String,
        language: String
    ): String {
        return """
            Ești un ghid turistic prietenos în Timișoara. Folosește DOAR textul sursă de mai jos.
            Loc: "$name"
            Text sursă (Ghidul turistic al Primăriei): "$sourceDescription"
            Interese ale vizitatorului: $interests
            Limba răspunsului: $language
            Scrie un card:
            - history: 3-4 propoziții
            - funFact: o curiozitate extrasă din sursă
            - mustSee: o propoziție despre ce merită văzut
            Dacă sursa nu conține o informație, scrie "Informație indisponibilă în sursă".
            Nu adăuga ani, nume sau cifre care nu apar în text. Ton cald, fără exagerări.
        """.trimIndent()
    }

    /**
     * Construiește payload-ul JSON pentru generateContent cu răspuns structurat garantat.
     */
    private fun buildRequestBodyJson(promptText: String): JSONObject {
        val root = JSONObject()

        // contents: [{ parts: [{ text: "..." }] }]
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val textPart = JSONObject().put("text", promptText)
        partsArray.put(textPart)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        root.put("contents", contentsArray)

        // generationConfig: responseMimeType: "application/json" + schema
        val generationConfig = JSONObject()
        generationConfig.put("responseMimeType", "application/json")

        val responseSchema = JSONObject()
        responseSchema.put("type", "OBJECT")
        val properties = JSONObject()
        properties.put("history", JSONObject().put("type", "STRING"))
        properties.put("funFact", JSONObject().put("type", "STRING"))
        properties.put("mustSee", JSONObject().put("type", "STRING"))
        responseSchema.put("properties", properties)

        val required = JSONArray()
        required.put("history")
        required.put("funFact")
        required.put("mustSee")
        responseSchema.put("required", required)

        generationConfig.put("responseSchema", responseSchema)
        root.put("generationConfig", generationConfig)

        return root
    }

    /**
     * Parsează răspunsul JSON returnat de Gemini.
     */
    private fun parseGeminiJsonResponse(rawResponse: String, isGrounded: Boolean): AiText {
        val root = JSONObject(rawResponse)
        val candidates = root.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            throw IllegalStateException("Răspunsul Gemini nu conține candidates")
        }

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val textPart = parts.getJSONObject(0).getString("text")

        // textPart este un string JSON valid conform responseSchema
        val parsedJson = JSONObject(textPart)
        val history = parsedJson.optString("history", "Informație indisponibilă în sursă.")
        val funFact = parsedJson.optString("funFact", "Informație indisponibilă în sursă.")
        val mustSee = parsedJson.optString("mustSee", "Informație indisponibilă în sursă.")

        return AiText(
            history = history,
            funFact = funFact,
            mustSee = mustSee,
            isGrounded = isGrounded
        )
    }

    /**
     * Fallback când nu există descriere oficială (obiectiv adăugat de utilizator).
     */
    private fun generateUngroundedSummary(poi: Poi, interests: String, language: String): AiText {
        return AiText(
            history = if (language.equals("en", ignoreCase = true)) {
                "No verified official description is available for this location."
            } else {
                "Nu avem o descriere oficială verificată pentru acest loc în ghidul Primăriei."
            },
            funFact = if (language.equals("en", ignoreCase = true)) {
                "Added by user or custom tour."
            } else {
                "Obiectiv inclus în tur personalizat."
            },
            mustSee = poi.address ?: poi.name,
            isGrounded = false
        )
    }

    /**
     * Construiește un rezumat de rezervă din descrierea oficială dacă Gemini nu răspunde la timp.
     */
    private fun buildOfficialFallback(poi: Poi, isGrounded: Boolean): AiText {
        val rawDesc = poi.description ?: "Descriere indisponibilă."
        val sentences = rawDesc.split(Regex("(?<=[.!?])\\s+"))
        val historyPart = sentences.take(3).joinToString(" ")
        val funFactPart = if (sentences.size > 3) sentences[3] else "Clădire emblematică din Timișoara."
        val mustSeePart = if (sentences.size > 4) sentences.last() else "Merită admirat pe traseul turistic verde."

        return AiText(
            history = historyPart.ifBlank { rawDesc },
            funFact = funFactPart,
            mustSee = mustSeePart,
            isGrounded = isGrounded
        )
    }
}
