# CounterTap — Full Project Plan

## Overview

CounterTap is a two-app ordering system for small and medium shops (restaurants, tea stalls, bakeries, etc.).

- **counter-tap-business** — installed by shop owners to manage their shop, products, and incoming orders
- **counter-tap** — installed by customers to scan a shop QR, browse menu, place orders, and pay via UPI

---

## Repository Structure

```
CounterTap/                          ← GitHub private monorepo
├── counter-tap-business/            ← Owner Android app (Kotlin + Compose)
│   ├── app/
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/countertap/business/
│   │       │   │   ├── ui/          ← Compose screens
│   │       │   │   ├── viewmodel/   ← ViewModels
│   │       │   │   ├── repository/  ← Firestore data layer
│   │       │   │   ├── model/       ← Data classes
│   │       │   │   └── di/          ← Hilt dependency injection
│   │       │   └── res/
│   │       └── test/
│   ├── build.gradle.kts
│   └── google-services.json         ← Firebase config (gitignored, injected via CI)
│
├── counter-tap/                     ← Customer Android app (Kotlin + Compose)
│   ├── app/
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/countertap/customer/
│   │       │   │   ├── ui/
│   │       │   │   ├── viewmodel/
│   │       │   │   ├── repository/
│   │       │   │   ├── model/
│   │       │   │   └── di/
│   │       │   └── res/
│   │       └── test/
│   └── build.gradle.kts
│
├── shared/                          ← Shared Kotlin module (models, constants)
│   └── src/main/java/com/countertap/shared/
│       ├── model/                   ← Firestore data classes used by both apps
│       └── util/                    ← Common utilities
│
├── backend/
│   └── functions/                   ← Firebase Cloud Functions (Node.js / TypeScript)
│       ├── src/
│       │   ├── onOrderCreated.ts    ← Trigger: notify owner on new order
│       │   └── onOrderUpdated.ts    ← Trigger: notify customer on status change
│       └── package.json
│
├── .github/
│   └── workflows/
│       ├── pr-checks.yml            ← Lint + test on every PR
│       └── release.yml              ← Build + sign + publish on git tag
│
├── PLAN.md                          ← This file
└── README.md
```

---

## Tech Stack

| Layer | Choice | Reason |
|---|---|---|
| Language | Kotlin | Native Android, modern, null-safe |
| UI | Jetpack Compose | Google's current standard, no XML |
| State management | ViewModel + StateFlow | Official Android architecture |
| Dependency injection | Hilt | Google-recommended DI for Android |
| Navigation | Compose Navigation | Type-safe screen routing |
| Database | Firebase Firestore | Real-time listeners, no polling needed |
| Auth | Firebase Auth — Google Sign-In | Free, unlimited, no SMS cost |
| File storage | Firebase Storage | Product images, shop logos |
| Push notifications | Firebase Cloud Messaging (FCM) | Order alerts to owner |
| Background tasks | Firebase Cloud Functions | Server-side triggers |
| QR generation | ZXing (owner app) | Generate shop QR |
| QR scanning | ML Kit Barcode Scanning | Scan QR in customer app |
| UPI payments | Android Intent / UPI deep link | Free, zero gateway fees |
| Image loading | Coil | Lightweight, Compose-friendly |
| CI/CD | GitHub Actions | Free for private repos |
| Signing | Keystore via GitHub Secrets | Secure, automated |
| Release | GitHub Releases | APK download per version |
| Play Store | Fastlane (optional, Phase 5) | Automated store publishing |

---

## Firestore Data Model

```
tenants/{tenantId}
  name:         string        ← "Raju's Tea Stall"
  ownerId:      string        ← Firebase Auth UID
  upiId:        string        ← "raju@paytm"
  logoUrl:      string        ← Firebase Storage URL
  phone:        string
  address:      string
  active:       boolean
  createdAt:    timestamp

tenants/{tenantId}/categories/{categoryId}
  name:         string        ← "Beverages", "Snacks"
  sortOrder:    number

tenants/{tenantId}/products/{productId}
  name:         string        ← "Masala Chai"
  description:  string
  price:        number        ← in rupees (e.g. 30)
  categoryId:   string
  imageUrl:     string
  available:    boolean
  sortOrder:    number

tenants/{tenantId}/orders/{orderId}
  customerId:       string    ← Firebase Auth UID
  customerName:     string
  customerPhone:    string
  items: [
    { productId, name, price, quantity }
  ]
  total:            number
  status:           "pending" | "confirmed" | "preparing" | "ready" | "completed" | "cancelled"
  paymentStatus:    "unpaid" | "paid"
  paymentMethod:    "upi" | "cash"
  upiTransactionId: string    ← filled by customer after payment (manual v1)
  note:             string    ← special instructions
  createdAt:        timestamp
  updatedAt:        timestamp

users/{userId}
  name:     string
  phone:    string
  email:    string
  role:     "owner" | "customer"
  tenantId: string            ← only for owners
```

