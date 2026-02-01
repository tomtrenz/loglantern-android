# Refaktoring na xmlutil - LogLantern

## Proč přechod na xmlutil?

### Problémy se SimpleXML:

1. **Nemůže kombinovat `@Text` a `@Element`** ve stejné třídě
2. **Slabá podpora pro CDATA** - vyžaduje `data=true` parametr
3. **Složité anotace** - `@Root`, `@Path`, `@ElementList` s mnoha parametry
4. **Staršíprojekt** - poslední update 2015
5. **Obtížné řešení vnořených struktur**

### Výhody xmlutil:

✅ **Moderní Kotlin multiplatform knihovna**  
✅ **Plná integrace s kotlinx.serialization**  
✅ **Automatické zpracování CDATA**  
✅ **Lepší podpora namespaců**  
✅ **Čistší, jednodušší API**  
✅ **Aktivně vyvíjeno** (2024+)

---

## Provedené změny

### 1. Aktualizace závislostí

**`gradle/libs.versions.toml`:**
```toml
[versions]
xmlutil = "0.86.3"

[libraries]
xmlutil-core = { group = "io.github.pdvrieze.xmlutil", name = "core-android", version.ref = "xmlutil" }
xmlutil-serialization = { group = "io.github.pdvrieze.xmlutil", name = "serialization-android", version.ref = "xmlutil" }
```

**`app/build.gradle.kts`:**
```kotlin
plugins {
    // ...
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
}

dependencies {
    // Odebrány:
    // implementation(libs.retrofit.converter.simplexml)
    
    // Přidány:
    implementation(libs.xmlutil.core)
    implementation(libs.xmlutil.serialization)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
}
```

### 2. Nové XML datové modely

**Před (SimpleXML):**
```kotlin
@Root(name = "key", strict = false)
data class SplunkKeyElement(
    @field:Attribute(name = "name", required = false)
    var name: String? = null,
    
    @field:Element(name = "dict", required = false)
    var nestedDict: SplunkNestedDict? = null,
    
    @field:Text(required = false, data = true)  // ← Problém!
    var textValue: String? = null
)
```

**Po (xmlutil):**
```kotlin
@Serializable
@XmlSerialName("key", "http://dev.splunk.com/ns/rest", "s")
data class SplunkKey(
    @XmlElement(false)
    val name: String? = null,
    
    @XmlValue(true)  // ← CDATA automaticky zpracováno!
    val value: String = "",
    
    @XmlElement(false)
    @XmlSerialName("dict", "http://dev.splunk.com/ns/rest", "s")
    val dict: SplunkDict? = null
)
```

### 3. Vlastní Retrofit Converter

Vytvořen `XmlUtilConverterFactory.kt`:

```kotlin
class XmlUtilConverterFactory private constructor(
    private val xml: XML
) : Converter.Factory() {
    
    override fun responseBodyConverter(...): Converter<ResponseBody, *> {
        val serializer = serializer(type) as KSerializer<Any>
        return XmlResponseBodyConverter(xml, serializer)
    }
    
    companion object {
        fun create(xml: XML = XML {
            repairNamespaces = true
            xmlDeclMode = XmlDeclMode.None
        }): XmlUtilConverterFactory {
            return XmlUtilConverterFactory(xml)
        }
    }
}
```

### 4. Aktualizace NetworkModule

```kotlin
@Provides
fun provideRetrofitBuilder(okHttpClient: OkHttpClient): Retrofit.Builder {
    return Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(XmlUtilConverterFactory.create())  // ← xmlutil
        .addConverterFactory(GsonConverterFactory.create())
}
```

---

## Klíčové anotace xmlutil

| Anotace | Použití | Popis |
|---------|---------|-------|
| `@Serializable` | Na data class | Označuje třídu pro serializ aci |
| `@XmlSerialName(name, namespace, prefix)` | Na data class nebo property | Mapování na XML element |
| `@XmlElement(value)` | Na property | `true` = list elementů, `false` = single |
| `@XmlValue(true)` | Na property | Textový obsah elementu (včetně CDATA) |

### Příklady:

#### Root element s namespace
```kotlin
@Serializable
@XmlSerialName("feed", "http://www.w3.org/2005/Atom", "")
data class SplunkTokenResponse(...)
```

#### Element s prefixem
```kotlin
@XmlSerialName("dict", "http://dev.splunk.com/ns/rest", "s")
data class SplunkDict(...)
```

#### Seznam elementů
```kotlin
@XmlElement(true)
@XmlSerialName("key", "http://dev.splunk.com/ns/rest", "s")
val keys: List<SplunkKey> = emptyList()
```

#### Textový obsah (CDATA)
```kotlin
@XmlValue(true)
val value: String = ""
```

---

## Výhody v praxi

### 1. Automatické CDATA zpracování

**SimpleXML:**
```kotlin
@field:Text(required = false, data = true)  // Musí se explicitně nastavit
var textValue: String? = null
```

**xmlutil:**
```kotlin
@XmlValue(true)  // CDATA automaticky
val value: String = ""
```

### 2. Jednoduší namespace handling

**SimpleXML:** Složité, často nefunguje správně

**xmlutil:**
```kotlin
@XmlSerialName("key", "http://dev.splunk.com/ns/rest", "s")
// ↑ name   ↑ namespace                            ↑ prefix
```

### 3. Kombinace value a elementů

**SimpleXML:** ❌ Nelze - `TextException`

**xmlutil:** ✅ Funguje:
```kotlin
@Serializable
data class SplunkKey(
    @XmlValue(true)
    val value: String = "",        // Textový obsah
    
    @XmlElement(false)
    val dict: SplunkDict? = null   // Vnořený element
)
```

### 4. Null safety

**xmlutil** s Kotlin serialization má lepší podporu pro nullable typy a default hodnoty.

---

## Testování

Po refaktoringu by mělo všechno fungovat stejně, ale s čistším kódem:

```
D  Creating new token for user: admin
D  Received 1 token entries in response
D  Token ID: 07d9f5624f73d5c7a2c99f137712d30a...
D  Token created successfully: eyJraWQiOiJzcGx1bmsuc2..., expires: 2026-03-01 12:15:53
```

---

## Odkazy

- [xmlutil GitHub](https://github.com/pdvrieze/xmlutil)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [xmlutil dokumentace](https://pdvrieze.github.io/xmlutil/)

---

## Shrnutí

| Feature | SimpleXML | xmlutil |
|---------|-----------|---------|
| CDATA podpora | Složité (`data=true`) | Automatické |
| Kotlin support | Java-style anotace | Kotlin native |
| Namespace | Problematické | Výborné |
| @Text + @Element mix | ❌ Nelze | ✅ Funguje |
| Aktivní vývoj | ❌ (2015) | ✅ (2024+) |
| Multiplatform | ❌ | ✅ |

**Závěr:** xmlutil je modernější, čistší a spolehlivější řešení pro XML parsing v Kotlinu.
