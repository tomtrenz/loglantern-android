# 📐 LogLantern - Aplikační Struktura a Flow

## 🎯 Aplikační Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      SPLASH SCREEN                          │
│                    (Token Check)                            │
└──────────────┬──────────────────────────┬──────────────────┘
               │                          │
         TokenExists                Token Missing
               │                          │
               ▼                          ▼
┌──────────────────────────┐    ┌──────────────────────────┐
│      DASHBOARD           │    │     LOGIN SCREEN         │
│   🏠 📋 📊 ⚙️           │◄───│  • Base URL              │
└──────────────────────────┘    │  • Username              │
         │                      │  • Password              │
         │                      │  [Create Token]          │
         │                      └──────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────────────────┐
│              BOTTOM NAVIGATION BAR                           │
│  ┌──────┬──────┬──────┬──────┐                              │
│  │  🏠  │  📋  │  📊  │  ⚙️  │                              │
│  │ Domů │Tabl. │ Graf │Nast. │                              │
│  └──┬───┴───┬──┴───┬──┴───┬──┘                              │
│     │       │      │      │                                  │
│     ▼       ▼      ▼      ▼                                  │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐                                │
│  │Dash│ │Tabl│ │Pie │ │Sett│                                │
│  │brd │ │e   │ │Chrt│ │ings│                                │
│  └────┘ └────┘ └────┘ └──┬─┘                                │
│                           │                                   │
│                      [Logout]                                │
│                           │                                   │
│                           ▼                                   │
│                    LOGIN SCREEN                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Architektura MVVM

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Jetpack Compose Screens                             │   │
│  │  • SplashScreen                                       │   │
│  │  • LoginScreen                                        │   │
│  │  • DashboardScreen                                    │   │
│  │  • ResultsTableScreen                                 │   │
│  │  • ResultsPieChartScreen                              │   │
│  │  • SettingsScreen                                     │   │
│  └────────────────┬─────────────────────────────────────┘   │
│                   │ collectAsStateWithLifecycle              │
│                   ▼                                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ViewModels + UiState                                │   │
│  │  • SplashViewModel                                    │   │
│  │  • SplunkAuthViewModel                                │   │
│  │  • DashboardViewModel                                 │   │
│  │  • ResultsTableViewModel                              │   │
│  │  • ResultsPieChartViewModel                           │   │
│  │  • SettingsViewModel                                  │   │
│  └────────────────┬─────────────────────────────────────┘   │
└───────────────────┼──────────────────────────────────────────┘
                    │
                    │ Repository Pattern
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Use Cases                                            │   │
│  │  • ExecuteSearchUseCase                               │   │
│  └────────────────┬─────────────────────────────────────┘   │
│                   │                                          │
│  ┌────────────────▼─────────────────────────────────────┐   │
│  │  Repository Interfaces                                │   │
│  │  • AuthRepository                                     │   │
│  │  • SearchRepository                                   │   │
│  └────────────────┬─────────────────────────────────────┘   │
└───────────────────┼──────────────────────────────────────────┘
                    │
                    │ Implementation
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                        DATA LAYER                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Repository Implementations                           │   │
│  │  • AuthRepositoryImpl                                 │   │
│  │  • SearchRepositoryImpl                               │   │
│  └────┬─────────────────────────────────────────────┬───┘   │
│       │                                              │       │
│       ▼                                              ▼       │
│  ┌─────────┐                                   ┌─────────┐  │
│  │ Network │                                   │  Local  │  │
│  │   API   │                                   │ Storage │  │
│  │ ┌─────┐ │                                   │ ┌─────┐ │  │
│  │ │Retro│ │                                   │ │Data │ │  │
│  │ │ fit │ │                                   │ │Store│ │  │
│  │ └─────┘ │                                   │ └─────┘ │  │
│  │ ┌─────┐ │                                   │ ┌─────┐ │  │
│  │ │OkHtp│ │                                   │ │Encry│ │  │
│  │ │     │ │                                   │ │pted │ │  │
│  │ └─────┘ │                                   │ └─────┘ │  │
│  └─────────┘                                   └─────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔐 Bezpečnostní Flow

