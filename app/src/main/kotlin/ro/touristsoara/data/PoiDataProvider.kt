package ro.touristsoara.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ro.touristsoara.model.AiText
import ro.touristsoara.model.Interest
import ro.touristsoara.model.Language
import ro.touristsoara.model.Poi
import ro.touristsoara.model.RouteResult
import ro.touristsoara.model.TravelMode
import kotlin.math.roundToInt

object PoiDataProvider {

    private val initialPois = listOf(
        Poi(
            id = "catedrala-mitropolitana",
            name = "Catedrala Mitropolitană Ortodoxă",
            nameEn = "Orthodox Metropolitan Cathedral",
            address = "Bulevardul Regele Ferdinand I",
            description = "Construită între 1936 și 1946, catedrala îmbină stilul tradițional moldovenesc cu cel bizantin. Prin suplețea proporțiilor și eleganța compoziției a devenit imediat unul dintre simbolurile orașului. Având o înălțime de 90 m, a fost până în 2018 cea mai înaltă biserică din România.",
            descriptionEn = "Built between 1936 and 1946, the cathedral combines traditional Moldavian and Byzantine styles. With its supple proportions and elegant composition, it immediately became one of the symbols of the city. With a height of 90 m, it was until 2018 the tallest church in Romania.",
            imagePath = "catedrala-mitropolitana.jpg",
            lat = 45.7507,
            lng = 21.22423,
            visitMinutes = 20,
            source = "city",
            enabled = true,
            aiSummary = AiText(
                history = "Catedrala a fost ridicată în perioada 1936-1946, combinând armonios arhitectura moldovenească cu elemente bizantine. A devenit rapid emblema spirituală a Timișoarei și un punct de reper vizibil din tot centrul.",
                funFact = "Cele 7 clopote au o greutate totală de aproape 8 tone și au fost armonizate de compozitorul Sabin Drăgoi.",
                mustSee = "Admiră cele 11 turle acoperite cu țiglă smălțuită în motive populare bănățene și colecția de artă bisericească veche de la subsol."
            )
        ),
        Poi(
            id = "palatul-culturii",
            name = "Palatul Culturii (Opera & Teatrul Național)",
            nameEn = "Palace of Culture (Opera)",
            address = "Piața Victoriei / Str. Mărășești nr. 2",
            description = "Construită în stil renascentist, după proiectul arhitecților vienezi Helmer și Fellner, clădirea a fost inaugurată în 1875. În 20 decembrie 1989, de la balconul clădirii, Timișoara este proclamată primul oraș liber de comunism.",
            descriptionEn = "Inaugurated in 1875 in Renaissance style by Helmer and Fellner. On December 20, 1989, from its balcony, Timișoara was proclaimed the first Romanian city free of communism.",
            imagePath = "palatul-culturii.jpg",
            lat = 45.75441,
            lng = 21.22612,
            visitMinutes = 15,
            source = "city",
            enabled = true,
            aiSummary = AiText(
                history = "Inaugurat în 1875, edificiul a renăscut după două mari incendii, primind celebra fațadă monumentală cu arc de triumf. Este unic în Europa prin găzduirea a 4 instituții de teatru și operă în trei limbi.",
                funFact = "Balconul Operei este locul istoric unde zeci de mii de timișoreni au strigat pentru prima oară 'Azi în Timișoara, mâine-n toată țara!'.",
                mustSee = "Sala mare decorată în stil neoromânesc și panorama de neuitat asupra Pieței Victoriei din fața intrării principale."
            )
        ),
        Poi(
            id = "palatul-lloyd",
            name = "Palatul Lloyd (Rectoratul UPT)",
            nameEn = "Lloyd Palace",
            address = "Piața Victoriei nr. 2",
            description = "Prima clădire construită în Piața Victoriei după defortificare. Aici a funcționat Bursa Agricolă și cafeneaua Lloyd. Realizată de Lipót Baumhorn în stil Secession eclectic.",
            descriptionEn = "The first building constructed in Victory Square. Formerly the Agricultural Exchange, built in eclectic Secession style by Lipót Baumhorn.",
            imagePath = "palatul-lloyd.jpg",
            lat = 45.75355,
            lng = 21.22523,
            visitMinutes = 10,
            source = "city",
            enabled = true,
            aiSummary = AiText(
                history = "Construit în stil Secession la începutul secolului XX, a găzduit elita comercială a Banatului și Bursa Agricolă înainte de a deveni sediul Rectoratului Politehnicii.",
                funFact = "La subsolul și parterul clădirii a funcționat legendara cafenea Lloyd, locul preferat de întâlnire al scriitorilor și negustorilor timișoreni.",
                mustSee = "Detaliile decorative ale fațadei și ornamentele heraldice reprezentând comerțul și industria bănățeană."
            )
        ),
        Poi(
            id = "castelul-huniade",
            name = "Castelul Huniade (Muzeul Banatului)",
            nameEn = "Huniade Castle",
            address = "Piața Iancu Huniade nr. 1",
            description = "Cea mai veche clădire din Timișoara, ridicată între 1307 și 1315 de Carol Robert de Anjou și refăcută de Iancu de Hunedoara în secolul al XV-lea.",
            descriptionEn = "The oldest building in Timișoara, originally built 1307-1315 by Charles Robert of Anjou and restored by John Hunyadi in the 15th century.",
            imagePath = "castelul-huniade.jpg",
            lat = 45.75304,
            lng = 21.22708,
            visitMinutes = 15,
            source = "city",
            enabled = true,
            aiSummary = AiText(
                history = "Reprezintă cel mai vechi monument păstrat din oraș, fiind inițial reședință regală medievală a regelui Ungariei Carol Robert de Anjou.",
                funFact = "În curtea interioară încă se regăsesc fragmente din zidurile palatului din secolul al XIV-lea descoperite la săpături arheologice.",
                mustSee = "Turnul masiv din cărămidă și tunurile istorice expuse în fața castelului."
            )
        ),
        Poi(
            id = "palatul-baroc",
            name = "Palatul Baroc (Muzeul Național de Artă)",
            nameEn = "Baroque Palace",
            address = "Piața Unirii nr. 1",
            description = "Construit de-a lungul anilor 1700, a fost sediul guvernatorilor imperiali ai Banatului. Astăzi adăpostește colecții prestigioase de artă europeană și opera pictorului Corneliu Baba.",
            descriptionEn = "Built in the 1700s as the seat of the Banat governors, today housing the National Museum of Art with prestigious collections.",
            imagePath = "palatul-baroc.jpg",
            lat = 45.75725,
            lng = 21.2294,
            visitMinutes = 20,
            source = "city",
            enabled = true,
            aiSummary = AiText(
                history = "Reședință a guvernatorilor austrieci în secolul al XVIII-lea, palatul a găzduit împărați precum Iosif al II-lea și Franz Joseph I în vizitele lor imperiale la Timișoara.",
                funFact = "Aspectul fațadei evocă palatele vieneze ale epocii, cu motive rococo unice în România.",
                mustSee = "Sala Barocă cu candelabre somptuoase și colecția dedicată maestrului Corneliu Baba."
            )
        ),
        Poi(
            id = "domul-sf-gheorghe",
            name = "Domul Romano-Catolic Sfântul Gheorghe",
            nameEn = "St. George Roman Catholic Dome",
            address = "Piața Unirii nr. 12",
            description = "Cea mai importantă clădire barocă din Timișoara, proiectată de Joseph Emanuel Fischer von Erlach, arhitectul curții imperiale vieneze.",
            descriptionEn = "The most significant Baroque cathedral in Timișoara, designed by imperial Viennese architect Fischer von Erlach.",
            imagePath = "domul-sf-gheorghe.jpg",
            lat = 45.75813,
            lng = 21.2304,
            visitMinutes = 15,
            source = "city",
            enabled = true,
            aiSummary = AiText(
                history = "Piatra de temelie a fost pusă în 1736. Construcția a durat aproape patru decenii, devenind inima spirituală a comunităților catolice germane, maghiare și românești din Banat.",
                funFact = "Turlele au o înălțime redusă deliberat pentru a nu servi drept țintă artileriei în caz de asediu asupra cetății.",
                mustSee = "Altarul principal pictat de Michael Angelo Unterberger, directorul Academiei de Arte Frumoase din Viena."
            )
        ),
        Poi(
            id = "bastionul-maria-tereza",
            name = "Bastionul Maria Theresia",
            nameEn = "Maria Theresa Bastion",
            address = "Strada Hector / Calea Alexandru Ioan Cuza",
            description = "Cea mai mare bucată păstrată din vechea cetate de tip Vauban a Timișoarei din secolul al XVIII-lea. Astăzi un spațiu pietonal activ cu galerii, cafenele și muzee.",
            descriptionEn = "The largest preserved remnant of Timișoara's 18th-century Vauban fortress, now a vibrant pedestrian cultural quarter.",
            imagePath = "bastionul-maria-tereza.jpg",
            lat = 45.75693,
            lng = 21.23367,
            visitMinutes = 15,
            source = "team",
            enabled = true,
            aiSummary = AiText(
                history = "Parte a sistemului defensiv stelar construit de administrația habsburgică între 1730 și 1761, a rezistat campaniilor de demolare a zidurilor de la sfârșitul anilor 1800.",
                funFact = "Cărămizile groase de peste 1 metru au adăpostit în trecut depozite de grâne și muniție ferite de bombardamente.",
                mustSee = "Plimbarea pe galeriile exterioare suspendate și curtea interioară pavată."
            )
        ),
        Poi(
            id = "sinagoga-din-cetate",
            name = "Sinagoga din Cetate",
            nameEn = "Cetate Synagogue",
            address = "Strada Mărășești nr. 6",
            description = "Construită în stil maur eclectic în 1865 după planurile arhitectului vienez Carl Schumann, recent restaurată splendid.",
            descriptionEn = "Moorish eclectic synagogue built in 1865 by Viennese architect Carl Schumann, recently magnificently restored.",
            imagePath = "sinagoga-din-cetate.jpg",
            lat = 45.75646,
            lng = 21.22613,
            visitMinutes = 10,
            source = "city",
            enabled = true,
            aiSummary = AiText(
                history = "Inaugurată în 1865 de sărbătoarea Roș Hașana, este o mărturie vie a bogatei comunități evreiești care a contribuit decisiv la modernizarea Timișoarei.",
                funFact = "Orga sa istorică a fost realizată de celebrul meșter timișorean Carl Leopold Wegenstein.",
                mustSee = "Cupolele maure albastre și vitraliile restaurate cu motive geometrice orientale."
            )
        )
    )

