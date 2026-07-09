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
- **Payments**: Cash-only (v1); Razorpay UPI-only planned for v2
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
  orders/{orderId}         — Order data (status, paymentStatus, note, items[], customerId)
  categories/{categoryId}  — Menu categories (name, order:int)
users/{userId}/
  fcmToken                 — FCM device token (saved by business app on startup)
  orders/{orderId}         — UserOrderSummary: tenantId, shopName, status, totalAmount, items, note, createdAt
```

Shared data models are in `shared/src/main/java/com/countertap/shared/Models.kt`

## Current Phase

**Phase 6 — Dashboards + Payments (IN PROGRESS — design improvements pending)**

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

### Feature: Live Orders (business app) ✅
- [x] Orders tab — real-time Firestore listener for incoming orders (`repository/OrderRepository.kt`)
- [x] Order cards: show items, customer name, total; accept/reject actions (`ui/orders/OrdersScreen.kt`)
- [x] Update order status in Firestore via batch write — syncs to `users/{customerId}/orders/` too

### Feature: Customer Order History ✅
- [x] `UserOrderSummary` saved to `users/{uid}/orders/` on order placement
- [x] `OrderHistoryScreen` — real-time status updates, dark premium cards (`ui/orders/OrderHistoryScreen.kt`)
- [x] "My Orders" icon button on customer home screen
- [x] Routes: `ORDER_HISTORY = "order_history"`

### Feature: FCM Push Notifications ✅
- [x] Business app `CounterTapMessagingService` — handles token refresh + incoming messages
- [x] Business app saves FCM token to `users/{uid}.fcmToken` on startup (`MainActivity.kt`)
- [x] `POST_NOTIFICATIONS` permission requested on Android 13+
- [x] Cloud Function `notifyNewOrder` in `functions/index.js` — triggers on order creation
- [x] Cloud Function deployed to Firebase (`countertap-dev`, region `asia-south1`)
- [x] `firebase.json` added at repo root for CLI deployment

### Feature: New Order Audio Alert (business app) ✅
- [x] Plays notification sound when new order arrives (app in foreground)
- [x] Text-to-Speech announcement: "New order from {name}. {N} items. Total {amount} rupees."
- [x] TTS uses Android built-in engine — free, no internet needed
- [x] `onCleared()` properly shuts down TTS to avoid leaks

### Phase 6 — Dashboards (IN PROGRESS)
- [x] Business app: Dashboard tab (tab 0) — today's revenue, 4 stat cards, active orders preview, best sellers
- [x] Business app: tabs shifted — Dashboard(0), Orders(1), Menu(2), QR(3)
- [x] Customer app: `ActiveOrderRepository` — SharedPreferences for in-progress order (kept for safety)
- [x] Customer app: `CustomerHomeViewModel` — reactive Firestore listener on `users/{uid}/orders` via `listenToUserOrders`; all active orders shown (not just most recent)
- [x] Customer app: home header changed to "Good morning/afternoon/evening" + name (time-based)
- [x] Customer app: active order cards with status timeline — all active orders shown, each tappable
- [x] Customer app: bottom nav (Home | My Orders | Scan) — persistent on both Home and My Orders screens
- [x] Business app: dashboard active order cards tappable — navigates to Orders tab
- [x] Dashboard design improvements (both apps) — done

### Phase 5 — Payments ✅ (v1 Cash-only)
- [x] **Cash-only payment** — UPI deep link abandoned (GPay blocks amount pre-fill from unregistered apps)
- [x] `PaymentMethod` + `PaymentStatus` constants added to shared `Models.kt`
- [x] `Order` model has `paymentMethod` (default "cash") and `paymentStatus` (default "unpaid") fields
- [x] Business app: **"Mark as Paid"** button on active order cards — updates both tenant and user order docs atomically
- [x] Business app: Unpaid/Paid badge on every order card
- [x] Business app: Complete button disabled until order is marked paid
- [x] Customer app: Order Tracking shows **"Pay ₹X cash at counter"** (orange) when unpaid
- [x] Customer app: Order Tracking switches to **"✅ Payment confirmed"** (green) in real time when owner marks paid
- [x] `OrderRepository.markPaymentPaid` (customer) + `OrderRepository.markAsPaid` (business) — both update tenant + user docs via batch write
- [ ] Razorpay UPI-only — planned for v2 (0% fee for UPI, automatic confirmation)

### Bug fixes (2026-07-05)
- [x] Stale active orders with blank tenantId/orderId filtered out in CustomerHomeViewModel
- [x] "Change Restaurant" button removed from Order Tracking (customer uses bottom nav instead)
- [x] My Orders screen: bottom nav visible, no back button needed
- [x] `CustomerBottomNav` extracted to shared `ui/home/CustomerBottomNav.kt`

### Bug fixes (2026-07-09)
- [x] `pr-checks.yml` — google-services.json injection was missing `| base64 --decode`; secrets are base64-encoded so plain `echo` produced corrupt JSON

### Dashboard design improvements (2026-07-09) ✅
- [x] Business dashboard: date in header ("Wednesday, 9 Jul"), storefront icon replaces dead notification bell
- [x] Business dashboard: revenue card has accent left bar + "from N orders today" subtitle
- [x] Business dashboard: stat cards have contextual icons (Pending, CheckCircle, Receipt, TrendingUp)
- [x] Business dashboard: empty state upgraded to icon + "Share your QR code to start receiving orders"
- [x] Business dashboard: best sellers list shows rank numbers (1, 2, 3)
- [x] Customer home: accent-bar section header for "RECENT RESTAURANTS" (consistent with business app style)
- [x] Customer home: accent-bar section header for "ACTIVE ORDERS" (orange, matching order status color)
- [x] Customer home: shop card "Open" button renamed to "Order"

### Remaining
- [x] Firestore security rules — `firestore.rules` at repo root; deploy with `firebase deploy --only firestore:rules`
- [x] Dashboard design improvements (both apps)
- [x] App icons + splash screen — storefront (business), tea cup (customer); `androidx.core:core-splashscreen` 700ms hold
- [ ] Play Store prep — needs Google Play Developer account ($25 one-time fee)

## Key Decisions Made

1. **Google Sign-In via Firebase Auth** (not direct Google) — needed for Firestore security rules (request.auth.uid)
2. **Cash-only payments v1** — UPI deep link attempted but GPay blocks `upi://pay` intents with pre-filled amount from unregistered apps; Razorpay UPI-only (0% fee) planned for v2
3. **Single Firebase project** (`countertap-dev`) serves as production — project ID is permanent and cannot be renamed; a separate prod project is not needed for a multi-tenant app at this scale
4. **Monorepo** — both apps + shared module in one repo
5. **KAPT** for Hilt (can migrate to KSP later)
6. **OrderStatus** as String constants (not enum) for Firestore compatibility
7. **guava:32.1.2-android** added to customer app — required for CameraX `ListenableFuture` compile access
8. **ML Kit barcode filter** — use `rawValue != null` not `TYPE_TEXT`; Firestore IDs are classified as TYPE_UNKNOWN
12. **TTS for new orders** — `TextToSpeech` initialized in `OrdersViewModel`, `ttsReady` flag checked before speaking; `onCleared()` calls `tts.shutdown()`; new orders detected by diffing `knownOrderIds` set; `isFirstLoad` flag skips alert on initial data load
10. **FCM token storage** — business app saves token to `users/{uid}.fcmToken` using `SetOptions.merge()` so it doesn't overwrite other user fields; `CounterTapMessagingService.onNewToken()` also updates it when the token rotates
11. **Cloud Function** — `functions/index.js` at repo root; uses Firebase Functions v2 (`onDocumentCreated`); requires Node 20; deploy with `firebase deploy --only functions` from `functions/` dir
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
3. **Only remaining work**: Play Store prep — requires creating a Google Play Developer account ($25 one-time fee at play.google.com/console), then a service account JSON for the API upload job in `release.yml`
4. Cloud Functions already deployed to `countertap-dev` (asia-south1)
5. CI/CD: `release.yml` triggers on every push to main, auto-publishes APK + AAB to GitHub Releases; Play Store upload job is stubbed out at the bottom — uncomment when account is ready