---

## counter-tap-business — Screen Plan

### Auth Flow
- **Splash screen** — check login state, redirect
- **Sign in screen** — "Sign in with Google" button (one tap)

### Onboarding (first login only)
- **Shop setup screen** — shop name, address, phone, upload logo
- **UPI setup screen** — enter UPI ID, shown to customers at checkout

### Main App (bottom nav: Orders / Menu / Settings)

**Orders tab**
- **Live orders screen** — real-time list of incoming orders, grouped by status (pending, preparing, ready)
  - Tap order → Order detail sheet → change status buttons
  - Badge count on tab icon for pending orders
- **Order history screen** — completed/cancelled orders, filterable by date

**Menu tab**
- **Product list screen** — all products grouped by category, toggle availability inline
- **Add/edit product screen** — name, price, description, category, image upload, availability toggle
- **Category management screen** — add/reorder/delete categories

**Settings tab**
- **Shop profile screen** — edit name, address, logo
- **UPI settings screen** — change UPI ID
- **QR code screen** — shows shop QR (encodes tenantId), download/share button
- **Account screen** — logout

---

## counter-tap — Screen Plan

### Auth Flow
- **Splash screen** — check login state
- **Sign in screen** — "Sign in with Google" button (one tap)
  - Name and email pulled from Google account automatically
  - No manual entry needed

### Main Flow

- **Home / Scanner screen** — camera view to scan shop QR code
  - OR enter shop code manually
  - Recent shops list (locally cached)

- **Shop menu screen** — after scanning QR, shows:
  - Shop name + logo header
  - Products grouped by category
  - Search bar
  - Each product: name, price, description, image, "Add" button
  - Floating cart button with item count + total

- **Cart screen**
  - List of selected items with quantity +/- controls
  - Special instructions text field
  - Order total
  - Payment method selector (UPI / Cash)
  - "Place Order" button

- **Payment screen** (if UPI selected)
  - Shows amount + owner UPI ID
  - "Pay via UPI" button → launches UPI deep link (opens GPay/PhonePe/Paytm)
  - After returning: "Enter transaction ID" field (manual confirmation, v1)
  - "I've paid" button → marks paymentStatus = paid, places order

- **Order tracking screen**
  - Real-time order status with visual stepper
  - Status: Order Placed → Confirmed → Preparing → Ready → Done
  - Shop phone number to call if needed

- **Order history screen**
  - Past orders, re-order button

---

## CI/CD Pipeline

### pr-checks.yml — triggers on every pull request

```
Steps:
1. Checkout code
2. Set up JDK 17
3. Set up Android SDK
4. Inject google-services.json from GitHub Secret
5. Run: ./gradlew :counter-tap-business:app:lint
6. Run: ./gradlew :counter-tap-business:app:testDebugUnitTest
7. Run: ./gradlew :counter-tap:app:lint
8. Run: ./gradlew :counter-tap:app:testDebugUnitTest
9. Upload test reports as artifact
10. Block merge if any step fails
```

### release.yml — triggers on git tag push (e.g. v1.0.0)

```
Steps:
1. Checkout code
2. Set up JDK 17
3. Inject google-services.json from GitHub Secret
4. Inject keystore from GitHub Secret
5. Run all tests (same as PR checks)
6. Build release APK: ./gradlew :counter-tap-business:app:assembleRelease
7. Build release APK: ./gradlew :counter-tap:app:assembleRelease
8. Sign both APKs with keystore
9. Create GitHub Release with tag name
10. Upload both signed APKs to the release
11. (Phase 5) Fastlane: upload to Google Play internal track
```

### GitHub Secrets required

