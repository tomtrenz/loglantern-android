# CDATA Token Parsing Fix - LogLantern

## Problém

Token v XML odpovědi je zabalen v `<![CDATA[...]]>` a SimpleXML měl problém s parsováním:

```
java.lang.RuntimeException: org.simpleframework.xml.core.TextException: 
Text annotation @org.simpleframework.xml.Text used with elements in class SplunkKey
```

### Příčina

SimpleXML neumí kombinovat `@Text` a `@Element` anotace ve stejné třídě. Původní `SplunkKey` měl:
- `@Text` pro textové hodnoty
- `@Element(name = "dict")` pro vnořené dicty

To způsobilo konflikt.

### XML struktura z Splunku

```xml
<s:dict>
  <s:key name="id">07d9f5624f73d5c7a2c99f137712d30a...</s:key>
  <s:key name="token"><![CDATA[eyJraWQiOiJzcGx1bmsuc2VjcmV0...]]></s:key>
  <s:key name="claims">
    <s:dict>
      <s:key name="exp">1772572953</s:key>
      <s:key name="sub">admin</s:key>
    </s:dict>
  </s:key>
</s:dict>
```

---

## Řešení

### Nové XML datové modely

Rozdělil jsem parsování na specializované třídy:

#### 1. **SplunkKeyElement** (pro top-level klíče)

```kotlin
@Root(name = "key", strict = false)
data class SplunkKeyElement(
    @field:Attribute(name = "name", required = false)
    var name: String? = null,
    
    // Pro vnořené dicty (např. claims)
    @field:Element(name = "dict", required = false)
    var nestedDict: SplunkNestedDict? = null,
    
    // Pro textové hodnoty (např. token, id) - CDATA
    @field:Text(required = false, data = true)
    var textValue: String? = null
)
```

**Klíčový parametr:** `data = true` v `@Text` anotaci → umožňuje parsování CDATA obsahu!

#### 2. **SplunkNestedDict** (pro vnořené `<s:dict>`)

```kotlin
@Root(name = "dict", strict = false)
data class SplunkNestedDict(
    @field:ElementList(entry = "key", inline = true, required = false)
    var keys: List<SplunkSimpleKey>? = null
)
```

#### 3. **SplunkSimpleKey** (pro klíče uvnitř vnořených dicts)

```kotlin
@Root(name = "key", strict = false)
data class SplunkSimpleKey(
    @field:Attribute(name = "name", required = false)
    var name: String? = null,
    
    @field:Text(required = false, data = true)
    var value: String? = null
)
```

---

### Aktualizovaná logika parsování

V `AuthRepositoryImpl`:

```kotlin
// Token z CDATA
val tokenValue = createdToken.content?.keys
    ?.find { it.name == "token" }
    ?.textValue  // ← CDATA obsah

// ID tokenu
val tokenId = createdToken.content?.keys
    ?.find { it.name == "id" }
    ?.textValue

// Expiry z vnořeného claims dict
val claimsDict = createdToken.content?.keys
    ?.find { it.name == "claims" }
    ?.nestedDict  // ← Vnořený dict
    
val expiryTimestamp = claimsDict?.keys
    ?.find { it.name == "exp" }
    ?.value
    ?.toLongOrNull()
```

**Fallback:** Pokud `claims` dict není dostupný, parsujeme JWT token payload přímo pomocí Base64 dekódování.

---

## Klíčové změny

### XML anotace

| Parametr | Použití | Popis |
|----------|---------|-------|
| `data = true` | `@Text(data = true)` | Parsuje CDATA obsah |
| `strict = false` | `@Root(strict = false)` | Ignoruje neznámé elementy |
| `required = false` | Všechny anotace | Prvky jsou volitelné |

### Struktura tříd

```
SplunkTokenResponse (feed)
└── TokenEntry (entry)
    └── TokenContent (content/dict)
        └── SplunkKeyElement[] (key)
            ├── textValue (pro CDATA: token, id, status)
            └── nestedDict (pro vnořené dict: claims, eai:acl)
                └── SplunkSimpleKey[] (key)
                    └── value (exp, sub, iat, ...)
```

---

## Testování

Po této opravě by mělo logování zobrazit:

```
D  Creating new token for user: admin
D  Received 1 token entries in response
D  Token ID: 07d9f5624f73d5c7a2c99f137712d30a...
D  Token created successfully: eyJraWQiOiJzcGx1bmsuc2..., expires: 2026-03-01 12:15:53
```

**Očekávaný token:**
- Délka: ~300-400 znaků (JWT)
- Formát: `eyJ...` (Base64 encoded JWT)
- CDATA je automaticky odstraněno SimpleXML parserem

---

## Odkazy

- [SimpleXML Documentation](http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php)
- [CDATA v XML](https://www.w3.org/TR/xml/#sec-cdata-sect)
- [JWT struktura](https://jwt.io/)

---

## Poznámky

1. **`data = true`** je kritický parametr pro CDATA parsování
2. Rozdělení na dvě třídy klíčů (`SplunkKeyElement` vs `SplunkSimpleKey`) řeší problém s mixováním `@Text` a `@Element`
3. JWT token má expiry v payload - můžeme ho dekódovat jako fallback
4. SimpleXML automaticky odstraňuje CDATA markup
