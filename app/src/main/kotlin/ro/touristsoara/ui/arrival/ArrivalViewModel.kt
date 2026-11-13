package ro.touristsoara.ui.arrival

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.touristsoara.data.AiText
import ro.touristsoara.data.GeminiRepository
import ro.touristsoara.data.Poi
import ro.touristsoara.logic.GeofenceChecker

/**
 * Starea UI pentru ecranul / dialogul de sosire la obiectiv.
 */
data class ArrivalUiState(
    val currentPoi: Poi? = null,
    val distanceMeters: Double? = null,
    val aiSummary: AiText? = null,
    val isLoadingAi: Boolean = false,
    val isArrivalDialogVisible: Boolean = false,
    val visitedPoiCount: Int = 0,
    val userLat: Double? = null,
    val userLng: Double? = null
)

/**
 * Gestionează logica F4: detectare sosire (<50 m), interogare Gemini și starea dialogului.
 */
class ArrivalViewModel(
    private val geminiRepository: GeminiRepository = GeminiRepository(),
    private val geofenceChecker: GeofenceChecker = GeofenceChecker()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArrivalUiState())
    val uiState: StateFlow<ArrivalUiState> = _uiState.asStateFlow()

    private var activeTourPois: List<Poi> = emptyList()
    var visitorInterests: String = "istorie, arhitectură"
    var language: String = "ro"

    /**
     * Setează lista de obiective incluse în traseul curent.
     * Declanșează pre-generarea automată cu Gemini în fundal (latență 0 la sosire).
     */
    fun setTourPois(pois: List<Poi>) {
        this.activeTourPois = pois
        viewModelScope.launch {
            geminiRepository.pregenerateSummaries(pois, visitorInterests, language)
        }
    }

    /**
     * Actualizează coordonatele curente ale utilizatorului (din GPS sau din simulator).
     * Verifică dacă utilizatorul a intrat în raza de 50 m de un obiectiv nevizitat.
     */
    fun onUserLocationChanged(lat: Double, lng: Double) {
        _uiState.update { it.copy(userLat = lat, userLng = lng) }

        // Dacă un dialog este deja activ, nu deschidem altul
        if (_uiState.value.isArrivalDialogVisible) return

        val arrivalResult = geofenceChecker.checkArrival(lat, lng, activeTourPois)
        if (arrivalResult != null) {
            triggerArrival(arrivalResult.poi, arrivalResult.distanceMeters)
        }
    }

    /**
     * Declanșează manual sosirea la un obiectiv (esențial pentru F7 "Simulează locația" la demo).
     */
    fun simulateArrivalAt(poi: Poi) {
        triggerArrival(poi, distanceMeters = 25.0)
    }

    /**
     * Procesează sosirea la un POI și încarcă descrierea structurată cu Gemini.
     */
    private fun triggerArrival(poi: Poi, distanceMeters: Double) {
        _uiState.update {
            it.copy(
                currentPoi = poi,
                distanceMeters = distanceMeters,
                isArrivalDialogVisible = true,
                isLoadingAi = poi.aiSummary == null,
                aiSummary = poi.aiSummary
            )
        }

        if (poi.aiSummary == null) {
            viewModelScope.launch {
                val summary = geminiRepository.getPoiSummary(poi, visitorInterests, language)
                _uiState.update {
                    if (it.currentPoi?.id == poi.id) {
                        it.copy(aiSummary = summary, isLoadingAi = false)
                    } else {
                        it
                    }
                }
            }
        }
    }

    /**
     * Utilizatorul a încheiat vizita la acest obiectiv și continuă turul.
     */
    fun onContinueRoute() {
        val current = _uiState.value.currentPoi
        if (current != null) {
            geofenceChecker.markVisited(current.id)
        }

        _uiState.update {
            it.copy(
                isArrivalDialogVisible = false,
                currentPoi = null,
                distanceMeters = null,
                aiSummary = null,
                visitedPoiCount = geofenceChecker.getVisitedCount()
            )
        }
    }

    /**
     * Închide dialogul fără a marca obiectivul ca vizitat (utilizatorul doar a ascuns cardul).
     */
    fun onDismissDialog() {
        _uiState.update { it.copy(isArrivalDialogVisible = false) }
    }

    /**
     * Re-deschide cardul obiectivului curent dacă a fost minimizat.
     */
    fun reopenArrivalDialog() {
        if (_uiState.value.currentPoi != null) {
            _uiState.update { it.copy(isArrivalDialogVisible = true) }
        }
    }

    /**
     * Resetează starea pentru un traseu nou.
     */
    fun resetTour() {
        geofenceChecker.resetVisited()
        _uiState.value = ArrivalUiState()
    }
}