    private val _poisState = MutableStateFlow(initialPois)
    val poisState: StateFlow<List<Poi>> = _poisState.asStateFlow()

    fun togglePoiEnabled(poiId: String) {
        _poisState.value = _poisState.value.map {
            if (it.id == poiId) it.copy(enabled = !it.enabled) else it
        }
    }

    fun updateVisitMinutes(poiId: String, newMinutes: Int) {
        _poisState.value = _poisState.value.map {
            if (it.id == poiId) it.copy(visitMinutes = newMinutes.coerceIn(5, 60)) else it
        }
    }

    fun addCustomPoi(name: String, address: String, minutes: Int) {
        val newPoi = Poi(
            id = "user-${System.currentTimeMillis()}",
            name = name,
            nameEn = name,
            address = address.ifBlank { "Timișoara" },
            description = "Obiectiv turistic adăugat personal în itinerar.",
            descriptionEn = "Custom tourist spot added to the itinerary.",
            lat = 45.7550 + (Math.random() - 0.5) * 0.01,
            lng = 21.2280 + (Math.random() - 0.5) * 0.01,
            visitMinutes = minutes.coerceIn(5, 60),
            source = "user",
            enabled = true,
            aiSummary = AiText(
                history = "Locație adăugată de utilizator pe traseul verde.",
                funFact = "Personalizarea traseului îți oferă libertate deplină de explorare pe propriul orar.",
                mustSee = "Bucură-te de atmosfera cartierului și de ritmul prietenos al orașului."
            )
        )
        _poisState.value = _poisState.value + newPoi
    }

