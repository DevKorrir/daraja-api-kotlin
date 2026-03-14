# Epesa 💸

An Android application for initiating M-Pesa STK Push payments using the Safaricom Daraja API. Built with clean MVVM architecture, Hilt dependency injection, Jetpack Compose UI, and Kotlin Coroutines.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Daraja API Setup](#daraja-api-setup)
  - [Configure Credentials](#configure-credentials)
  - [Build and Run](#build-and-run)
- [How the Payment Flow Works](#how-the-payment-flow-works)
- [Sandbox Testing](#sandbox-testing)
- [Security](#security)
- [Contributing](#contributing)

---

## Overview

Epesa integrates with the [Safaricom Daraja API](https://developer.safaricom.co.ke) to trigger M-Pesa STK Push requests directly from an Android app. The user enters a phone number and amount, the app requests an OAuth token from Safaricom, constructs a signed STK Push request, and sends it to the Daraja API. Safaricom then pushes a payment prompt to the user's phone.

**This project is currently in sandbox (test) mode.** No real money is processed.

---

## Architecture

Epesa is built on **MVVM (Model-View-ViewModel)** with a clean layered structure separating concerns into three layers:

```
presentation  →  domain  ←  data
```

- **`presentation`** — Jetpack Compose UI, ViewModel, UiState. Knows only the domain layer.
- **`domain`** — Pure Kotlin. Repository interface and domain models. No Android or network imports.
- **`data`** — Retrofit API service, DTOs, and the concrete repository implementation. Knows the domain interface and implements it.

This means the ViewModel never touches Retrofit or OkHttp directly. It calls the `MpesaRepository` interface. Hilt injects the concrete `MpesaRepositoryImpl` at runtime. The UI layer has zero knowledge of the network layer.

---

## Tech Stack

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.x | Primary language |
| Jetpack Compose | BOM | Declarative UI |
| Hilt | 2.x | Dependency injection |
| KSP | Latest | Annotation processing (replaces kapt) |
| Retrofit | 2.x | HTTP client abstraction |
| OkHttp | 4.x | HTTP engine + logging interceptor |
| Gson | 2.x | JSON serialisation / deserialisation |
| Kotlin Coroutines | 1.x | Asynchronous execution |
| StateFlow | — | Reactive UI state (replaces LiveData) |
| Timber | 5.x | Debug logging (no-op in release) |

---

## Project Structure

```
app/src/main/java/dev/korryr/epesa/
│
├── HiltApp.kt                                  # Application class — Hilt init + Timber setup
├── MainActivity.kt                             # Single Activity — Compose host
│
├── core/
│   └── di/
│       ├── NetworkModule.kt                    # Provides OkHttpClient and Retrofit
│       └── MpesaModule.kt                      # Provides MpesaApiService and MpesaRepository
│
└── feature/
    └── payment/
        ├── data/
        │   ├── remote/
        │   │   ├── MpesaApiService.kt          # Retrofit interface — HTTP endpoint declarations
        │   │   └── dto/
        │   │       ├── AccessTokenResponse.kt  # OAuth token response DTO
        │   │       ├── StkPushRequest.kt       # STK Push request body DTO
        │   │       └── StkPushResponse.kt      # STK Push response DTO
        │   └── repository/
        │       └── MpesaRepositoryImpl.kt      # Concrete implementation — network + mapping
        │
        ├── domain/
        │   ├── model/
        │   │   └── PaymentResult.kt            # Clean domain model (no API or Android deps)
        │   └── repository/
        │       └── MpesaRepository.kt          # Repository contract (interface)
        │
        └── presentation/
            ├── PaymentUiState.kt               # Sealed interface — all possible screen states
            ├── PaymentViewModel.kt             # State manager — validation + coroutine launch
            ├── PaymentScreen.kt                # Root composable screen
            └── components/
                ├── StkPushDialog.kt            # Payment input dialog (phone + amount)
                └── PaymentResultDialog.kt      # Payment result dialog (success or error)
```

---

## Getting Started

### Prerequisites

- Android Studio Panda2 or later
- JDK 11
- Android device or emulator running API 24+
- A Safaricom Daraja developer account — [register here](https://developer.safaricom.co.ke)

### Daraja API Setup

1. Log in to the [Safaricom Developer Portal](https://developer.safaricom.co.ke)
2. Create a new app or open an existing one
3. Under **Keys & Secrets**, copy your **Consumer Key** and **Consumer Secret**
4. Under **Test Credentials → Lipa Na Mpesa Online**, copy the **Passkey**
5. The sandbox **Business Short Code** is `174379` by default

### Configure Credentials

This project uses a `keys.properties` file at the project root to keep credentials out of source code. This file is listed in `.gitignore` and is never committed.

1. Copy the example file:

```bash
cp keys.properties.example keys.properties
```

2. Open `keys.properties` and fill in your values:

```properties
mpesa.consumerKey=YOUR_CONSUMER_KEY
mpesa.consumerSecret=YOUR_CONSUMER_SECRET
mpesa.passkey=YOUR_PASSKEY
mpesa.businessShortCode=174379
mpesa.callbackUrl=https://yourdomain.com/mpesa/callback
```

> **`callbackUrl`** must be a publicly reachable HTTPS URL. Safaricom's servers POST the final payment result to this URL after the user completes or cancels the STK prompt. This is a server-side concern — the Android app does not receive the callback directly.

3. Sync Gradle — the values are injected into `BuildConfig` at compile time via `buildConfigField` in `app/build.gradle.kts`.

### Build and Run

```bash
./gradlew assembleDebug
```

Or run directly from Android Studio with **Run → Run 'app'**.

---

## How the Payment Flow Works

The payment involves two separate HTTP calls in sequence:

**Step 1 — OAuth token request (app → Safaricom)**

```
GET https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials
Authorization: Basic Base64(consumerKey:consumerSecret)
```

Returns a short-lived Bearer token used to authenticate the STK Push request.

**Step 2 — STK Push request (app → Safaricom)**

```
POST https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest
Authorization: Bearer {access_token}
Body: { BusinessShortCode, Password, Timestamp, Amount, PhoneNumber, ... }
```

The `Password` field is derived as:

```
Password = Base64( BusinessShortCode + Passkey + Timestamp )
```

Where `Timestamp` is the current time in `yyyyMMddHHmmss` format.

A `ResponseCode: "0"` means Safaricom accepted the request and has pushed a payment prompt to the phone. It does **not** mean the user has paid yet.

**Step 3 — Payment callback (Safaricom → your server)**

After the user enters their M-Pesa PIN or cancels the prompt, Safaricom sends a POST request to your `callbackUrl` with the final result. This is handled by your backend, not the Android app.

**UiState flow:**

```
Idle → Loading → Success("Check your phone") or Error("message")
                        ↓
                    resetState()
                        ↓
                      Idle
```

---

## Sandbox Testing

Use these official Safaricom sandbox test values:

> **Note:** The Safaricom sandbox can be slow or intermittently unavailable. If you receive timeout errors, wait a few minutes and retry.

---

## Security

- **`keys.properties` is never committed.** It is listed in `.gitignore`. Your Consumer Key, Consumer Secret, and Passkey stay on your machine only.
- **`keys.properties.example` is committed.** It contains no real values and documents the required credential schema for contributors.
- **Credentials are injected at compile time** via `BuildConfig`. They are baked into the APK binary and never exist in source code or version control.
- **Timber logs nothing in release builds.** `Timber.plant(Timber.DebugTree())` is called only when `BuildConfig.DEBUG` is `true`. In release builds, all `Timber.*` calls are no-ops.
- **Basic Auth is computed once** using a `lazy` delegate in `MpesaRepositoryImpl`. The Base64-encoded credential string is built on first use and cached — it is never recomputed on subsequent calls.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Copy `keys.properties.example` to `keys.properties` and add your sandbox credentials
4. Make your changes
5. Commit: `git commit -m "feat: describe your change"`
6. Push: `git push origin feature/your-feature-name`
7. Open a Pull Request

Please follow the existing architecture conventions — new features go inside `feature/` with the same `data / domain / presentation` layering.

---

## License

```
MIT License

Copyright (c) 2025 DevKorrir

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so.
