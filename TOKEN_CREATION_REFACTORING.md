# Token Creation Refactoring - LogLantern

## Změna přístupu k tokenům

### Původní přístup (ZASTARALÝ)
- GET `/services/authorization/tokens?username=admin`
- Získání existujícího povoleného tokenu
- Problém: Co když token neexistuje?

### Nový přístup (AKTUÁLNÍ)
- POST `/services/authorization/tokens`
- **Vytvoření nového tokenu** při každém přihlášení
- Uživatel vidí token v dialogu a může ho zkopírovat
- Token je uložen bezpečně pro další použití

---

## Implementované změny

### 1. **SplunkApiService** - Přidána POST metoda

```kotlin
@FormUrlEncoded
@POST("services/authorization/tokens")
suspend fun createToken(
    @Header("Authorization") authHeader: String,
    @Field("name") username: String,
    @Field("audience") audience: String,
    @Field("type") type: String = "static"
): SplunkTokenResponse
```

**Parametry:**
- `name` - uživatelské jméno (pro koho je token vytvořen)
- `audience` - **"LogParser"** (identifikace aplikace)
- `type` - **"static"** (dlouhodobý token)

**cURL ekvivalent:**
```bash
curl -u 'admin:password' -X POST \
  https://192.168.99.70:8089/services/authorization/tokens \
  --data name=admin \
  --data audience=LogParser \
  --data type=static
```

### 2. **AuthRepository & AuthRepositoryImpl**

**Nové rozhraní:**
```kotlin
data class TokenInfo(
    val token: String,
    val expiry: String
)

interface AuthRepository {
    suspend fun createAndSaveToken(
        baseUrl: String, 
        username: String, 
        password: String
    ): Result<TokenInfo>
    
    suspend fun clearData()
}
```

**Změna v implementaci:**
- ❌ `fetchAndSaveToken()` - ODSTRANĚNO (staré GET)
- ✅ `createAndSaveToken()` - NOVÉ (POST + vytvoření tokenu)
- ✅ Vrací `TokenInfo` s tokenem a platností

### 3. **LoginUseCase**

```kotlin
suspend operator fun invoke(
    baseUrl: String, 
    username: String, 
    password: String
): Result<TokenInfo>  // Nyní vrací TokenInfo místo Unit
```

### 4. **SplunkAuthViewModel**

**Rozšířený UiState:**
```kotlin
data class SplunkAuthUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null,
    val createdToken: String? = null,      // NOVÉ
    val tokenExpiry: String? = null        // NOVÉ
)
```

**Nová metoda:**
```kotlin
fun acknowledgeToken() {
    _uiState.update { it.copy(createdToken = null, tokenExpiry = null) }
}
```

### 5. **SplunkLoginScreen** - Zobrazení tokenu

**Přidán AlertDialog:**
- Zobrazí se po úspěšném vytvoření tokenu
- Ukáže celý token + platnost
- Tlačítko **"Copy & Continue"** - zkopíruje token do schránky
- Tlačítko **"Continue"** - pokračuje bez kopírování

**UI Flow:**
```
1. Uživatel zadá credentials
2. Klikne "Create Token & Login"
3. POST request vytvoří nový token
4. Dialog zobrazí token
5. Uživatel může zkopírovat token
6. Pokračuje na Dashboard
7. Token je již uložen v secure storage
```

---

## Bezpečnostní aspekty

✅ **Heslo se NIKDY neukládá** - pouze se použije pro Basic Auth  
✅ **Token je zobrazen pouze jednou** - v dialogu po vytvoření  
✅ **Token je uložen v EncryptedSharedPreferences**  
✅ **Možnost zkopírovat token** - pro backup nebo další použití  

---

## Testování

### Krok 1: Přihlášení
1. Spusťte aplikaci
2. Na login screen zadejte:
   - **Base URL**: `https://192.168.99.70:8089`
   - **Username**: `admin`
   - **Password**: vaše heslo
3. Klikněte "Create Token & Login"

### Krok 2: Zobrazení tokenu
- Dialog se zobrazí s:
  - Celým tokenem (např. `d7e5b88c62965d463b4f0ea07b66384a5142e455b6cf25eee1224c9d6721b2a2`)
  - Platností (např. `2026-11-10 12:06:36`)

### Krok 3: Kopírování (volitelné)
- Klikněte "Copy & Continue" pro zkopírování
- NEBO klikněte "Continue" pro přeskočení

### Krok 4: Ověření
- Přejděte na Settings
- Zkontrolujte, že token je uložen a zobrazuje se

---

## Logování (Debug)

V Logcatu filtrujte podle tagu `AuthRepositoryImpl`:

```
D  Creating new token for user: admin
D  Received 1 token entries in response
D  Token created successfully: d7e5b88c..., expires: 2026-11-10 12:06:36
```

---

## Poznámky

1. **Audience = "LogParser"** - identifikuje naši aplikaci v Splunku
2. **Type = "static"** - token je dlouhodobý (podle nastavení Splunk serveru)
3. **Token se vytváří při každém přihlášení** - starý token zůstává platný, pokud není smazán
4. **Dialog je modal** - nelze zavřít kliknutím mimo, pouze tlačítky

---

## Co dál?

Pro správu tokenů (smazání starých tokenů) lze použít DELETE request:
```bash
curl -u 'admin:password' -X DELETE \
  https://192.168.99.70:8089/services/authorization/tokens/<token_id>
```

To lze implementovat později v Settings obrazovce.
