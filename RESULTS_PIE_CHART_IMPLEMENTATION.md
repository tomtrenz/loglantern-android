# ResultsPieChartScreen - Implementace ✅

## 📊 Popis funkcionality

Obrazovka pro zobrazení statistických dat ze Splunku pomocí sloupcového grafu (Bar Chart).

### Podporované SPL dotazy:
```spl
search index=main | stats count by src_ip
search index=main | stats count by host
search index=main | stats sum(bytes) by user
```

## 🛠️ Implementované komponenty

### 1. **ResultsPieChartViewModel**
- ✅ Injektuje `ExecuteSearchUseCase` pro kompletní search workflow
- ✅ Spravuje UI state s `searchQuery`, `chartData`, `isLoading`, `errorMessage`, `statusMessage`
- ✅ `updateSearchQuery(query: String)` - aktualizace search dotazu
- ✅ `executeSearch()` - spuštění vyhledávání s:
  - Vytvoření search jobu
  - Polling dokončení (max 60s)
  - Získání výsledků
  - Parsing stats dat
- ✅ `parseStatsResults()` - parsuje výsledky ve formátu:
  ```json
  [
    {"src_ip": "192.168.1.1", "count": "150"},
    {"src_ip": "10.0.0.5", "count": "89"}
  ]
  ```

### 2. **ResultsPieChartScreen**
- ✅ Input pole pro SPL dotaz (multiline, 3-5 řádků)
- ✅ Tlačítko "Spustit vyhledávání" s loading indikátorem
- ✅ Status/Error karty pro zobrazení průběhu
- ✅ **Sloupcový graf** (Bar Chart) pomocí knihovny **Vico**:
  - Zobrazuje max 15 položek pro čitelnost
  - Vertikální osa: počet (count)
  - Horizontální osa: labels (zkrácené na 10 znaků)
  - Primary color z Material3 theme
- ✅ Karta s detaily dat (top 10 položek):
  - Label + hodnota v řádku
  - Horizontální dělítka mezi položkami
  - "... a X dalších" pokud je víc než 10
- ✅ Placeholder když nejsou data (s nápovědou k použití)

### 3. **Data Flow**
```
User Input (SPL query)
    ↓
executeSearch()
    ↓
ExecuteSearchUseCase
    ↓ (createSearchJob)
Splunk API: POST /services/search/jobs
    ↓ (SID returned)
    ↓ (waitForJobCompletion - polling)
Splunk API: GET /services/search/jobs/{sid} (loop)
    ↓ (isDone = true)
    ↓ (getSearchResults)
Splunk API: GET /services/search/jobs/{sid}/results
    ↓
parseStatsResults()
    ↓
List<ChartDataPoint>
    ↓
BarChart() Composable
    ↓
Vico sloupcový graf
```

## 📦 Přidané závislosti

### `gradle/libs.versions.toml`:
```toml
vico = "2.0.0-alpha.28"

vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
vico-core = { group = "com.patrykandpatrick.vico", name = "core", version.ref = "vico" }
```

### `app/build.gradle.kts`:
```kotlin
// Charts
implementation(libs.vico.compose)
implementation(libs.vico.compose.m3)
implementation(libs.vico.core)
```

## 🎯 Datové struktury

### ChartDataPoint
```kotlin
data class ChartDataPoint(
    val label: String,   // src_ip, host, user, atd.
    val value: Float     // count nebo sum
)
```

### ResultsPieChartUiState
```kotlin
data class ResultsPieChartUiState(
    val searchQuery: String = "search index=main | stats count by src_ip",
    val chartData: List<ChartDataPoint> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)
```

## 🔧 Použití

1. **Spusťte aplikaci** a přihlaste se
2. **Navigujte na Results Pie Chart** z Dashboardu
3. **Zadejte SPL dotaz** se stats:
   - Defaultně: `search index=main | stats count by src_ip`