| Secret name | What it holds |
|---|---|
| `GOOGLE_SERVICES_JSON` | Firebase config file content |
| `KEYSTORE_FILE` | Base64-encoded keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `PLAY_STORE_JSON_KEY` | Google Play API service account (Phase 5) |

---

## Testing Strategy

**Unit tests** (run on every PR)
- ViewModel logic (order state transitions, cart calculations)
- Repository layer (Firestore data mapping)
- Utility functions (UPI deep link builder, QR code encoder)

**Widget / UI tests** (run on every PR)
- Individual Compose screens with mocked ViewModels
- Cart total calculation display
- Order status stepper rendering

**Integration tests** (run on release only — needs emulator)
- Full add-to-cart → place order flow against Firestore emulator
- Google Sign-In flow with Firebase Auth emulator

---

## Build Phases

### Phase 1 — Foundation (Week 1–2)
- Set up GitHub private monorepo
- Create both Android projects with Kotlin + Compose
- Configure Firebase project (dev + prod environments)
- Set up Hilt DI, Navigation, base architecture
- Implement Google Sign-In auth in both apps
- Shared data models in `/shared` module
- GitHub Actions PR check workflow

### Phase 2 — Business App Core (Week 3–4)
- Shop onboarding flow
- Add/edit/delete products and categories
- QR code generation screen
- UPI ID setup

### Phase 3 — Customer App Core (Week 5–6)
- QR code scanner
- Menu browsing screen
- Cart functionality
- Order placement

### Phase 4 — Real-time Orders (Week 7)
- Live order list in business app (Firestore listeners)
- Order status update buttons (owner side)
- Order tracking screen in customer app (real-time)
- FCM push notifications (owner gets alert on new order)

### Phase 5 — Payments (Week 8)
- UPI deep link integration in customer app
- Manual transaction ID confirmation (v1)
- Payment status reflected in order

### Phase 6 — CI/CD + Release (Week 9)
- Release GitHub Actions workflow
- Keystore setup + signing
- GitHub Releases with APK uploads
- Firebase App Distribution for beta testers

### Phase 7 — Polish + Play Store (Week 10+)
- App icons, splash screens, onboarding illustrations
- Error handling, empty states, loading skeletons
- Fastlane Google Play publishing
- Store listing: screenshots, description, category

---

## Key Libraries (build.gradle.kts)

```kotlin
// Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose")
implementation("androidx.navigation:navigation-compose")

// Firebase
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-storage-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")

// Architecture
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
implementation("com.google.dagger:hilt-android")
kapt("com.google.dagger:hilt-compiler")

// QR (business app)
implementation("com.google.zxing:core")

// QR scan (customer app)
implementation("com.google.mlkit:barcode-scanning")
implementation("androidx.camera:camera-camera2")
implementation("androidx.camera:camera-lifecycle")
implementation("androidx.camera:camera-view")

// Image loading
implementation("io.coil-kt:coil-compose")

// Testing
testImplementation("junit:junit")
testImplementation("io.mockk:mockk")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

---

## UPI Payment — How It Works (v1)

No payment gateway SDK needed. Uses Android Intent to open any UPI app installed on the customer's phone.

```
1. Owner sets UPI ID in business app settings (e.g. "raju@paytm")
   → stored in Firestore: tenants/{id}.upiId

2. Customer places order → goes to payment screen
   → app builds UPI deep link:
      upi://pay?pa=raju@paytm&pn=Raju's Tea Stall&am=75&cu=INR&tn=CounterTap Order #CT-001

3. Android Intent fires → customer's phone shows UPI app chooser
   → customer pays in GPay / PhonePe / Paytm / any UPI app

4. Customer returns to app → manually enters transaction ID
   → taps "I've paid"
   → order.paymentStatus = "paid", order placed

5. Owner sees order with paymentStatus in business app
   → confirms and processes
```

v2 (future): Razorpay SDK gives automatic payment confirmation callback, removes manual step.

---

## Firebase Project Setup

Two Firebase environments recommended:

- `countertap-dev` — for development and testing
- `countertap-prod` — for production releases

Each has its own `google-services.json`. Dev config used in debug builds, prod config injected by CI for release builds.

---

## What's NOT in Scope (v1)

- iOS app
- Automatic UPI payment verification (v2 with Razorpay)
- Multi-table / table number support
- Inventory management
- Analytics dashboard
- Staff accounts (only one owner per shop)
- Multiple shops per owner
