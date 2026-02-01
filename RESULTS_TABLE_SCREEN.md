# ResultsTableScreen Implementation - LogLantern

## ✅ Implementováno: Plně funkční tabulka s výsledky search jobů

### 📋 Vytvořené komponenty:

1. **`ResultsTableViewModel`** - ViewModel s kompletní logikou
2. **`ResultsTableScreen`** - UI s search inputem a tabulkou
3. **`DataTable`** - Scrollovatelná tabulka pro zobrazení výsledků
4. **`TableCell`** - Stylizovaná buňka tabulky
5. **`LoadingView`** (rozšířen) - Loading s custom textem
6. **`ErrorView`** (rozšířen) - Error view s Retry tlačítkem

---

## 🎯 Funkce ResultsTableScreen

### 1. **Search Input Area**
- Multi-line `OutlinedTextField` pro SPL query (2-4 řádky)
- Placeholder: `"search index=main | head 100"`
- Defaultní query: `"search index=main earliest=-1h | head 100"`
- Disabled během loadingu

### 2. **Execute Button**
- Text se mění: "Execute Search" → "Searching..."
- Disabled když:
  - Search běží (isLoading = true)
  - Query je prázdný

### 3. **Content States**

#### Loading State:
```kotlin
LoadingView(
    text = "Executing search job...\nThis may take a few seconds"
)
```
- Zobrazuje CircularProgressIndicator
- Multi-line text s novým řádkem

#### Error State:
```kotlin
ErrorView(
    message = uiState.error ?: "Unknown error",
    onRetry = { viewModel.executeSearch() }
)
```
- Červený "Error" nadpis
- Zobrazí error message
- Retry tlačítko pro nový pokus

#### Empty Results:
```
"No results found"
```

#### Success State:
```
Results: X rows, Y columns
[Scrollovatelná tabulka]
```

---

## 📊 DataTable Component

### Struktura:

```
┌─────────────────────────────────────┐
│ Header Row (primary color)         │ ← Sticky header
├─────────────────────────────────────┤
│ Data Row 1 (surface)                │
│ Data Row 2 (surfaceVariant)         │ ← Alternating colors
│ Data Row 3 (surface)                │
│ ...                                  │
└─────────────────────────────────────┘
   ↔ Horizontal scroll
   ↕ Vertical scroll
```

### Features:

1. **Dual Scrolling:**
   - Horizontální scroll pro široké tabulky
   - Vertikální scroll pro mnoho řádků
   - Scrolly jsou nezávislé

2. **Fixed Column Width:**
   - Každá kolona: 200.dp
   - Konzistentní šířka

3. **Alternating Row Colors:**
   - Sudé řádky: `MaterialTheme.colorScheme.surface`
   - Liché řádky: `MaterialTheme.colorScheme.surfaceVariant`

4. **Border & Dividers:**
   - Vnější border: 1.dp outline
   - Header divider: HorizontalDivider
   - Row dividers: 0.5.dp outlineVariant

5. **Typography:**
   - Header: `titleSmall` + Bold
   - Data: `bodyMedium`

---

## 🔄 ViewModel Workflow

### UiState:
```kotlin
data class ResultsTableUiState(
    val searchQuery: String = "search index=main earliest=-1h | head 100",
    val isLoading: Boolean = false,
    val error: String? = null,
    val columns: List<String> = emptyList(),
    val rows: List<Map<String, Any>> = emptyList(),
    val searchExecuted: Boolean = false
)
```

### Methods:

#### 1. `updateSearchQuery(query: String)`
- Aktualizuje search query v UiState
- Volá se při změně textu v input fieldu

