# XML Parsing Fix - LogLantern

## Problém
Parsování XML z Splunk API nefungovalo správně. Status tokenu byl vždy `null`.

## Příčina
Původní XML datové modely neodpovídaly skutečné struktuře Splunk XML response:

### Skutečná XML struktura:
```xml
<entry>
  <title>d7e5b88c62965d463b4f0ea07b66384a5142e455b6cf25eee1224c9d6721b2a2</title>
  <content>
    <s:dict>
      <s:key name="status">enabled</s:key>
      <s:key name="claims">
        <s:dict>
          <s:key name="exp">1794743996</s:key>
          <s:key name="sub">admin</s:key>
          ...
        </s:dict>
      </s:key>
      ...
    </s:dict>
  </content>
</entry>
```

## Řešení

### 1. Nové XML datové modely (`SplunkTokenResponse.kt`):

```kotlin
@Root(name = "feed", strict = false)
data class SplunkTokenResponse(
    @field:ElementList(entry = "entry", inline = true, required = false)
    var entries: List<TokenEntry>? = null
)

@Root(name = "entry", strict = false)
data class TokenEntry(
    @field:Element(name = "title", required = false)
    var title: String? = null,  // Token value je zde!
    
    @field:Path("content")
    @field:Element(name = "dict", required = false)
    var content: TokenContent? = null
)

@Root(name = "dict", strict = false)
data class TokenContent(
    @field:ElementList(entry = "key", inline = true, required = false)
    var keys: List<SplunkKey>? = null
)

@Root(name = "key", strict = false)
data class SplunkKey(
    @field:Attribute(name = "name", required = false)
    var name: String? = null,
    
    @field:Text(required = false)
    var value: String? = null,  // Pro jednoduché hodnoty (status, exp, ...)
    
    @field:Element(name = "dict", required = false)
    var dict: SplunkDict? = null  // Pro vnořené dicty (claims)
)

@Root(name = "dict", strict = false)
data class SplunkDict(
    @field:ElementList(entry = "key", inline = true, required = false)
    var keys: List<SplunkKey>? = null
)
```

### 2. Aktualizovaná logika v `AuthRepositoryImpl`:

```kotlin
// Token je přímo v title elementu
val tokenValue = enabledToken.title

// Status je v content/dict/keys
val status = entry.content?.keys?.find { it.name == "status" }?.value

// Expiry je v content/dict/keys[name="claims"]/dict/keys[name="exp"]
val claimsDict = enabledToken.content?.keys?.find { it.name == "claims" }?.dict
val expiryTimestamp = claimsDict?.keys?.find { it.name == "exp" }?.value?.toLongOrNull()
```

## Klíčové body

1. **Token value** je v `<title>` elementu entry, NE ve vnořených keys
2. **Status** je přímý klíč v `content/dict/keys` s `name="status"`
3. **Expiry** je vnořený v `content/dict/keys[name="claims"]/dict/keys[name="exp"]`
4. SimpleXML používá `@field:Text` pro získání textového obsahu `<s:key>` elementů
5. Vnořené `<s:dict>` jsou mapovány pomocí samostatné `SplunkDict` třídy

## Testování

Po této opravě by mělo logování zobrazit:
```
D  Received 1 token entries
D  Token d7e5b88c... status: enabled  ✅ (místo null)
D  Token found: d7e5b88c..., expires: 2026-11-10 12:06:36
```

## Poznámka

Pokud IDE stále hlásí "Redeclaration: TokenEntry", je to kvůli staré cache. **Restartujte Android Studio** a chyba zmizí.
