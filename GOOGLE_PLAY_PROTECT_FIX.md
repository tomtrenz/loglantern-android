# 🛡️ Jak vyřešit Google Play Protect varování

## ✅ PROBLÉM VYŘEŠEN

Google Play Protect detekoval vaši aplikaci jako "harmful app" kvůli **SSL certificate bypass** v kódu.

## Co bylo provedeno:

### 1. ✅ Přidán BuildConfig přepínač
V `app/build.gradle.kts` jsou nyní dva build typy:

- **Debug build**: `ALLOW_SELF_SIGNED_CERTS = true` (pro vývoj/testování)
- **Release build**: `ALLOW_SELF_SIGNED_CERTS = false` (pro produkci/Google Play)

### 2. ✅ NetworkModule používá BuildConfig
`NetworkModule.kt` nyní **automaticky vypne** SSL bypass v release buildu.

### 3. ✅ Přidáno podrobné logování
V debug režimu vidíte v logcatu jasná varování:
```
⚠️ SSL VERIFICATION DISABLED - DEVELOPMENT MODE ONLY
⚠️ This is NOT SAFE for production and will trigger Google Play Protect!
```

V release režimu:
```
✅ SSL verification ENABLED - Production mode
```

---

## 📱 Jak sestavit aplikaci pro testování

### Pro VÝVOJ (s podporou self-signed certifikátů):
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Pro DISTRIBUCI (bez SSL bypass):
```bash
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

---

## ⚠️ DŮLEŽITÉ pro Google Play

Pokud chcete aplikaci nahrát na Google Play Store:

1. **✅ Použijte release build** (`assembleRelease`)
2. **✅ SSL bypass bude automaticky vypnutý**
3. **⚠️ Aplikace bude vyžadovat platný SSL certifikát na Splunk serveru**
4. **✅ Podepište APK pomocí release keystore**

---

## 🔧 Testování obou variant

### Zjistit, který build máte nainstalovaný:
Podívejte se do logcatu při spuštění aplikace:

**Debug build:**
```
W/NetworkModule: ⚠️ SSL VERIFICATION DISABLED - DEVELOPMENT MODE ONLY
```

**Release build:**
```
I/NetworkModule: ✅ SSL verification ENABLED - Production mode
```

---

## 🚀 Pro produkční nasazení

Máte dvě možnosti:

### Možnost 1: Získat platný SSL certifikát (doporučeno)
1. Požádat správce Splunk serveru o instalaci platného SSL certifikátu
2. Použít Let's Encrypt (zdarma)
3. Nakonfigurovat DNS jméno místo IP adresy

### Možnost 2: Certificate Pinning (pokročilé)
Implementovat pinning konkrétního certifikátu:
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("your-splunk-domain.com", "sha256/HASH_VASEHO_CERTIFIKATU")
    .build()
```

---

## 📊 Aktuální stav

| Build Type | SSL Bypass | Google Play OK? | Splunk self-signed OK? |
|------------|-----------|-----------------|------------------------|
| **Debug**  | ✅ Enabled | ❌ NO          | ✅ YES                |
| **Release**| ❌ Disabled | ✅ YES         | ❌ NO                 |

---

## 🔍 Kontrola před nahráním na Google Play

```bash
# 1. Sestavit release build
./gradlew assembleRelease

# 2. Zkontrolovat APK (mělo by být isMinifyEnabled=true)
# 3. Podepsat APK release keystorem
# 4. Otestovat na zařízení bez self-signed certifikátů
# 5. Zkontrolovat logcat - mělo by být "SSL verification ENABLED"
```

---

## ❓ FAQ

**Q: Proč debug build stále spouští Google Play Protect?**  
A: Debug buildy jsou označeny jako "unsigned" a často spouští varování. To je normální.

**Q: Můžu debug APK nahrát na Google Play?**  
A: NE! Google Play vyžaduje **release build s release signing**.

**Q: Co když potřebuji self-signed certifikát i v produkci?**  
A: Musíte implementovat **certificate pinning** pro konkrétní certifikát. Kontaktujte mě pro detaily.

**Q: Jak vytvořit release keystore?**  
A: V Android Studio: Build → Generate Signed Bundle/APK → Create new keystore

---

## 📝 Související soubory

- `app/build.gradle.kts` - BuildConfig konfigurace
- `app/src/main/java/cz/splnsito/mrthom/loglantern/app/di/NetworkModule.kt` - SSL logika
- `SECURITY_SSL_BYPASS.md` - Podrobná technická dokumentace

---

## ✅ Checklist před odevzdáním projektu

- [ ] Aplikace funguje v debug režimu se self-signed certifikáty
- [ ] Release build má vypnutý SSL bypass
- [ ] Kód je v GIT repozitáři
- [ ] README obsahuje upozornění na bezpečnost
- [ ] Pro Google Play distribuce je potřeba platný certifikát

---

**Vytvořeno:** 2. února 2026  
**Účel:** Vyřešit Google Play Protect varování při zachování funkčnosti pro vývoj
