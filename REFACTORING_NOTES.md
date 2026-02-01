# LogLantern - Refaktoring přihlašování (2026-02-01)

## Shrnutí změn

Kompletně přepracováno přihlašování pro získání Splunk access tokenů podle dokumentace:
https://help.splunk.com/en/splunk-enterprise/administer/manage-users-and-security/10.2/authenticate-into-the-splunk-platform-with-tokens/manage-or-delete-authentication-tokens

### Co bylo změněno

#### 1. **Nový přihlašovací tok**
- **Starý způsob:** Dvoufázové přihlášení (POST login → session key → GET tokens)
- **Nový způsob:** Jednofázové přihlášení (GET tokens s Basic Auth)
- Endpoint: `GET /services/authorization/tokens?username=<username>`
- Autentizace: `Basic <base64(username:password)>`

#### 2. **XML parsování místo JSON**
- Přidána závislost: `retrofit2-converter-simplexml`
- Vytvořeny nové datové modely pro XML response:
  - `SplunkTokenResponse` (root feed element)
  - `TokenEntry` (jednotlivé tokeny)
  - `TokenKey` (klíč-hodnota páry v XML)

#### 3. **Rozšíření ukládání dat**
`SettingsDataStore` nyní ukládá:
- Base URL (již dříve)
- **Username** (nově)
- **Token expiry** (nově - formátováno jako datum)

#### 4. **Zjednodušení architektury**

**AuthRepository:**
- Odstraněno: `login()`, `getAuthToken()`, `saveToken()`, `saveBaseUrl()`
- Přidáno: `fetchAndSaveToken(baseUrl, username, password)` - vše v jedné metodě
- `clearData()` - smaže token i všechna nastavení

**LoginUseCase:**
- Zjednodušeno na volání jediné metody `fetchAndSaveToken`
- Parametry: baseUrl, username, password (nikoli LoginRequest objekt)

**SplunkAuthViewModel:**
- Odstraněno použití `LoginRequest`
- Přímé volání s parametry

#### 5. **Settings Screen - nové zobrazení**
Nyní zobrazuje:
- Base URL
- Username
- Token (prvních 20 znaků)
- Token Expiry (datum a čas)

#### 6. **Odstraněné soubory**
- `LoginRequest.kt` - již nepotřebný
- `LoginResponse.kt` - již nepotřebný  
- `TokenListResponse.kt` - nahrazeno `SplunkTokenResponse.kt`

### Logika získání tokenu

```kotlin
1. Uživatel zadá: baseUrl, username, password
2. Uložíme baseUrl do DataStore
3. Vytvoříme Basic Auth header: "Basic base64(username:password)"
4. Zavoláme GET /services/authorization/tokens?username=<username>
5. Parsujeme XML odpověď
6. Najdeme první token se status="enabled"
7. Uložíme:
   - Token value do TokenStorage (encrypted)
   - Username do SettingsDataStore
   - Token expiry do SettingsDataStore (formátováno)
```

### Příklad curl požadavku

```bash
curl -k -u 'admin:password' -X GET \
  https://192.168.99.70:8089/services/authorization/tokens?username=admin
```

### Testování

1. Spustit aplikaci
2. Na login screen zadat:
   - Base URL: `https://192.168.99.70:8089`
   - Username: `admin`
   - Password: vaše heslo
3. Po úspěšném přihlášení přejít na Settings
4. Ověřit, že se zobrazují všechna data

### Debug logování

V `AuthRepositoryImpl` jsou přidány logy pro sledování procesu:
- Počet přijatých tokenů
- Status každého tokenu
- Hodnota a platnost zvoleného tokenu

Filtrujte Logcat podle tagu: `AuthRepositoryImpl`

### Bezpečnost

- **Heslo se NIKDY neukládá** - používá se pouze pro autentizaci
- Token je uložen v `EncryptedSharedPreferences`
- Base URL, username a token expiry v `DataStore`

---

**Poznámka:** Pro produkci nastavit `DEBUG = false` v `NetworkModule.kt` pro vypnutí HTTP logování.
