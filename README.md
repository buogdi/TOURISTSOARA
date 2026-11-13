# TOURISTȘOARA 🌿
> **Green Mobility Tourist Route Planner for Timișoara (UniHack 8)**

*„Ai 3 ore până la tren? Spune-ne cât timp ai, iar noi te plimbăm prin Timișoara pe cel mai verde traseu și te ducem la timp la destinație.”*

---

## 📌 Despre Proiect
Aplicație Android nativă (Kotlin + Jetpack Compose) dezvoltată pentru **UniHack 8** (Track: **Green Mobility** și Challenge-ul MLH: **Best use of Gemini**). Aplicația utilizează date oficiale ale Primăriei Timișoara, rețeaua GTFS SMTT/STPT, hărțile OpenStreetMap pentru piste de biciclete și Google Gemini pentru carduri turistice grounded și generative recaps.

Planul detaliat de arhitectură și specificații: **[TOURISTSOARA_PLAN.md](TOURISTSOARA_PLAN.md)**.

## 🚀 Funcționalități Cheie
- **Planificare pe Buget de Timp (F2/F3):** Algoritm inteligent cu buffer de 15 minute pentru sosire la timp la destinație (ex: Gara de Nord).
- **Date Reale ale Orașului:**
  - 20 de Obiective turistice oficiale din ghidul Primăriei Timișoara (`cont.primariatm.ro/api/mobile/pois`).
  - Rețeaua completă de transport public SMTT (58 linii, 890 stații, trasee polylines).
  - Piste de biciclete, parcări velo și stații i'velo (OpenStreetMap / Overpass).
- **Grounded Gemini AI (MLH Challenge):**
  - Carduri informative la sosire generate exclusiv din descrierile oficiale (fără halucinații).
  - Schema JSON structurată (`history`, `funFact`, `mustSee`).
  - Personalizare după interese (istorie, arhitectură, familie, gastronomie).
  - Rezumat narativ final al turului.
- **Green Score & CO₂ Economisit (F5):**
  - Calcul transparent al emisiilor evitate față de deplasarea cu autoturismul personal (120 g CO₂/km).
  - Punctaj verde bazat pe distanța parcursă pe jos, cu bicicleta sau transport public.
- **Rezistență Offline & Demo:**
  - Snapshot-uri locale integrate în `app/src/main/assets/`.
  - Mod „Simulează locația” pentru testare și prezentare fără GPS activ.

## 🛠️ Setup și Configurare
1. Deschide proiectul în **Android Studio**.
2. Adaugă cheile API în `local.properties`:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   OPENROUTESERVICE_API_KEY=your_ors_key_here
   ```
3. Sincronizează proiectul cu Gradle și rulează aplicația pe un emulator sau dispozitiv fizic Android (API 24+).

---
© 2026 Echipa TOURISTȘOARA · UniHack 8 · Date: Primăria Municipiului Timișoara & OpenStreetMap contributors.