```
┌─────────────────────────────────────────────────────────────┐
│  LOGIN                                                       │
│                                                              │
│  User Input:                                                 │
│  ┌──────────────────────────────────────┐                   │
│  │ Base URL: https://splunk:8089        │                   │
│  │ Username: admin                       │                   │
│  │ Password: ••••••••                    │                   │
│  └──────────────────────────────────────┘                   │
│              │                                               │
│              ▼                                               │
│  ┌──────────────────────────────────────┐                   │
│  │ POST /services/authorization/tokens  │                   │
│  │ Authorization: Basic base64(user:pwd)│                   │
│  └──────────────────────────────────────┘                   │
│              │                                               │
│              ▼                                               │
│  ┌──────────────────────────────────────┐                   │
│  │ Response: JWT Token                  │                   │
│  │ {                                     │                   │
│  │   "token": "eyJraWQ...",             │                   │
│  │   "expiry": "2026-03-01"             │                   │
│  │ }                                     │                   │
│  └──────────────────────────────────────┘                   │
│              │                                               │
│              ▼                                               │
│  ┌──────────────────────────────────────┐                   │
│  │ ✅ Store in EncryptedDataStore       │                   │
│  │ ✅ Store Base URL                     │                   │
│  │ ✅ Store Username                     │                   │
│  │ ✅ Store Token Expiry                 │                   │
│  │ ❌ NEVER store password               │                   │
│  └──────────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Search Jobs Flow

```
┌─────────────────────────────────────────────────────────────┐
│  RESULTS TABLE / PIE CHART SCREEN                           │
│                                                              │
│  1. User Input SPL Query                                     │
│  ┌──────────────────────────────────────┐                   │
│  │ search index=main | stats count      │                   │
│  │                    by src_ip          │                   │
│  └──────────────────────────────────────┘                   │
│              │                                               │
│              ▼                                               │
│  2. Create Search Job                                        │
│  ┌──────────────────────────────────────┐                   │
│  │ POST /services/search/jobs           │                   │
│  │ Response: { "sid": "1234.567" }      │                   │
│  └──────────────────────────────────────┘                   │
│              │                                               │
│              ▼                                               │
│  3. Poll Job Status (every 1s)                              │
│  ┌──────────────────────────────────────┐                   │
│  │ GET /services/search/jobs/{sid}      │                   │
│  │ while (!isDone) { ... }              │                   │
│  └──────────────────────────────────────┘                   │
│              │                                               │
│              ▼                                               │
│  4. Get Results                                              │
│  ┌──────────────────────────────────────┐                   │
│  │ GET /services/search/jobs/{sid}/     │                   │
│  │     results                           │                   │
│  │ Response: JSON data                   │                   │
│  └──────────────────────────────────────┘                   │
│              │                                               │
│              ▼                                               │
│  5. Display                                                  │
│  ┌──────────────────────────────────────┐                   │
│  │ 📋 Table View                         │                   │
│  │    OR                                 │                   │
│  │ 📊 Pie Chart                          │                   │
│  └──────────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎨 UI Component Hierarchy

```
MainActivity
  └── LogLanternApp
       └── LogLanternTheme
            └── MainScaffold
                 ├── NavigationBar (Bottom)
                 │    ├── Dashboard Item 🏠
                 │    ├── Table Item 📋
                 │    ├── PieChart Item 📊
                 │    └── Settings Item ⚙️
                 │
                 └── NavHost
                      ├── SplashScreen
                      ├── LoginScreen
                      ├── DashboardScreen
                      ├── ResultsTableScreen
                      ├── ResultsPieChartScreen
                      └── SettingsScreen
```

---

## 📦 Package Structure

```
cz.splnsito.mrthom.loglantern
├── 📱 app
│   ├── LogLanternApplication.kt (@HiltAndroidApp)
│   ├── LogLanternApp.kt (Compose entry)
│   └── di
│       ├── AppModule.kt
│       ├── NetworkModule.kt
│       └── StorageModule.kt
├── 🎯 core
│   ├── network
│   │   ├── SplunkApiService.kt
│   │   ├── ApiServiceFactory.kt
│   │   └── auth
│   │       └── SplunkAuthInterceptor.kt
│   ├── security
│   │   ├── TokenStorage.kt
│   │   └── EncryptedTokenStorage.kt
│   └── ui
│       ├── theme (Material3)
│       └── components (PrimaryButton, etc.)
├── 💾 data
│   ├── local
│   │   └── SettingsDataStore.kt
│   └── repository
│       ├── AuthRepositoryImpl.kt
│       └── SearchRepositoryImpl.kt
├── 🎯 domain
│   ├── model
│   ├── repository (interfaces)
│   │   ├── AuthRepository.kt
│   │   └── SearchRepository.kt
│   └── usecase
│       └── ExecuteSearchUseCase.kt
├── 🎨 feature
│   ├── splash
│   │   ├── SplashScreen.kt
│   │   └── SplashViewModel.kt
│   ├── auth
│   │   ├── SplunkLoginScreen.kt
│   │   └── SplunkAuthViewModel.kt
│   ├── dashboard
│   │   ├── DashboardScreen.kt
│   │   └── DashboardViewModel.kt
│   ├── settings
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   └── results
│       ├── table
│       │   ├── ResultsTableScreen.kt
│       │   └── ResultsTableViewModel.kt
│       └── chart
│           ├── ResultsPieChartScreen.kt
│           └── ResultsPieChartViewModel.kt
└── 🧭 navigation
    ├── LogLanternRoutes.kt
    ├── LogLanternNavGraph.kt
    ├── BottomNavItem.kt
    └── MainScaffold.kt
```

---

**Vytvořeno:** 2. února 2026  
**Projekt:** LogLantern Android Application
