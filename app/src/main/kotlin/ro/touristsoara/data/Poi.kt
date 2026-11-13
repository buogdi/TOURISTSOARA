package ro.touristsoara.data

/**
 * Model de date pentru un Punct de Interes (POI), conform contractului din TOURISTSOARA_PLAN.md (Secțiunea 5).
 *
 * @property id Identificator unic (ex. "catedrala-mitropolitana")
 * @property name Denumirea oficială în limba română
 * @property nameEn Denumirea în limba engleză (opțională)
 * @property description Descrierea oficială din ghidul turistic al Primăriei
 * @property descriptionEn Descrierea în limba engleză (opțională)
 * @property address Adresa fizică
 * @property imagePath Calea relativă către imagine (ex. "/pois/catedrala-mitropolitana.jpg")
 * @property lat Latitudine geografică
 * @property lng Longitudine geografică
 * @property visitMinutes Durata recomandată a vizitei în minute
 * @property source Sursa datelor: "city" | "team" | "user"
 * @property enabled Indică dacă obiectivul este inclus în tur
 * @property aiSummary Rezumatul generat de Gemini (istorie, curiozitate, must-see)
 */
data class Poi(
    val id: String,
    val name: String,
    val nameEn: String? = null,
    val description: String? = null,
    val descriptionEn: String? = null,
    val address: String? = null,
    val imagePath: String? = null,
    val lat: Double,
    val lng: Double,
    val visitMinutes: Int = 10,
    val source: String = "city",
    var enabled: Boolean = true,
    var aiSummary: AiText? = null
)

/**
 * Text structurat generat de Gemini conform cerințelor challenge-ului MLH "Best use of Gemini".
 *
 * @property history 3-4 propoziții despre istoria locului
 * @property funFact O curiozitate sau detaliu inedit extras exclusiv din textul sursă
 * @property mustSee Recomandare scurtă despre ce merită văzut
 * @property isGrounded True dacă rezumatul a fost ancorat (grounded) în descrierea oficială a orașului
 */
data class AiText(
    val history: String,
    val funFact: String,
    val mustSee: String,
    val isGrounded: Boolean = true
)
