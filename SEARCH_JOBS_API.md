# Splunk Search Jobs API - LogLantern

## Přidána podpora pro Splunk Search Jobs

### 📋 Nově vytvořené soubory:

1. **`SplunkSearchModels.kt`** - JSON datové modely pro Search Jobs API
2. **`SearchRepository.kt`** - Repository interface pro search operace
3. **`SearchRepositoryImpl.kt`** - Implementace search repository
4. **`ExecuteSearchUseCase.kt`** - Use case pro kompletní search workflow

### 🔧 Upravené soubory:

1. **`SplunkApiService.kt`** - Přidány 3 nové endpointy
2. **`RepositoryModule.kt`** - Přidán binding pro SearchRepository

---

## 📊 API Endpointy

### 1. Create Search Job
```kotlin
@POST("services/search/jobs")
suspend fun createSearchJob(
    @Header("Authorization") token: String,
    @Field("search") searchQuery: String,
    @Field("output_mode") outputMode: String = "json"
): SplunkSearchJobResponse
```

**Použití:**
```kotlin
val response = api.createSearchJob(
    token = "Bearer eyJraWQiOiJzcGx1bmsuc2...",
    searchQuery = "search index=main | head 100"
)
// response.sid = "1707678123.456"
```

### 2. Get Job Status
```kotlin
@GET("services/search/jobs/{sid}")
suspend fun getJobStatus(
    @Header("Authorization") token: String,
    @Path("sid") sid: String,
    @Query("output_mode") outputMode: String = "json"
): SplunkJobStatusResponse
```

**Použití:**
```kotlin
val status = api.getJobStatus(
    token = "Bearer eyJraWQiOiJzcGx1bmsuc2...",
    sid = "1707678123.456"
)
// status.entry[0].content.isDone = true
```

### 3. Get Results
```kotlin
@GET("services/search/jobs/{sid}/results")
suspend fun getResults(
    @Header("Authorization") token: String,
    @Path("sid") sid: String,
    @Query("output_mode") outputMode: String = "json",
    @Query("count") count: Int = 0
): SplunkSearchResultResponse
```

**Použití:**
```kotlin
val results = api.getResults(
    token = "Bearer eyJraWQiOiJzcGx1bmsuc2...",
    sid = "1707678123.456",
    count = 100  // 0 = všechny
)
// results.results = [{_time: "...", _raw: "..."}, ...]
```

---

## 📦 Datové modely

### SplunkSearchJobResponse
```kotlin
data class SplunkSearchJobResponse(
    val sid: String  // Search ID pro polling
)
```

### JobStatusContent
```kotlin
data class JobStatusContent(
    val sid: String? = null,
    val isDone: Boolean = false,           // Job dokončen?
    val isFailed: Boolean = false,          // Job selhal?
    val isFinalized: Boolean = false,
    val dispatchState: String? = null,      // "DONE", "RUNNING", etc.
    val doneProgress: Double = 0.0,         // 0.0 - 1.0
    val scanCount: Int = 0,
    val eventCount: Int = 0,
    val resultCount: Int = 0                // Počet výsledků
)
```

### SplunkSearchResultResponse
```kotlin
data class SplunkSearchResultResponse(
    val results: List<Map<String, Any>> = emptyList(),  // Dynamické výsledky
    val fields: List<ResultField> = emptyList(),        // Dostupné pole
    val preview: Boolean = false
)
```

---

## 🏗️ Repository Layer

### SearchRepository Interface

```kotlin
interface SearchRepository {
    // Vytvoří search job
    suspend fun createSearchJob(searchQuery: String): Result<String>
    
    // Získá status jobu
    suspend fun getJobStatus(sid: String): Result<JobStatusContent>
    
    // Získá výsledky
    suspend fun getSearchResults(sid: String, count: Int = 0): Result<SplunkSearchResultResponse>
    
    // Čeká na dokončení s polling
    suspend fun waitForJobCompletion(
        sid: String,
        maxWaitTime: Long = 60000,
        pollingInterval: Long = 1000
    ): Result<JobStatusContent>
}
```

### SearchRepositoryImpl - Klíčové featury:

