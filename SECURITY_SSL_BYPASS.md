# ⚠️ SSL Certificate Bypass - Bezpečnostní varování

## Problém

Google Play Protect detekuje aplikaci jako "harmful app" kvůli **obcházení SSL certifikátové validace** v souboru:
- `app/src/main/java/cz/splnsito/mrthom/loglantern/app/di/NetworkModule.kt`

## Co způsobuje varování?

V kódu jsou implementovány tyto bezpečnostní obchůzky:

1. **TrustManager který akceptuje všechny SSL certifikáty** (i neplatné, self-signed)
2. **HostnameVerifier který akceptuje všechny hostname** (vypnutá validace DNS)

```kotlin
// NEBEZPEČNÝ KÓD - akceptuje všechny certifikáty
val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
    override fun checkClientTrusted(...) {} // prázdná implementace
    override fun checkServerTrusted(...) {} // prázdná implementace
    override fun getAcceptedIssuers() = arrayOf()
})

// NEBEZPEČNÝ KÓD - akceptuje všechny hosty
builder.hostnameVerifier { _, _ -> true }
```

## Proč je to v kódu?

Tento kód je nutný pro **vývojové/testovací prostředí**, protože:
- Splunk server používá **self-signed SSL certifikát** (např. `https://95.82.185.87:10456`)
- Interní servery často nemají platné CA-signed certifikáty
- Pro testování je potřeba spojení i s neplatným certifikátem

## 🔴 RIZIKO PRO PRODUKCI

Tento kód vytváří **KRITICKOU bezpečnostní díru**:
- Aplikace přijme certifikát od kohokoli (man-in-the-middle útok)
- Data mohou být odposlouchávána nebo modifikována
- Hesla a tokeny mohou být ukradeny
- Google Play aplikaci zamítne nebo označí jako malware

## ✅ Řešení

### Pro VÝVOJ/TESTOVÁNÍ:
Ponechte konstantu `ALLOW_SELF_SIGNED_CERTS = true` v `NetworkModule.kt`

### Pro PRODUKCI:
1. **Nastavte `ALLOW_SELF_SIGNED_CERTS = false`** v `NetworkModule.kt`
2. Požádejte správce Splunk serveru o **platný SSL certifikát** (Let's Encrypt, atd.)
3. Nebo implementujte **certificate pinning** pro konkrétní server

### Lepší řešení - BuildConfig varianty:

V `build.gradle.kts (app)`:
```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("boolean", "ALLOW_SELF_SIGNED", "true")
        }
        release {
            buildConfigField("boolean", "ALLOW_SELF_SIGNED", "false")
        }
    }
}
```

Pak v `NetworkModule.kt`:
```kotlin
private const val ALLOW_SELF_SIGNED_CERTS = BuildConfig.ALLOW_SELF_SIGNED
```

## 🛡️ Pro distribuci přes Google Play

**PŘED NAHRÁNÍM NA GOOGLE PLAY MUSÍTE:**

1. Vypnout SSL bypass (`ALLOW_SELF_SIGNED_CERTS = false`)
2. Používat pouze servery s platnými SSL certifikáty
3. Nebo implementovat certificate pinning pro konkrétní certifikát
4. Podepsat APK release keystorem (ne debug)

## Alternativní řešení - Certificate Pinning

Pokud máte kontrolu nad serverem, můžete použít certificate pinning:

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("yourdomain.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

## Současný stav

- ✅ Kód je **označený varováními** v komentářích
- ✅ Je **viditelný přepínač** `ALLOW_SELF_SIGNED_CERTS`
- ✅ Přidáno **logování** pro debug
- ⚠️ **JE STÁLE NEBEZPEČNÉ** pro produkční použití

## Shrnutí

Pro školní projekt/testování: **v pořádku, ale označeno varováním**
Pro Google Play distribuce: **MUSÍ BÝT VYPNUTO**
Pro produkční nasazení: **POUŽÍT PLATNÉ SSL CERTIFIKÁTY**
