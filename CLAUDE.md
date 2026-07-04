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
  products/{productId}     — Product data (available:bool, categoryId:string)
  orders/{orderId}         — Order data (status, paymentStatus, note, items[])
  categories/{categoryId}  — Menu categories (name, order:int)
users/{userId}             — User profile
```

Shared data models are in `shared/src/main/java/com/countertap/shared/Models.kt`

## Current Phase

**Phase 4 — UI Redesign (COMPLETE)**

## Completed Work

### Phase 1 — Scaffolding ✅
- [x] GitHub repo, Firebase project, both apps registered
- [x] Google Sign-In, Firestore, Storage configured
- [x] Gradle wrapper (8.11.1), sync passing
- [x] SHA-1 fingerprint added to Firebase
- [x] google-services.json in place (gitignored)
- [ ] GitHub Secrets set up (GOOGLE_SERVICES_JSON_BUSINESS, GOOGLE_SERVICES_JSON_CUSTOMER, KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)

### Phase 1 Coding ✅
- [x] Google Sign-In auth screen — both apps (`ui/auth/SignInScreen.kt`)
- [x] Hilt modules — `di/AppModule.kt` in both apps
- [x] Navigation graphs — both apps
- [x] AuthViewModel — both apps

### Phase 2 — Business App ✅
- [x] Shop onboarding screen (`ui/onboarding/ShopSetupScreen.kt`)
- [x] UPI setup screen (`ui/onboarding/UpiSetupScreen.kt`)
- [x] Loading screen — checks Firestore, routes to onboarding or home
- [x] Product list screen with category grouping (`ui/menu/ProductListScreen.kt`)
- [x] Add/edit product screen (`ui/menu/AddEditProductScreen.kt`)
- [x] Category management screen (`ui/menu/CategoryScreen.kt`)
- [x] QR code generation + share (`ui/qr/QrCodeScreen.kt`) — encodes tenantId
- [x] Home screen with Orders / Menu / QR tabs (bottom nav, no overlap)

### Phase 3 — Customer App ✅
- [x] QR scanner (`ui/scanner/ScannerScreen.kt`) — CameraX + ML Kit
- [x] Menu screen (`ui/menu/MenuScreen.kt`) — shop header, products grouped by category; uncategorized products shown as fallback
- [x] Cart screen (`ui/cart/CartScreen.kt`) — quantity controls, note, place order
- [x] Order confirmation screen (`ui/order/OrderConfirmationScreen.kt`)
- [x] CartViewModel shared between Menu and Cart via nav back-stack scoping
- [x] Full nav graph: SIGN_IN → SCANNER → MENU/{tenantId} → CART/{tenantId} → ORDER_CONFIRMED/{orderId}

## Phase 4 — Next Steps (priority order)

### UI Redesign ✅ (dark premium style)
- [x] Fix system ActionBar: `res/values/themes.xml` (NoActionBar) + `android:theme` in both manifests
- [x] Business app HomeScreen — Column pattern + animated tab nav (AccentBlue glow pill, ReceiptLong/List/QrCode)
- [x] Business app ProductListScreen — left accent bar + price badge chip; dim+strikethrough for unavailable
- [x] Customer app MenuScreen — vertical-rule category headers + accent-bar product cards with price chip
- [x] Customer app CartScreen — accent-bar cart items + raised summary footer (total + Place Order)
- [x] Customer app ScannerScreen — 4-corner L-marker viewfinder + dimmed overlay
- [x] Customer app OrderConfirmationScreen — concentric glow rings + order ID card

### Feature: Live Orders (business app) — NEXT
- [ ] Orders tab — real-time Firestore listener for incoming orders
- [ ] Order cards: show items, customer name, total; accept/reject actions
- [ ] Update order status in Firestore (PENDING → CONFIRMED → READY etc.)

### Phase 5
- [ ] UPI deep link payment (customer pays after order confirmed)

## Key Decisions Made

1. **Google Sign-In via Firebase Auth** (not direct Google) — needed for Firestore security rules (request.auth.uid)
2. **UPI deep link** for payments in v1 (free, 0% fees) — Razorpay in v2 for auto-confirmation
3. **Single Firebase project** (countertap-dev) for dev — add countertap-prod at launch
4. **Monorepo** — both apps + shared module in one repo
5. **KAPT** for Hilt (can migrate to KSP later)
6. **OrderStatus** as String constants (not enum) for Firestore compatibility
7. **guava:32.1.2-android** added to customer app — required for CameraX `ListenableFuture` compile access
8. **ML Kit barcode filter** — use `rawValue != null` not `TYPE_TEXT`; Firestore IDs are classified as TYPE_UNKNOWN
9. **Left accent bar pattern** — use `Row` + `height(IntrinsicSize.Min)` + `Box(Modifier.width(4.dp).fillMaxHeight())` for left border in card items; never use `fillMaxHeight()` inside a wrapping `Box` (gives 0 or unbounded height in lazy lists)

## CRITICAL: Bottom Nav / Inset Rule

**Never use nested Scaffold for tab screens.** The correct pattern for any screen with bottom navigation:

```kotlin
Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.weight(1f)) { /* tab content */ }
    NavigationBar(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        windowInsets = WindowInsets(0)
    )
}
```

Inner Scaffolds (e.g. ProductListScreen) must use `contentWindowInsets = WindowInsets(0)`.

For full-screen screens without bottom nav, add `statusBarsPadding()` to the top element so content doesn't hide behind the status bar.

## CI/CD

- `.github/workflows/pr-checks.yml` — lint + unit tests on every PR
- `.github/workflows/release.yml` — signed APKs on git tag v*.*.*
- Secrets needed: GOOGLE_SERVICES_JSON_BUSINESS, GOOGLE_SERVICES_JSON_CUSTOMER, KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD

## How to Resume in a New Session

1. Read this file
2. Check git log for latest commit
3. Continue from "Next Steps" in Phase 4 above