1. **Automatický token** - Získává token z `TokenStorage`
2. **Bearer autentizace** - `Authorization: Bearer <token>`
3. **Polling mechanismus** - `waitForJobCompletion()` aktivně čeká na dokončení
4. **Detailní logging** - Debug logy pro každou operaci
5. **Error handling** - Všechny metody vrací `Result<T>`

---

## 🎯 Use Case Layer

### ExecuteSearchUseCase

**Kompletní workflow v jedné metodě:**

```kotlin
class ExecuteSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(
        searchQuery: String,
        maxWaitTime: Long = 60000,
        resultCount: Int = 0
    ): Result<SplunkSearchResultResponse> {
        // 1. Vytvoří search job
        // 2. Počká na dokončení (polling)
        // 3. Vrátí výsledky
    }
}
```

**Příklad použití ve ViewModelu:**

```kotlin
viewModelScope.launch {
    executeSearchUseCase(
        searchQuery = "search index=main earliest=-1h | stats count by host",
        maxWaitTime = 60000,
        resultCount = 100
    ).onSuccess { results ->
        // Zobraz v tabulce nebo grafu
        _uiState.update { it.copy(searchResults = results.results) }
    }.onFailure { error ->
        // Zobraz chybu
        _uiState.update { it.copy(error = error.message) }
    }
}
```

---

## 🔄 Search Workflow

### Krok za krokem:

```
1. Uživatel zadá SPL query
   ↓
2. createSearchJob(query)
   → Vrátí SID (Search ID)
   ↓
3. waitForJobCompletion(sid)
   → Polling každou sekundu
   → Kontroluje isDone / isFailed
   ↓
4. getSearchResults(sid)
   → Vrátí JSON výsledky
   ↓
5. Zobrazení v UI (tabulka/graf)
```

### Polling detail:

```kotlin
while (!timeout) {
    val status = getJobStatus(sid)
    
    if (status.isDone) {
        return success(status)
    }
    
    if (status.isFailed) {
        return failure("Job failed")
    }
    
    delay(1000ms)  // Počkej 1s před dalším pollováním
}
```

---

## 📱 Integrace do UI

### Pro ResultsTableScreen:

```kotlin
@HiltViewModel
class ResultsTableViewModel @Inject constructor(
    private val executeSearchUseCase: ExecuteSearchUseCase
) : ViewModel() {

    fun search(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            executeSearchUseCase(query).onSuccess { results ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        tableData = results.results,
                        columns = results.fields.map { it.name ?: "" }
                    )
                }
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(isLoading = false, error = error.message)
                }
            }
        }
    }
}
```

### Pro ResultsPieChartScreen:

```kotlin
fun loadChartData(query: String) {
    viewModelScope.launch {
        executeSearchUseCase(
            searchQuery = "search $query | stats count by category"
        ).onSuccess { results ->
            val chartData = results.results.map { row ->
                PieChartEntry(
                    label = row["category"] as? String ?: "Unknown",
                    value = (row["count"] as? Number)?.toFloat() ?: 0f
                )
            }
            _uiState.update { it.copy(chartData = chartData) }
        }
    }
}
```

---

## 🧪 Příklady SPL queries

### Pro tabulku:
```spl
search index=main earliest=-1h latest=now
| head 100
| table _time, host, source, _raw
```

### Pro graf (pie chart):
```spl
search index=main earliest=-24h
| stats count by sourcetype
```

### Pro časovou sérii:
```spl
search index=main earliest=-1h
| timechart span=5m count
```

---

## ✅ Co je hotovo:

- ✅ JSON datové modely pro všechny API responses
- ✅ 3 API endpointy (create, status, results)
- ✅ SearchRepository s polling mechanikou
- ✅ ExecuteSearchUseCase pro kompletní workflow
- ✅ Hilt DI binding
- ✅ Error handling a logging
- ✅ Bearer token autentizace

---

## 🚀 Další kroky:

1. **Implementovat ResultsTableViewModel** s `executeSearchUseCase`
2. **Implementovat ResultsChartViewModel** s `executeSearchUseCase`
3. **UI komponenty** pro zobrazení tabulky a grafu
4. **Loading states** během polling
5. **Error handling** v UI

---

**Search Jobs API je připraveno! 🎉**

Můžeme nyní vytvářet search jobs, pollovat jejich status a získávat výsledky pro tabulky a grafy!