4. **Klikněte "Spustit vyhledávání"**
5. **Sledujte status zprávy**:
   - "Vytvářím search job..."
   - "Job vytvořen (SID: xxx), čekám na dokončení..."
   - "Job běží..."
   - "Načteno X hodnot"
6. **Výsledek**:
   - Sloupcový graf s daty
   - Tabulka top 10 položek pod grafem

## 📋 Příklady SPL dotazů

```spl
# Počet logů podle zdrojové IP
search index=main | stats count by src_ip

# Počet podle hostitele
search index=main | stats count by host

# Suma bytes podle uživatele
search index=main | stats sum(bytes) by user

# Průměrná doba zpracování podle zdroje
search index=main | stats avg(duration) by source

# Počet chybových logů podle severity
search index=main error=true | stats count by severity
```

## 🎨 UI/UX Vlastnosti

- ✅ Material3 Design System
- ✅ Responzivní layout
- ✅ Scrollovatelný obsah (verticalScroll)
- ✅ Loading indikátor během search jobu
- ✅ Disabled input při načítání
- ✅ Barevné karty pro status (primary) a error (error)
- ✅ Emoji ikony pro lepší UX (📊, 🔍, ℹ️, ❌, 📈, 📋)
- ✅ Placeholder s nápovědou když nejsou data
- ✅ Elevation na kartách (4.dp na grafu)

## 🐛 Error Handling

- ✅ Try-catch kolem executeSearchUseCase
- ✅ onSuccess/onFailure z Result<T>
- ✅ Zobrazení error message v červené kartě
- ✅ Logging do Logcat:
  - `PieChartViewModel` tag
  - Log při spuštění search
  - Log při úspěchu (počet results)
  - Log při parsingu každého data pointu
  - Log při chybě

## 📱 Screenshot Flow

```
┌─────────────────────────────────┐
│  📊 Splunk Statistiky           │
├─────────────────────────────────┤
│ ┌───────────────────────────┐   │
│ │ search index=main |       │   │
│ │ stats count by src_ip     │   │
│ └───────────────────────────┘   │
│                                 │
│ [🔍 Spustit vyhledávání]        │
│                                 │
│ ┌───────────────────────────┐   │
│ │ ℹ️ Načteno 5 hodnot        │   │
│ └───────────────────────────┘   │
│                                 │
│ ┌───────────────────────────┐   │
│ │ Graf statistik            │   │
│ │                           │   │
│ │ ██████████ 150            │   │
│ │ ██████ 89                 │   │
│ │ ███ 45                    │   │
│ │                           │   │
│ └───────────────────────────┘   │
│                                 │
│ ┌───────────────────────────┐   │
│ │ 📋 Detaily (5 položek)    │   │
│ │ 192.168.1.1 ........ 150  │   │
│ │ 10.0.0.5 ............ 89  │   │
│ │ 172.16.0.10 ......... 45  │   │
│ └───────────────────────────┘   │
└─────────────────────────────────┘
```

## ✅ Checklist

- [x] Přidat Vico library do dependencies
- [x] Vytvořit ChartDataPoint data class
- [x] Implementovat ResultsPieChartViewModel
- [x] Injektovat ExecuteSearchUseCase
- [x] Implementovat executeSearch() s polling
- [x] Implementovat parseStatsResults()
- [x] Vytvořit ResultsPieChartScreen UI
- [x] Přidat search input field
- [x] Přidat search button s loading
- [x] Implementovat BarChart() composable s Vico
- [x] Přidat status/error karty
- [x] Přidat data details tabulku
- [x] Přidat placeholder když nejsou data
- [x] Přidat error handling
- [x] Přidat logging
- [x] Dokumentace

## 🚀 Next Steps

Pro plnou funkcionalitu spusťte v Android Studiu:
```bash
./gradlew :app:assembleDebug
```

A nebo proveďte **Gradle Sync** pro stažení Vico knihovny.

---

**Status**: ✅ Implementace dokončena, čeká na Gradle sync a build.