#### 2. `executeSearch()`
**Workflow:**
```
1. Validace query (nesmí být prázdný)
   ↓
2. Set isLoading = true
   ↓
3. Zavolá ExecuteSearchUseCase
   ↓
4. Use case:
   - Vytvoří search job
   - Polluje status (každou 1s)
   - Získá výsledky
   ↓
5. OnSuccess:
   - Extrahuje columns z fields nebo prvního řádku
   - Nastaví rows a columns
   - Set isLoading = false
   ↓
6. OnFailure:
   - Nastaví error message
   - Set isLoading = false
```

#### 3. `clearError()`
- Vymaže error message (volitelné)

---

## 📱 UI Flow

### Krok za krokem:

```
1. User otevře ResultsTableScreen
   ↓
2. Vidí default query v input fieldu
   ↓
3. Může upravit query (např. změnit index, časové rozmezí)
   ↓
4. Klikne "Execute Search"
   ↓
5. Loading: "Executing search job..."
   Polling běží na pozadí (1-60s)
   ↓
6. Success:
   - "Results: 100 rows, 5 columns"
   - Tabulka s daty
   ↓
7. User může:
   - Scrollovat tabulku (↔ ↕)
   - Změnit query a spustit nový search
```

---

## 🧪 Příklady queries

### Základní search:
```spl
search index=main earliest=-1h | head 100
```

### S filtrem:
```spl
search index=main host="webserver01" earliest=-24h | head 50
```

### S agregací:
```spl
search index=main earliest=-1h | stats count by host
```

### S tabulkou specifických polí:
```spl
search index=main earliest=-1h | table _time, host, source, _raw
```

---

## 🎨 Design Features

### Material 3 Theming:
- ✅ Používá `MaterialTheme.colorScheme`
- ✅ Používá `MaterialTheme.typography`
- ✅ Responsive spacing (8.dp, 16.dp)
- ✅ Proper padding and margins

### Accessibility:
- ✅ Alternating row colors pro čitelnost
- ✅ Dostatečný kontrast (error červená)
- ✅ Clear visual hierarchy

### Performance:
- ✅ Lazy scrolling (built-in Column scroll)
- ✅ Efficient state management (StateFlow)
- ✅ No unnecessary recompositions

---

## 🔧 Technické detaily

### Dependencies:
```kotlin
@HiltViewModel
class ResultsTableViewModel @Inject constructor(
    private val executeSearchUseCase: ExecuteSearchUseCase
) : ViewModel()
```

### State Collection:
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```
- Lifecycle-aware
- Automatické unsubscribe

### Dynamic Column Extraction:
```kotlin
val columns = results.fields.mapNotNull { it.name }.ifEmpty {
    // Fallback: extrahuj z prvního řádku
    results.results.firstOrNull()?.keys?.toList() ?: emptyList()
}
```

### Dynamic Row Rendering:
```kotlin
rows.forEachIndexed { rowIndex, row ->
    columns.forEach { column ->
        val value = row[column]?.toString() ?: ""
        TableCell(text = value, ...)
    }
}
```

---

## ✅ Co funguje:

- ✅ Search query input (multi-line)
- ✅ Execute button s validací
- ✅ Loading state s progress indicator
- ✅ Error handling s retry
- ✅ Empty results message
- ✅ Dual-scroll tabulka
- ✅ Alternating row colors
- ✅ Dynamic columns a rows
- ✅ Material 3 design
- ✅ Lifecycle-aware state
- ✅ Hilt DI injection

---

## 🚀 Další možná vylepšení:

1. **Export do CSV** - Tlačítko pro export výsledků
2. **Column Sorting** - Kliknutí na header pro sort
3. **Column Filtering** - Filter pro jednotlivé sloupce
4. **Search History** - Dropdown s předchozími queries
5. **Pagination** - Pro velmi velké resultsety
6. **Column Resize** - Možnost měnit šířku sloupců
7. **Copy Cell** - Long press pro kopírování hodnoty

---

**ResultsTableScreen je plně funkční! 🎉**

Můžete spustit libovolný SPL query a vidět výsledky v přehledné, scrollovatelné tabulce s Material 3 designem!
