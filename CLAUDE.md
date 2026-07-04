# CounterTap — Claude Context File

This file helps Claude quickly understand the project state in any new session.
Read this + PLAN.md before making any changes.

## What This Project Is

Two Android apps for small/medium shops (tea stalls, restaurants):
- **counter-tap-business** (`com.countertap.business`) — shop owner app: manage menu, view orders, generate QR, set UPI ID
- **counter-tap** (`com.countertap.customer`) — customer app: scan QR, browse menu, place orders, pay via UPI

Firebase (Firestore) backend, Google Sign-In auth, UPI deep link payments.

## Repo

GitHub: https://github.com/debabrata-mandal/CounterTap (private)
Local: C:\Github\default\CounterTap

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM — ViewModel + StateFlow
- **DI**: Hilt (KAPT)
- **Navigation**: Compose Navigation
- **Backend**: Firebase (Firestore, Auth, Storage, FCM)
- **Auth**: Google Sign-In via Firebase Auth + Credential Manager
- **Payments**: UPI deep link (free, opens GPay/PhonePe)
- **QR generate**: ZXing (business app)
- **QR scan**: ML Kit + CameraX (customer app)
- **Image loading**: Coil
- **Min SDK**: 24 (Android 7.0)
- **Compile SDK**: 34

## Firebase Project

- Project: `countertap-dev` (Blaze plan)
- Region: asia-south1 (Firestore), us-east1 (Storage)
- Auth: Google Sign-In enabled
- Firestore: created, test mode
- Storage: created, test mode
- Package names registered: com.countertap.business, com.countertap.customer
- google-services.json: placed locally (gitignored, injected via CI secrets)

## Firestore Data Model

```
tenants/{tenantId}/
  products/{productId}     — Product data
  orders/{orderId}         — Order data
  categories/{categoryId}  — Menu categories
users/{userId}             — User profile
```

Shared data models are in `shared/src/main/java/com/countertap/shared/Models.kt`

## Current Phase

**Phase 1 — Scaffolding (IN PROGRESS)**

Setup steps completed:
- [x] GitHub repo created and pushed
- [x] Firebase project countertap-dev created (Blaze)
- [x] Both Android apps registered in Firebase
- [x] Google Sign-In enabled in Firebase Auth
- [x] Firestore database created (asia-south1, test mode)
- [x] Firebase Storage created (test mode)
- [x] Android Studio installed, project opened
- [x] Both app modules scaffolded (build.gradle.kts, AndroidManifest, MainActivity, App class)
- [x] Shared module scaffolded with data models
- [x] Gradle wrapper created (gradle-wrapper.properties → Gradle 8.11.1)
- [x] Gradle sync passing cleanly
- [x] SHA-1 fingerprint added to Firebase (A1:AF:E7:FD:AD:05:C8:BF:09:3E:FC:BF:5E:E6:11:86:38:84:E8:8B)
- [x] google-services.json correct for both apps (both contain SHA-1 for both packages)
- [ ] GitHub Secrets set up (GOOGLE_SERVICES_JSON_BUSINESS, GOOGLE_SERVICES_JSON_CUSTOMER, KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)

## Phase 1 Coding — Completed
- [x] Google Sign-In auth screen — business app (`ui/auth/SignInScreen.kt`)
- [x] Google Sign-In auth screen — customer app (`ui/auth/SignInScreen.kt`)
- [x] Hilt modules — `di/AppModule.kt` in both apps (FirebaseAuth, Firestore, Storage)
- [x] Navigation graphs — `navigation/NavGraph.kt` in both apps (sign_in → home)
- [x] Base ViewModels — `viewmodel/AuthViewModel.kt` in both apps

## Phase 2 — Next Steps
- [ ] Shop onboarding screen (business app) — shop name, address, phone, logo upload
- [ ] UPI setup screen (business app)
- [ ] After sign-in: check Firestore for existing tenant doc; route to onboarding or home
- [ ] Add/edit/delete products and categories (business app)
- [ ] QR code generation screen (business app)

## Key Decisions Made

1. **Google Sign-In via Firebase Auth** (not direct Google) — needed for Firestore security rules (request.auth.uid)
2. **UPI deep link** for payments in v1 (free, 0% fees) — Razorpay in v2 for auto-confirmation
3. **Single Firebase project** (countertap-dev) for dev — add countertap-prod at launch
4. **Monorepo** — both apps + shared module in one repo
5. **KAPT** for Hilt (can migrate to KSP later)
6. **OrderStatus** as String constants (not enum) for Firestore compatibility

## CI/CD

- `.github/workflows/pr-checks.yml` — lint + unit tests on every PR
- `.github/workflows/release.yml` — signed APKs on git tag v*.*.*
- Secrets needed: GOOGLE_SERVICES_JSON_BUSINESS, GOOGLE_SERVICES_JSON_CUSTOMER, KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD

## How to Resume in a New Session

1. Read this file + PLAN.md
2. Check current state of the code in the repo
3. Continue from "Next steps" above
