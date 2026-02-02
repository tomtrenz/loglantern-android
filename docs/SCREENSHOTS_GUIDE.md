# 📸 Screenshots Guide

## Jak přidat screenshoty do README

### 1. Pořízení screenshotů

**V Android Emulátoru:**
1. Spusťte aplikaci v emulátoru
2. Projděte všechny screeny
3. Použijte Screenshot tool v emulátoru (ikona fotoaparátu)
4. Nebo `Ctrl+S` / `Cmd+S` pro screenshot

**Na fyzickém zařízení:**
1. Spusťte aplikaci
2. Pro každou obrazovku: `Power + Volume Down`
3. Přeneste screenshoty do počítače

---

### 2. Potřebné screenshoty

Vytvořte screenshoty pro tyto obrazovky:

- [ ] `splash.png` - Splash screen s logem
- [ ] `login.png` - Login screen (před přihlášením)
- [ ] `dashboard.png` - Dashboard s bottom navigation
- [ ] `table.png` - Results Table se search výsledky
- [ ] `piechart.png` - Pie Chart s grafem statistik
- [ ] `settings.png` - Settings screen s informacemi
- [ ] `navigation.png` - Ukázka bottom navigation baru

---

### 3. Úprava screenshotů (volitelné)

**Doporučené rozměry:**
- Portrait: 1080x1920 (9:16)
- Nebo využít Device Frame v Android Studio

**Nástroje:**
- Android Studio Device Frame Screenshot
- Online: https://mockuphone.com/
- Photoshop/GIMP pro úpravy

---

### 4. Umístění screenshotů

Zkopírujte screenshoty do:
```
LogLantern/docs/screenshots/
├── splash.png
├── login.png
├── dashboard.png
├── table.png
├── piechart.png
├── settings.png
└── navigation.png
```

**Příkaz:**
```bash
# Z adresáře kde máte screenshoty
cp splash.png /Users/trenz/AndroidStudioProjects/LogLantern/docs/screenshots/
cp login.png /Users/trenz/AndroidStudioProjects/LogLantern/docs/screenshots/
cp dashboard.png /Users/trenz/AndroidStudioProjects/LogLantern/docs/screenshots/
cp table.png /Users/trenz/AndroidStudioProjects/LogLantern/docs/screenshots/
cp piechart.png /Users/trenz/AndroidStudioProjects/LogLantern/docs/screenshots/
cp settings.png /Users/trenz/AndroidStudioProjects/LogLantern/docs/screenshots/
cp navigation.png /Users/trenz/AndroidStudioProjects/LogLantern/docs/screenshots/
```

---

### 5. Commit a Push

```bash
git add docs/screenshots/
git commit -m "Add application screenshots"
git push origin main
```

---

### 6. Ověření na GitHubu

Po push zkontrolujte, že screenshoty jsou viditelné v README na GitHubu.

---

## 📝 Tips pro lepší screenshoty

1. **Vyčistěte data před screenshotem**
   - Použijte demo/test data
   - Vyplňte všechna pole správně

2. **Showcase funkcionalitu**
   - Table: Ukažte data v tabulce
   - Pie Chart: Ukažte barevný graf se statistikami
   - Settings: Zobrazení všech informací

3. **Konzistence**
   - Stejné rozlišení všech screenshotů
   - Stejné téma (light/dark)
   - Stejné zařízení/emulator

4. **Bottom Navigation**
   - Udělejte screenshot kde je bottom bar vidět
   - Ukazuje profesionalitu aplikace

---

## 🎨 Alternativa: Device Frame

Android Studio umí přidat rámečky zařízení automaticky:

1. Otevřete screenshot v Android Studio
2. Right-click → "Show in Explorer/Finder"
3. Nebo použijte Device Art Generator online

---

**Po přidání screenshotů odstraňte poznámku z README:**
```markdown
> 📝 **Note:** Screenshots will be added after the application is built and tested.
```
