# TOURISTȘOARA: plan de proiect (UniHack 8 · Green Mobility)

> **Pitch:** „Ai 3 ore până la tren? Spune-ne cât timp ai, iar noi te plimbăm prin Timișoara pe cel mai verde traseu și te ducem la timp la destinație.”

**Echipă:** 4 dezvoltatori începători · **Timp:** 2 zile · **Platformă:** Android (Android Studio, Kotlin)
**Versiune plan:** v3. Integrat cu *Timișoara City Data and Services Guide* (https://ghid-pmt.vercel.app/unihack, date din 13 nov 2026) și cu **Gemini** pentru cardurile de informații (challenge MLH „Best use of Gemini”).

---

## 1. Cum răspundem la brief

Brief: *„use real city data on bike lanes and transit to design cleaner, seamless urban transport in Timișoara”*.

| Cerința juriului | Ce arătăm în demo | Sursă reală |
|---|---|---|
| Date reale despre piste de biciclete | Strat cu pistele pe hartă + parcări de biciclete + stații i'velo | OpenStreetMap (Overpass) |
| Date reale despre transport public | Linii și stații pe hartă, orar real, mod „mix” cu tramvai/autobuz | Endpoint-urile Primăriei bazate pe GTFS SMTT |
| Transport curat | Doar mers pe jos, bicicletă, transport public | n/a |
| Impact măsurabil | **Green Score + CO₂ economisit** față de mașină | Factori de emisie cu sursă citată |
| Experiență fluidă | Timp liber → traseu → informații la fiecare oprire → sosire la timp | n/a |
| Adoptabil de oraș | Obiective oficiale din ghidul turistic al Primăriei, linkuri spre Sesizări, cod open source | Vezi secțiunea 12 |
| Challenge MLH „Best use of Gemini” | Gemini în mai multe puncte reale: carduri grounded, personalizare, multimodal, recap | Vezi secțiunea 11 |

## 2. Poziționare față de ce există deja

Ghidul spune clar: *o aplicație care copiază ce există e greu de adoptat; una care completează un gol e ușor de adoptat.*
- Timișoara CityApp, Tranzy și Google Maps au deja **ore de sosire live** și planificator de călătorie. **Noi nu facem „încă o aplicație de sosiri”.**
- **Golul pe care îl umplem:** niciuna nu răspunde la „am X ore libere, ce văd și cum ajung la timp la tren, cât mai verde?”. Planificarea pe buget de timp + turism + CO₂ economisit.
- Folosim aceleași date ca CityApp (endpoint-urile Primăriei), deci integrarea ulterioară e naturală.

---

## 3. Surse de date și unde intră în aplicație

| Prioritate | Sursă | Ce luăm | Unde apare în aplicație |
|---|---|---|---|
| **P0** | `GET https://cont.primariatm.ro/api/mobile/pois` | 20 de obiective din ghidul turistic al orașului: nume RO/EN, adresă, descriere RO/EN, imagine, coordonate | **Lista de bază de obiective** (înlocuiește lista tastată de noi), cardul de la sosire |
| **P0** | OpenRouteService (cheie gratuită) | Rută pe jos / bicicletă între puncte | Traseul desenat + durata |
| **P0** | **Gemini API** (cheie din Google AI Studio) | Carduri la sosire, personalizare, recap, „ce văd aici?” | Cardul de obiectiv, rezumat final (secțiunea 11) |
| **P0+** | OpenStreetMap, Overpass | Piste de biciclete (~150 de segmente), parcări bicicletă (~80), stații i'velo (~49) | Strat pe hartă, kilometri pe pistă în Green Score |
| **P0+** | `GET /api/mobile/transit/network` (cont.primariatm.ro) | 58 linii, ~890 stații, 116 traseele (polyline) | Strat cu linii și stații pe hartă |
| **P1** | `GET /api/mobile/transit/lines/{routeId}` și `/stops/{stopId}/timetable` | Stațiile unei linii în ordine, orarul programat | Modul „mix cu transport public” |
| **P1** | `GET /api/mobile/wallet/types?category=transport` | Prețuri bilete (ex. 60 min: 5 lei, zi: 18 lei) | Cost estimat în rezumat |
| **P1** | `GET /api/mobile/events` | Evenimente în oraș (data în secunde unix, `venueLat/Lng` pot fi null) | „Azi, lângă acest loc” în cardul de obiectiv |
| **P1** | sesizari.primariatm.ro | Doar **link** către site | Buton „Ai văzut o problemă? Raportează” |
| **Bonus** | `GET /api/zones` | 40 de cartiere (GeoJSON) | „Ești în cartierul X” la sosire |
| **Bonus** | `GET /api/sesizari?...` | Rapoarte publice lângă traseu | Avertisment „probleme raportate pe traseu” |
| **Nu folosim** | Parcare (Timpark), trafic (doar un eșantion de la o cameră), nomenclator străzi, catalog servicii | n/a | n/a (nu sunt relevante pentru mobilitatea verde sau cer geocodare complexă) |

### Reguli de folosire (din ghid)
- **Endpoint-urile sunt pentru rezidenți în timp real.** Nu le apelăm la fiecare ecran: **descărcăm o dată, salvăm în cache pe telefon** și refolosim. Datele se schimbă rar.
- Setăm un `User-Agent` cu numele echipei (ex. `TOURISTSOARA-UniHack8`).
- Fără garanție de uptime, forma răspunsurilor se poate schimba după hackathon → **păstrăm un snapshot în `assets/`** și folosim snapshotul dacă apelul pică.
- Folosim doar hosturile `*.primariatm.ro` din ghid.
- **Nu copiem conținut de pe visit-timisoara.com** (interzis fără acord scris) și **nu stocăm conținut Google Places**.
- OpenStreetMap: afișăm creditul **„© OpenStreetMap contributors”** în aplicație (licență ODbL).
- Orarul GTFS se schimbă la câteva săptămâni (v4.31 valabil 5 nov–31 dec 2026): **reluăm descărcarea cu o zi înainte de demo**.
- Prima acțiune a Dev4: rulează fiecare endpoint cu `curl` și **verifică forma reală a răspunsului** înainte să scrie cod pe ea. Structurile din acest plan vin din ghid.

### Capcane care ne pot costa ore (din ghid)
| Capcană | Ce facem |
|---|---|
| Ordinea coordonatelor diferă: GeoJSON și `coordinates` = `[lng, lat]`; POI-urile au `latitude`/`longitude`; stațiile au `lat`/`lon`; fișierele NGSI-LD de trafic dau `[lat, lng]` | Testăm un punct cunoscut (Piața Victoriei) pe hartă la fiecare sursă nouă |
| `polyline` din rețeaua de transport e **Google encoded polyline, precizie 5** | Folosim un decodor de ~20 de linii (vezi 6.3) |
| În OSM, lipsa unei piste pe hartă ≠ lipsa pistei în realitate | În UI scriem „nu avem date”, nu „nu există pistă” |
| Toate cursele au `bikes_allowed = 2` („nu”) și nu există câmpuri de accesibilitate | **Nu combinăm bicicleta cu tramvaiul** și nu promitem rute accesibile |
| Deviațiile și liniile suspendate sunt publicate doar ca text, nu ca date | Afișăm „Orar programat, verifică anunțurile STPT/SMTT” |
| Geocoderele scriu străzile altfel decât registrul oficial | **Nu cerem adrese tastate.** Destinația = obiectiv, stație sau punct ales pe hartă |
| Multe seturi de date sunt anuale și vechi | Afișăm anul/data lângă fiecare cifră |

---

## 4. Funcționalități

### P0: fluxul de bază (fără el nu avem demo)
**F1. Ecran de input**
- Plecare: locația curentă sau punct ales pe hartă
- Destinație: obiectiv din listă, **stație de transport (căutare în cele ~890 de stații din `network`, ex. „Gara de Nord”)** sau punct ales pe hartă
- Timp disponibil
- Mod: pe jos / bicicletă (mix cu transport = P1)

**F2. Generator de traseu cu buffer de 15 min**, vezi secțiunea 8.

**F3. Hartă:** traseu, obiective, poziția utilizatorului, bară „Timp rămas · Ești pe grafic ✅ / Întârzii ⚠️”.

**F4. Detectare sosire (≤ 50 m) + card AI**
- Conținutul este generat de **Gemini**, bazat pe **descrierea oficială** din endpoint (promptul și arhitectura din secțiunea 11)
- Cardul spune sursa: „Sursa: Ghidul turistic al Primăriei Timișoara · rezumat generat cu Gemini”

**F5. Green Score + CO₂ economisit** (secțiunea 7)

**F6. Lista de obiective editabilă** (secțiunea 5)

**F7. Buton „Simulează locația”** pentru demo fără GPS

### P0+: datele reale cerute de brief (obligatoriu pentru jurizare)
**F8. Strat „Piste de biciclete”:** polilinii verzi din `bike_lanes.geojson` (OSM), plus parcări de biciclete și stații i'velo ca markere
**F9. Strat „Transport public”:** liniile (din `paths`, decodate) și stațiile din `transit/network`, colorate cu `color` din răspuns (tramvai/troleibuz/autobuz după `type`: 0, 11, 3)
- Comutatoare pe hartă pentru a porni/opri straturile
- Eticheta „Surse: SMTT feed v4.31-4 · OSM, date din 13 nov 2026” (versiunea vine din `feedVersion`)

### P1: în ordinea priorității
1. **Gemini G2 + G5**: personalizare după interese (chips pe ecranul de input) și „povestea plimbării tale” la final (secțiunea 11). Costă un parametru în prompt și un apel, dar se văd mult în demo.
2. **Limbi RO/EN**: aproape gratis, API-ul dă deja `name`/`nameEn` și `description`/`descriptionEn`. Româna e implicită.
3. **Buton „Raportează o problemă”** care deschide `sesizari.primariatm.ro` în browser (nu construim propria căsuță de sesizări)
4. **Gemini G3 „Spune-mi mai mult”** (mic chat grounded) și **G4 „Ce văd aici?”** (poză, multimodal)
5. **Mod „mix cu transport public”** (secțiunea 6)
6. **Cost bilet** în rezumat (din `wallet/types`)
7. **„Azi lângă acest loc”**: evenimente din `events`
8. Re-calculare live (sari peste un obiectiv dacă întârzii)
9. Text-to-speech (Android `TextToSpeech`, citește cardul)

### Bonus
Cartierul curent (`/api/zones`), probleme raportate pe traseu (`/api/sesizari`, **doar categorie + status**, fără descrieri sau poze).

---

## 5. Lista de obiective editabilă (F6)

Trei straturi, combinate la pornire:

| Strat | Fișier / loc | Cine îl modifică | Conținut |
|---|---|---|---|
| 1. **Oraș** | `GET /api/mobile/pois` (cache + snapshot `assets/pois_city.json`) | Primăria (nu îl edităm) | 20 de obiective oficiale |
| 2. **Echipă** | `assets/pois_overrides.json` | Oricine din echipă, fără cod Kotlin | `visitMinutes` per obiectiv (API-ul nu are durata vizitei), obiective extra, obiective dezactivate implicit |
| 3. **Utilizator** | SharedPreferences (JSON) | Utilizatorul, din aplicație | Locuri adăugate, dezactivări |

**Lista finală = strat 1 + extra din strat 2 + locuri din strat 3, minus cele dezactivate.**

Exemplu `pois_overrides.json`:
```json
{
  "visitMinutes": { "catedrala-mitropolitana": 20, "default": 10 },
  "disabled": [],
  "extra": [
    { "id": "bastion_theresia", "name": "Bastionul Theresia", "lat": 0.0, "lng": 0.0,
      "visitMinutes": 20, "description": "" }
  ]
}
```
(Coordonatele se completează după verificare pe hartă. Nu le inventăm.)

**În aplicație:**
1. Ecran „Obiective”: listă cu comutator activ/inactiv pe fiecare
2. „Adaugă loc”: nume, durata vizitei, poziție prin long-press pe hartă
3. Ștergere doar pentru locurile adăugate de utilizator (cele din oraș/echipă se dezactivează)
4. Pentru un loc fără descriere oficială, cardul spune explicit „Nu avem o descriere verificată pentru acest loc” (și, opțional, AI generează un text marcat ca **negrefat pe o sursă**)

### Model de date
```kotlin
data class Poi(
    val id: String,
    val name: String, val nameEn: String? = null,
    val description: String? = null, val descriptionEn: String? = null,
    val address: String? = null,
    val imagePath: String? = null,          // relativ la https://cont.primariatm.ro
    val lat: Double, val lng: Double,       // din latitude/longitude
    val visitMinutes: Int = 10,
    val source: String = "city",            // city | team | user
    var enabled: Boolean = true,
    var aiSummary: AiText? = null
)
data class AiText(val history: String, val funFact: String, val mustSee: String)
```

---

## 6. Transport public și biciclete în aplicație

### 6.1 Ce descărcăm o dată (și salvăm local)
- `transit/network` (~160 KB): `routes`, `stops`, `paths`, `feedVersion`
- Overpass: `bike_lanes.geojson`, `bike_parking.geojson`, `bike_sharing.geojson` (export o singură dată, în `assets/`)

### 6.2 Interogări Overpass (punct de plecare)
Se rulează în overpass-turbo → *Export → GeoJSON*. Verificați numărul rezultatelor față de ghid (aprox. 150 piste, 80 parcări, 49 stații) și ajustați dacă ies prea multe/puține (ex. dacă zona prinde și localități vecine).
```
[out:json][timeout:60];
area["name"="Timișoara"]["boundary"="administrative"]->.a;
(
  way["highway"="cycleway"](area.a);
  way["cycleway"~"lane|track"](area.a);
  way["cycleway:left"~"lane|track"](area.a);
  way["cycleway:right"~"lane|track"](area.a);
);
out geom;
```
Parcări: `node["amenity"="bicycle_parking"](area.a);` · Stații i'velo: `node["amenity"="bicycle_rental"](area.a);`

### 6.3 Decodor polyline (precizie 5), ~20 de linii Kotlin
```kotlin
fun decodePolyline(encoded: String): List<Pair<Double, Double>> {
    val out = mutableListOf<Pair<Double, Double>>()
    var index = 0; var lat = 0; var lng = 0
    while (index < encoded.length) {
        var shift = 0; var result = 0; var b: Int
        do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
        lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
        shift = 0; result = 0
        do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
        lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
        out.add(Pair(lat / 1e5, lng / 1e5))   // (lat, lng)
    }
    return out
}
```

### 6.4 Mod „mix cu transport public” (P1, simplu, doar curse directe)
Pentru un segment mai lung de ~1,5 km:
1. Găsește stația A cea mai apropiată de punctul de plecare al segmentului și stația B cea mai apropiată de sosire (în 500 m), cu **cel puțin o linie comună** (`routeIds` din `stops`)
2. Din `lines/{routeId}` verifică direcția în care A apare înaintea lui B
3. Timp = mers până la A + așteptare (următoarea oră din `timetable` a lui A) + călătorie (diferența dintre orele din A și B pentru aceeași direcție, sau estimare ~2 min/stație) + mers de la B
4. Dacă nu există linie directă → **mergem pe jos** și nu inventăm legături cu transfer
5. Afișăm „Orar programat, fără date live; verifică anunțurile de deviații”

**Bicicleta nu se combină cu transportul public** (feedul spune că bicicletele nu sunt permise). Pentru modul bicicletă, arătăm cea mai apropiată stație i'velo, cu textul „disponibilitate necunoscută” (nu avem date live).

---

## 7. Green Score și CO₂ economisit

```
co2_economisit_g = km_traseu × factor_mașină − Σ(km_segment × factor_mod_segment)
Green Score      = round(co2_economisit_g / 10) + 2 × km_pe_pistă_de_biciclete
```
| Mod | g CO₂ / pasager-km (orientativ) |
|---|---|
| Mașină (medie) | ~120 |
| Pe jos / bicicletă | 0 |
| Tramvai / troleibuz / autobuz | ~30–70 (alegem o valoare) |

- **Cineva din echipă găsește o sursă** (ex. EEA sau DEFRA) și o scrie în aplicație, în „Despre calcul”, plus pe slide. Ghidul cere să arătăm de unde vine fiecare cifră.
- `km_pe_pistă` = lungimea porțiunilor din traseu aflate la < 15 m de o linie din `bike_lanes.geojson` (aproximare, o menționăm)
- Ecran rezumat: km, CO₂ economisit, Green Score, **cost bilete** (dacă a folosit transport), buton „Raportează o problemă”
- **Cifra de urmărit pentru oraș:** „kg CO₂ economisiți prin trasee turistice verzi” (ghidul cere să numim o cifră care arată că funcționează)

---

## 8. Algoritmul de traseu (greedy)

```
Intrare: start, destinatie, timpDisponibil, mod, listaActiva
buffer = 15 min
timpRamas = timpDisponibil − buffer
pozitie = start; selectate = []

repetă:
    valizi = [c din listaActiva, neselectat, unde
              t(pozitie→c) + c.visitMinutes + t(c→destinatie) ≤ timpRamas]
    dacă valizi e gol: STOP
    c = validul cu t(pozitie→c) minim
    selectate += c
    timpRamas −= t(pozitie→c) + c.visitMinutes
    pozitie = c
```
- `t()` la alegere = **distanță în linie dreaptă (Haversine) × 1.3 / viteză** (pe jos 5 km/h, bicicletă 15 km/h). Fără apeluri API.
- După alegere: **un singur apel OpenRouteService** cu toate punctele. Dacă durata reală depășește bugetul, scoatem ultimul obiectiv și repetăm.
- Fallback fără internet: afișăm traseul drept cu linii drepte și durata estimată.

---

## 9. Arhitectură

```
app/src/main/
├── assets/
│   ├── pois_city.json            # snapshot /api/mobile/pois
│   ├── pois_overrides.json       # editabil de echipă
│   ├── transit_network.json      # snapshot /transit/network
│   ├── bike_lanes.geojson        # OSM
│   ├── bike_parking.geojson      # OSM
│   └── bike_sharing.geojson      # OSM
├── java/.../touristsoara/
│   ├── ui/        InputActivity, MapActivity, PoiCardDialog, PoiListActivity, SummaryActivity
│   ├── data/      Poi, PoiRepository (3 straturi), TransitRepository, CityApi (Retrofit), GeminiRepository
│   ├── logic/     RoutePlanner, TransitPlanner, GreenScore, GeofenceChecker, Polyline
│   └── network/   RoutingApi (OpenRouteService)
```
Retrofit: baza `https://cont.primariatm.ro/`, interceptor care adaugă `User-Agent: TOURISTSOARA-UniHack8`. Cache: fișier JSON în `filesDir` + data descărcării, cu snapshot din `assets/` ca fallback.

---

## 10. Împărțirea pe oameni

| Dev | Rol | Răspunde de |
|---|---|---|
| **Dev 1** | UI | Input, Obiective (listă + adăugare), Summary, card obiectiv, RO/EN |
| **Dev 2** | Hartă | osmdroid, markere, traseu, locația utilizatorului, **Simulează locația**, straturi bike/transit, comutatoare, credit OSM |
| **Dev 3** | Logică | RoutePlanner, GeofenceChecker, GreenScore, „ești pe grafic”, TransitPlanner (P1) |
| **Dev 4** | Date + AI | `curl` pe endpoint-uri, CityApi + cache + snapshot-uri, export Overpass, RoutingApi, GeminiRepository, prompturi |

**Contract (primele 30 min):** `Poi`, `AiText`, `RouteResult` (puncte, durată, distanță, segmente cu mod), `TransitNetwork`. Până sunt gata modulele reale, fiecare folosește date fake.

---

## 11. Gemini: cardurile de informații (challenge MLH „Best use of Gemini”)

> Pagina MLH despre Gemini (https://www.mlh.com/partners/gemini) oferă resurse și credite, **dar nu listează criteriile de jurizare ale challenge-ului**. Întrebați organizatorii sau citiți pagina challenge-ului din Devpost/Discord și ajustați slide-ul „Cum folosim Gemini” după ele. Planul pornește de la ideea că vrem **mai multe utilizări reale, nu un singur apel decorativ**.

### 11.1 Unde folosim Gemini

| # | Funcție | Ce face Gemini | Prioritate |
|---|---|---|---|
| **G1** | **Card la sosire** | Rezumat structurat (JSON) **bazat pe descrierea oficială** din ghidul turistic: istorie, curiozitate, ce merită văzut | **P0** |
| **G2** | **Personalizare** | Adaptează tonul și accentul după interese (istorie, arhitectură, familie, mâncare) alese cu chips pe ecranul de input | P1, ușor (un parametru în prompt) |
| **G5** | **Povestea plimbării** | La final, 3–4 propoziții despre turul făcut (obiective vizitate, km, Green Score), de partajat | P1, ușor, efect bun în demo |
| **G3** | **„Spune-mi mai mult”** | Răspunde la întrebări de follow-up **doar din sursa oficială** și din cardul curent; spune „nu știu” dacă sursa nu conține răspunsul | P1 |
| **G4** | **„Ce văd aici?”** | Multimodal: poza utilizatorului + numele locului → explicație, cu aceleași reguli de grounding | P1 / bonus |
| G6 | Limbi | Carduri în EN (din `descriptionEn`) sau alte limbi pentru turiști | P1 |

Ordinea de lucru: **G1 → G5 → G2 → G3 → G4**.

### 11.2 Apelul, fără SDK (REST, simplu pentru începători)
Un singur `POST` cu OkHttp/Retrofit, cheia în header. Verificați forma exactă și **numele modelului** în documentație (https://mlh.link/gemini-docs, plus Quickstart și Cookbook de pe pagina MLH). Puneți numele într-o singură constantă `GEMINI_MODEL`. Pentru carduri contează viteza, deci alegeți un model rapid (de tip Flash) și rezervați unul mai capabil pentru G3/G4 dacă e nevoie.

```
POST https://generativelanguage.googleapis.com/v1beta/models/{GEMINI_MODEL}:generateContent
Header: x-goog-api-key: <cheia din local.properties>
Body:
{
  "contents": [{ "parts": [{ "text": "<promptul de mai jos>" }] }],
  "generationConfig": {
    "responseMimeType": "application/json",
    "responseSchema": {
      "type": "OBJECT",
      "properties": {
        "history": { "type": "STRING" },
        "funFact": { "type": "STRING" },
        "mustSee": { "type": "STRING" }
      },
      "required": ["history", "funFact", "mustSee"]
    }
  }
}
Răspuns: candidates[0].content.parts[0].text  → JSON de parsat în AiText
```
Cu schema de ieșire nu parsăm text liber, iar cardul nu se strică dacă modelul adaugă comentarii.

### 11.3 Prompt (grounded)
Ghidul Primăriei avertizează că un asistent trebuie să spună ce nu a putut citi. Promptul primește **doar sursa oficială**:

```
Ești un ghid turistic prietenos în Timișoara. Folosește DOAR textul sursă de mai jos.
Loc: "{nume}"
Text sursă (Ghidul turistic al Primăriei): "{description}"
Interese ale vizitatorului: {interese}   // ex. "istorie, arhitectură" sau "nespecificat"
Limba răspunsului: {limba}
Scrie un card:
- history: 3-4 propoziții
- funFact: o curiozitate din sursă
- mustSee: o propoziție despre ce merită văzut
Dacă sursa nu conține o informație, scrie "Informație indisponibilă în sursă".
Nu adăuga ani, nume sau cifre care nu apar în text. Ton cald, fără exagerări.
```
- Pentru EN trimitem `descriptionEn`
- Pentru locuri **fără descriere oficială** (adăugate de utilizator): cardul spune „Nu avem o descriere verificată”, iar textul generat de Gemini e marcat vizibil ca **neverificat**
- G5 primește lista obiectivelor, km, CO₂ și Green Score (cifre calculate de noi, **nu inventate de model**)
- G3/G4 primesc sursa + cardul curent ca context și aceeași regulă „nu inventa”

### 11.4 Latență și reziliență
- **Pre-generare:** când traseul e confirmat, rulăm în fundal G1 pentru obiectivele selectate. La sosire cardul apare instant.
- **Cache pe disc** cu cheia `poiId + limbă + interese`. Nu apelăm Gemini la fiecare sosire.
- **Timeout ~8 s**, apoi afișăm descrierea oficială brută (fără rezumat)
- **Fără internet:** texte pre-generate în `assets/` pentru cele 20 de obiective în RO (și EN, dacă avem timp), generate acasă în AI Studio înainte de demo
- Limitele de rată nu le știm pentru contul vostru, deci tratați **429** ca „folosește cache/fallback”. Cu pre-generare faceți ~20–40 de apeluri în total.

### 11.5 Cheie, credite, securitate
Din pagina MLH (verificați condițiile pe linkurile lor):
- Plan Google pentru studenți, gratuit 1 an
- **$10 credite gratuite pe lună** prin Google Program Benefits
- **$300 credite** la un cont nou Google Cloud (pentru cei care nu sunt studenți)
- Cheia se creează în **Google AI Studio** (https://mlh.link/gemini-aistudio)

Reguli:
- Cheia în `local.properties` → `BuildConfig`, **niciodată în repo**
- O cheie din APK poate fi extrasă. Pentru hackathon e acceptabil dacă **restricționați cheia** și nu publicați APK-ul. Pentru adoptare reală, în pitch: „apelurile Gemini trec printr-un mic proxy al Primăriei/SMTT, nu direct din aplicație”.
- Ghidul cere „fără cont plătit ținut doar de unul dintre voi”, deci în README scrieți cum se înlocuiește cheia

### 11.6 Date personale
- G1/G2/G5 trimit doar **numele și descrierea locului** și interesele alese. **Nu trimitem locația utilizatorului.**
- G4 (poză): se trimite **doar la apăsarea explicită** a unui buton, cu mesajul „Evită fețe și numere de înmatriculare în poză”. Poza nu se salvează.
- Toate textele generate poartă eticheta „Generat cu Gemini · sursă: Ghidul turistic al Primăriei”

### 11.7 Cum prezentăm pentru challenge
Un slide „Cum folosim Gemini”, cu 4 puncte:
1. **Grounded:** răspunde doar din sursa oficială a orașului și spune „nu știu” (răspunde direct îngrijorării din ghid despre AI care induce în eroare)
2. **Output structurat:** JSON cu schemă, deci cardul nu se strică
3. **Personalizat și multimodal:** interese, „Ce văd aici?” cu poză, „Spune-mi mai mult”
4. **Închide bucla:** povestea plimbării cu Green Score-ul calculat de noi

În demo, 25–30 s: sosire → card → „Spune-mi mai mult” → (dacă merge) poză.

---

## 12. Cum o facem adoptabilă (criteriile din ghid)

| Criteriu | Ce facem |
|---|---|
| Proprietar numit | În pitch: **SMTT/STPT** pentru transport și **Primăria / Turism Timișoara** pentru obiective. Propunem integrarea ca modul în Timișoara CityApp |
| Se leagă de canale existente | Link spre Sesizări; datele vin de la sursă (Primărie, SMTT, OSM), nu din copii private |
| Rulează fără noi | **Licență MIT**, README cu pașii de setup, chei în `local.properties`, nicio plată pe card personal (cheia OpenRouteService e gratuită), fără server |
| Română întâi | UI în română, EN prin comutator (datele sunt deja bilingve) |
| Accesibil (WCAG 2.1 AA) | Contrast bun, text mare, `contentDescription` pe butoane și imagini, nu ne bazăm doar pe culoare |
| Privat implicit | Locația rămâne pe telefon, fără trackere, fără cont; spunem în aplicație ce se stochează |
| Onest cu datele | Sursa și data lângă fiecare cifră; „necunoscut” în loc de ghicit |
| Testat cu oameni | Cerem la 3–5 persoane să încerce aplicația sâmbătă/duminică și notăm ce au spus pentru juriu |
| Cifră de urmărit | kg CO₂ economisiți prin trasee verzi |

---

## 13. Calendar

### Ziua 1: baza (obiectiv: pe telefon se vede harta cu obiectivele oficiale și un traseu)
| Interval | Activitate |
|---|---|
| 0:00–1:00 | Setup proiect + Git, chei (ORS, **Gemini din AI Studio**); Dev4 testează întâi promptul din 11.3 direct în AI Studio, contractul de date; **Dev4 rulează `curl` pe endpoint-uri**; toți rulează aplicația goală pe telefon/emulator |
| 1:00–4:00 | Dev1: ecran input · Dev2: hartă cu markere din snapshot POI · Dev3: RoutePlanner cu date fake · Dev4: CityApi + cache + `PoiRepository` (3 straturi) |
| 4:00–6:00 | Dev4: RoutingApi · Dev3 se leagă la el · Dev2 desenează traseul |
| 6:00–8:00 | **Integrare 1:** input → traseu pe hartă, test pe telefon |

### Ziua 2: impact și prezentare
| Interval | Activitate |
|---|---|
| 0:00–2:00 | Dev3: geofence + „ești pe grafic” · Dev2: simulare locație + strat piste · Dev4: export Overpass + **Gemini G1 (card + cache + pre-generare)** · Dev1: card obiectiv |
| 2:00–4:00 | Dev1: ecran Obiective + Summary · Dev3: GreenScore · Dev2: strat transport (decodor polyline) · Dev4: rețea transport + snapshot |
| 4:00–5:30 | **Integrare 2:** flux complet până la Green Score |
| 5:30–6:30 | P1 rapid (**Gemini G5 + G2**, RO/EN, buton Sesizări), bug-uri, aspect verde |
| 6:30–8:00 | Slide-uri, repetiție, APK de rezervă, video demo |

**Regula:** P0 și P0+ se termină complet înainte de orice P1.

---

## 14. Riscuri

| Risc | Soluție |
|---|---|
| Endpoint-urile Primăriei pică sau schimbă forma | Snapshot-uri în `assets/`, `curl` verificat la început și cu o zi înainte de demo |
| Prea multe apeluri către servere folosite de rezidenți | Descărcare o dată, cache local, nu facem polling |
| Fără internet / GPS la demo | Texte Gemini pre-generate în `assets/`, fallback Haversine, „Simulează locația” |
| Gemini lent, 429 sau cheie expirată | Pre-generare + cache pe disc, timeout 8 s, fallback la descrierea oficială, cheie de rezervă |
| Gemini inventează date | Prompt grounded, schemă JSON, verificare manuală a celor 20 de texte înainte de demo |
| Orarul GTFS s-a schimbat | Re-descărcare cu o zi înainte, afișăm `feedVersion` |
| Prea ambițioși | Doar P0 + P0+ obligatoriu |
| Conflicte Git | Fiecare în fișierele lui, commit mic și des |
| Cifre CO₂ contestate | Sursă citată în aplicație și pe slide |
| Crash în prezentare | APK pe 2 telefoane + video |

---

## 15. Scenariul de demo (3 minute)

1. **Problema + golul** (25 s): CityApp arată când vine tramvaiul, dar nu răspunde la „ce văd în 2 ore și cum ajung la tren”
2. **Input** (20 s): „Am 2h30, plec din Piața Victoriei, merg la Gara de Nord, pe jos”
3. **Traseul** (35 s): obiective oficiale ale orașului, buffer de 15 min, **piste OSM și linii SMTT reale** pe hartă
4. **Obiectiv** (35 s): simulăm sosirea → card cu sursa „Ghidul turistic al Primăriei” + rezumat **Gemini**, apoi „Spune-mi mai mult” sau poza
5. **Lista editabilă** (25 s): dezactivăm un loc, adăugăm unul, regenerăm
6. **Green Score** (25 s): „X kg CO₂ economisiți față de mașină”, cu sursa factorilor, plus „povestea plimbării” scrisă de Gemini
7. **Adoptare** (15 s): MIT, modul pentru CityApp, link Sesizări, cifra de urmărit

---

## 16. Checklist final

- [ ] Fiecare endpoint testat cu `curl`, forma răspunsului confirmată
- [ ] Snapshot-uri în `assets/` (POI, rețea transport, piste, parcări, i'velo) cu data descărcării
- [ ] Aplicația rulează pe telefon fizic
- [ ] Flux complet: input → traseu → card → Green Score
- [ ] Lista editabilă merge pe cele 3 straturi
- [ ] Straturile de piste și transport se văd, cu etichetă de sursă și dată
- [ ] Creditul „© OpenStreetMap contributors” vizibil
- [ ] Texte Gemini pre-generate (RO, apoi EN) citite și corectate față de descrierea oficială
- [ ] Cheie Gemini restricționată, în `local.properties`, nu în repo; `GEMINI_MODEL` confirmat în documentație
- [ ] Criteriile challenge-ului „Best use of Gemini” aflate de la organizatori
- [ ] Slide „Cum folosim Gemini” (11.7)
- [ ] Sursa factorilor CO₂ notată
- [ ] Orarul/rețeaua re-descărcate cu o zi înainte de demo
- [ ] Nicio dată de la visit-timisoara.com sau Google Places
- [ ] Cheile API NU sunt în repo (`local.properties`), licență MIT + README
- [ ] Testat cu 3–5 persoane, notițe pentru juriu
- [ ] APK de rezervă + video demo + slide-uri