    fun calculateGreenRoute(
        startName: String,
        destinationName: String,
        budgetMinutes: Int,
        travelMode: TravelMode,
        interests: Set<Interest>,
        language: Language
    ): RouteResult {
        val activePois = _poisState.value.filter { it.enabled }
        val bufferMinutes = 15
        var remainingBudget = budgetMinutes - bufferMinutes

        val selectedStops = mutableListOf<Poi>()
        var accumulatedTime = 0
        var totalDistanceKm = 0.0

        // Greedy selection based on time budget and visit minutes
        for (poi in activePois) {
            val estimatedTravelMinutes = when (travelMode) {
                TravelMode.WALKING -> 8
                TravelMode.CYCLING -> 4
                TravelMode.TRANSIT_MIX -> 5
            }
            val neededTime = poi.visitMinutes + estimatedTravelMinutes
            if (remainingBudget >= neededTime) {
                selectedStops.add(poi)
                remainingBudget -= neededTime
                accumulatedTime += neededTime
                totalDistanceKm += when (travelMode) {
                    TravelMode.WALKING -> 0.7
                    TravelMode.CYCLING -> 0.9
                    TravelMode.TRANSIT_MIX -> 1.1
                }
            }
            if (selectedStops.size >= 5) break
        }

        if (selectedStops.isEmpty() && activePois.isNotEmpty()) {
            selectedStops.add(activePois.first())
            accumulatedTime += activePois.first().visitMinutes + 10
            totalDistanceKm += 1.0
        }

        val totalDurationMinutes = accumulatedTime + bufferMinutes
        val bikeLaneKm = (totalDistanceKm * 0.65).coerceAtMost(totalDistanceKm)

        // CO2 savings vs 120g/km car
        val carEmissionGrams = totalDistanceKm * 120.0
        val actualEmissionGrams = totalDistanceKm * travelMode.co2GramsPerKm
        val co2SavedGrams = (carEmissionGrams - actualEmissionGrams).coerceAtLeast(0.0)

        // Green Score = (CO2 saved / 10) + 2 * bikeLaneKm
        val score = (co2SavedGrams / 10.0 + (bikeLaneKm * 2.0)).roundToInt()

        val transitCost = if (travelMode == TravelMode.TRANSIT_MIX) 5.0 else 0.0

        val geminiStory = if (language == Language.RO) {
            "Plimbarea ta verde prin Timișoara a acoperit ${String.format("%.1f", totalDistanceKm)} km pe trasee prietenoase cu mediul. " +
            "Ai explorat ${selectedStops.size} puncte de reper remarcabile, economisind ${String.format("%.1f", co2SavedGrams / 1000.0)} kg de emisii de CO₂. " +
            "De la splendoarea Catedralei până la armonia barocă din Piața Unirii, ai experimentat sufletul verde și deschis al orașului!"
        } else {
            "Your green journey across Timișoara covered ${String.format("%.1f", totalDistanceKm)} km of eco-friendly pathways. " +
            "You discovered ${selectedStops.size} historic landmarks, preventing ${String.format("%.1f", co2SavedGrams / 1000.0)} kg of CO₂ emissions. " +
            "From the iconic Cathedral towers to Baroque splendor, you experienced the authentic green spirit of Timișoara!"
        }

        return RouteResult(
            startName = startName,
            destinationName = destinationName,
            timeBudgetMinutes = budgetMinutes,
            estimatedDurationMinutes = totalDurationMinutes,
            stops = selectedStops,
            totalDistanceKm = totalDistanceKm,
            bikeLaneKm = bikeLaneKm,
            co2SavedGrams = co2SavedGrams,
            greenScore = score,
            transitCostRon = transitCost,
            travelMode = travelMode,
            geminiStoryRecap = geminiStory
        )
    }
}
