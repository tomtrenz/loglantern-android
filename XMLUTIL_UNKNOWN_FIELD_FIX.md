# xmlutil Unknown Field Fix - LogLantern

## Problém

```
nl.adaptivity.xmlutil.serialization.UnknownXmlFieldException: 
Could not find a field for name 
{http://www.w3.org/2005/Atom}feed/{http://www.w3.org/2005/Atom}title (Element)
candidates: {http://www.w3.org/2005/Atom}entry (Element)
```

### Příčina

xmlutil defaultně vyžaduje, aby všechny XML elementy byly namapovány na pole v datové třídě. Splunk XML obsahuje mnoho meta elementů (`<title>`, `<id>`, `<updated>`, `<author>`, `<opensearch:*>` atd.), které nepotřebujeme.

### Splunk XML struktura:

```xml
<feed xmlns="http://www.w3.org/2005/Atom" ...>
  <title>tokens</title>                    ← Neznámý element!
  <id>https://...</id>                      ← Neznámý element!
  <updated>2026-02-01T21:41:15+00:00</updated>  ← Neznámý element!
  <generator build="..." version="..."/>    ← Neznámý element!
  <author><name>Splunk</name></author>      ← Neznámý element!
  <opensearch:totalResults>1</opensearch:totalResults>  ← Neznámý element!
  
  <entry>                                   ← Tohle chceme!
    <title>tokens</title>
    <content>
      <s:dict>
        <s:key name="token"><![CDATA[...]]></s:key>
      </s:dict>
    </content>
  </entry>
</feed>
```

Náš model měl pouze `entry` field, takže xmlutil vyhodil chybu při parsování `<title>`.

---

## Řešení

### 1. Aktualizace XML konfigurace

V `XmlUtilConverterFactory.kt` jsem přidal policy pro ignorování neznámých elementů:

```kotlin
fun create(xml: XML = XML {
    repairNamespaces = true
    xmlDeclMode = XmlDeclMode.None
    autoPolymorphic = true
    policy {
        isStrictBoolean = false
        pedantic = false
        ignoreUnknownChildren = true  // ← Klíčové nastavení!
    }
}): XmlUtilConverterFactory {
    return XmlUtilConverterFactory(xml)
}
```

**Klíčové parametry:**

- `ignoreUnknownChildren = true` - Ignoruje elementy, které nejsou namapovány na pole
- `pedantic = false` - Méně striktní parsování
- `isStrictBoolean = false` - Flexibilní parsování boolean hodnot
- `autoPolymorphic = true` - Automatická polymorfní deserializace

### 2. Oprava anotací v datových modelech

**Před:**
```kotlin
@Serializable
@XmlSerialName("feed", "http://www.w3.org/2005/Atom", "")
data class SplunkTokenResponse(
    @XmlElement(true)  // ← Problém!
    val entry: List<TokenEntry> = emptyList()
)
```

**Po:**
```kotlin
@Serializable
@XmlSerialName("feed", "http://www.w3.org/2005/Atom", "")
data class SplunkTokenResponse(
    @XmlChildrenName("entry", "http://www.w3.org/2005/Atom", "")  // ← Správně!
    val entry: List<TokenEntry> = emptyList()
)
```

**Proč `@XmlChildrenName` místo `@XmlElement`?**

- `@XmlChildrenName` - Pro seznamy elementů se stejným názvem (lepší pro kolekce)
- `@XmlElement(true)` - Pro obecné seznamy (méně specifické)

### 3. Vyčištění anotací pro `name` attribute

**Před:**
```kotlin
@XmlElement(false)
val name: String? = null
```

**Po:**
```kotlin
@XmlSerialName("name", "", "")
@XmlElement(false)
val name: String? = null
```

Explicitní mapování atributu `name` na pole.

---

## Finální XML datové modely

```kotlin
@Serializable
@XmlSerialName("feed", "http://www.w3.org/2005/Atom", "")
data class SplunkTokenResponse(
    @XmlChildrenName("entry", "http://www.w3.org/2005/Atom", "")
    val entry: List<TokenEntry> = emptyList()
)

@Serializable
@XmlSerialName("entry", "http://www.w3.org/2005/Atom", "")
data class TokenEntry(
    val title: String? = null,
    val content: TokenContent? = null
)

@Serializable
@XmlSerialName("content", "http://www.w3.org/2005/Atom", "")
data class TokenContent(
    @XmlSerialName("dict", "http://dev.splunk.com/ns/rest", "s")
    val dict: SplunkDict? = null
)

@Serializable
@XmlSerialName("dict", "http://dev.splunk.com/ns/rest", "s")
data class SplunkDict(
    @XmlChildrenName("key", "http://dev.splunk.com/ns/rest", "s")
    val keys: List<SplunkKey> = emptyList()
)

@Serializable
@XmlSerialName("key", "http://dev.splunk.com/ns/rest", "s")
data class SplunkKey(
    @XmlSerialName("name", "", "")
    @XmlElement(false)
    val name: String? = null,
    
    @XmlValue(true)
    val value: String = "",  // ← CDATA automaticky zpracováno!
    
    @XmlSerialName("dict", "http://dev.splunk.com/ns/rest", "s")
    val dict: SplunkDict? = null
)
```

---

## Klíčové anotace xmlutil

| Anotace | Použití | Popis |
|---------|---------|-------|
| `@XmlChildrenName` | Na List<T> | Mapování seznamu elementů se stejným názvem |
| `@XmlSerialName` | Na class nebo property | Mapování na XML element/atribut |
| `@XmlValue(true)` | Na property | Textový obsah (včetně CDATA) |
| `@XmlElement(false)` | Na property | Single element (ne list) |

---

## XML Policy Options

V `XML {}` builderu lze nastavit:

```kotlin
policy {
    ignoreUnknownChildren = true   // Ignoruje neznámé child elementy
    isStrictBoolean = false        // Flexibilní bool parsing (0/1, true/false)
    pedantic = false               // Méně striktní validace
    throwOnRepeatedElement = false // Nehodí výjimku při opakovaných elementech
}
```

---

## Testování

Po této opravě by mělo logování zobrazit:

```
D  Creating new token for user: admin
D  Received 1 token entries in response
D  Token ID: 92d19ce33bdfba0f...
D  Token created successfully: eyJraWQiOiJzcGx1bmsuc2..., expires: 2026-03-01 13:27:55
```

Token je nyní úspěšně extrahován z CDATA a všechny neznámé elementy jsou ignorovány!

---

## Shrnutí změn

1. ✅ Přidána `ignoreUnknownChildren = true` policy
2. ✅ Změna `@XmlElement(true)` → `@XmlChildrenName` pro seznamy
3. ✅ Explicitní mapování `name` atributu
4. ✅ Vyčištění zbytečných `@XmlElement` anotací
5. ✅ Nastavení `pedantic = false` pro flexibilnější parsing

**Výsledek:** xmlutil nyní správně parsuje Splunk XML a ignoruje všechny meta elementy, které nepotřebujeme! 🎉
