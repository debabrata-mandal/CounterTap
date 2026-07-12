# CounterTap — Claude Context File

This file helps Claude quickly understand the project state in any new session.
Read this file before making any changes.

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
  products/{productId}       — Product data (available:bool, categoryId:string, optionGroups:[])
  orders/{orderId}           — Order data (status, paymentStatus, paymentMethod, note, items[], customerId, tableSessionId?); each item has selectedOptions:[]
  categories/{categoryId}    — Menu categories (name, order:int)
  tables/{tableId}           — (Phase 8A) Table definitions: name, createdAt
  tableSessions/{sessionId}  — (Phase 8A) Active/closed sessions: tableId, tableName, status, openedAt, closedAt?, orderIds[], customerIds[], totalAmount, paymentStatus, paidVia?
  creditLines/{customerId}   — (Phase 8C) Credit line per customer: customerId, customerName, customerEmail, limit, balance, status, requestedAt, approvedAt?
users/{userId}/
  fcmToken                   — FCM device token (saved by business app on startup)
  orders/{orderId}           — UserOrderSummary: tenantId, shopName, status, totalAmount, items, note, createdAt
  creditLines/{tenantId}     — (Phase 8C) Mirror of tenant credit line for customer app: tenantId, shopName, limit, balance, status
```

Shared data models are in `shared/src/main/java/com/countertap/shared/Models.kt`

## Current Phase

**Phase 8 — Tables, Hamburger Menu & Credit Lines ✅ COMPLETE**
All three sub-phases shipped in branch `feature/credite-line` (PR open against main):
- **8A** — Tables (opt-in collective billing; invisible to shops with no tables)
- **8B** — Hamburger menu in business app: edit shop details, edit UPI, access credit lines
- **8C** — Credit lines: customer requests credit, owner approves with limit, cart charges to credit

**App version bumped to 2.0 (versionCode 2)** for both apps.

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

### Work done (2026-07-11) ✅

#### Feature: Product Options / Variants
- [x] `shared/Models.kt` — added `ProductOption`, `OptionGroup`, `SelectedOption` data classes; `Product.optionGroups: List<OptionGroup>`; `OrderItem.selectedOptions: List<SelectedOption>`
- [x] Business app `AddEditProductScreen` — "Options / Variants" section: add/remove option groups, each group has name, Required toggle, Multi-select toggle, list of options with name + price addon (+₹); uses `UUID.randomUUID()` for group/option IDs
- [x] Customer app `CartViewModel` — `CartItem` now carries `selectedOptions: List<SelectedOption>` and `unitPrice: Double`; added `addConfigured()`/`removeConfigured()`; `quantityOf()` sums all combos; `totalAmount` uses `unitPrice`; `placeOrder` stores `selectedOptions` on each `OrderItem`
- [x] Customer app `MenuScreen` — products with options show "Customisable" badge; ADD button opens `OptionPickerSheet` (ModalBottomSheet); RadioButton for single-select groups, Checkbox for multi-select; required groups block "Add to Cart" until selection made; price addon shown per option; "N in cart +" badge when quantity > 0 for option products; cart bar total uses `unitPrice`
- [x] Customer app `CartScreen` — selected options shown in blue below product name ("Sugar: With Sugar · Milk: Full Milk"); unit price shown as "₹X each"; stepper calls `addConfigured`/`removeConfigured` with the item's `selectedOptions`
- [x] Business app `OrdersScreen` — selected options shown in blue below each product name in order cards so owner can see exactly what customer chose

#### Bug fixes
- [x] `AddEditProductScreen` toggle overlap — `Switch(Modifier.size(32.dp))` was smaller than minimum touch target; replaced side-by-side layout with full-width label-left / switch-right rows
- [x] Business app orders missing customization details — `OrdersScreen` items loop now shows `selectedOptions` indented beneath each product name

#### UI improvements
- [x] Business app `CategoryScreen` — redesigned from plain list to 2-column grid of square tiles; each tile has emoji illustration (auto-detected from 30+ keyword rules: tea→☕, biryani→🍚, momos→🥟, etc.) on a coloured gradient background, category name + item count in a darker strip below; delete button as translucent overlay in top-right corner; empty state with 🗂️ illustration; emoji are standard Unicode rendered as large `Text` — no assets or internet required

### Remaining (pre-Phase 8)
- [x] Firestore security rules — `firestore.rules` at repo root; deploy with `firebase deploy --only firestore:rules`
- [x] Dashboard design improvements (both apps)
- [x] App icons + splash screen — storefront (business), tea cup (customer); `androidx.core:core-splashscreen` 700ms hold
- [ ] Play Store prep — needs Google Play Developer account ($25 one-time fee)

### Phase 8A — Tables (opt-in collective billing) ✅
- [x] `shared/Models.kt` — added `Table`, `TableSession`, `TableSessionStatus`; added `tableSessionId` + `tableName` to `Order`
- [x] `TablesRepository` (business + customer) — CRUD tables, create/close sessions, addOrderToSession (FieldValue.arrayUnion + increment)
- [x] Business app: `TableManagementScreen` — list/add/delete tables; session cards with close; empty state
- [x] Business app: Orders tab gets Individual / Tables sub-tabs (auto-shown when table orders exist)
- [x] Business app bottom nav: Tables tab (Dashboard | Orders | Menu | Tables | QR)
- [x] Customer app: `TableContextHolder` singleton + `TablePickerViewModel` — fetches tables, creates/joins session
- [x] Customer app: `CartViewModel.placeOrder` embeds `tableSessionId`/`tableName`; calls `addOrderToSession`; clears context after
- [x] Customer app: Cart header shows table name chip when in a table session
- [x] Customer app: **Table selector chip in `MenuScreen` header** — visible when shop has tables; shows "Takeaway" or selected table name; tappable to reselect
- [x] Customer app: `TablePickerBottomSheet` in `MenuScreen` — Takeaway option + scrollable table list; "Selected" badge on current choice; creates/joins Firestore session on pick
- [x] `TablePickerViewModel.pickTable(table: Table)` — simplified signature; `tenantId` stored internally from `loadTables()` call
- [x] Firestore rules — added `tables/{tableId}` (read: auth, write: owner) + `tableSessions/{sessionId}` (read/create/update: auth, delete: never) inside `tenants/{tenantId}`

### Bug fixes & polish (2026-07-12)
- [x] Business app: QR tab label was wrapping ("QR\nCod\ne") with 5 tabs — label shortened to "QR", icon changed to `Icons.Outlined.QrCode2`, tab columns use `Modifier.weight(1f)` + `padding(horizontal = 4.dp)`, pill padding reduced to `horizontal = 8.dp`
- [x] Business app Tables tab crash — uncaught exception in child `launch {}` coroutines caused app crash; wrapped inner launch blocks in `TablesViewModel` and `OrdersViewModel` with try-catch so tables feature fails silently (non-fatal, opt-in)
- [x] Business app Tables tab crash — Firestore PERMISSION_DENIED because rules didn't cover `tables`/`tableSessions`; fixed by deploying updated `firestore.rules`
- [x] `TablePickerScreen.kt` call-site updated after `pickTable` signature change (`pickTable(table)` + `onProceed()` separately)
- [x] `OrdersViewModel` — `openSessions` wipeout bug: `listenToOrders` was creating a fresh `OrdersUiState()` on every order change, resetting sessions to `emptyList()`; fixed with `.copy()`
- [x] `TablesRepository.settleAndCloseSession` — new method replaces `closeSession`; batch-writes all non-cancelled orders to COMPLETED + paid (both tenant + user docs); `TablesViewModel` and `TableManagementScreen` updated
- [x] Business app `OrdersScreen` — table orders now use kitchen-only actions (Accept/Reject/Mark Ready); no per-order payment/complete buttons; Settle & Close at session level handles payment
- [x] Customer app `CartViewModel` — removed `tableContextHolder.clear()` after order placement; table context persists for multiple rounds at same table
- [x] Customer app `OrderTrackingScreen` — payment section checks `tableName`: if blank → cash reminder; if set → "Added to {table}'s tab" info banner (AccentBlue); paid → green confirmed banner
- [x] `TablePickerViewModel.loadTables()` — clears `TableContextHolder` when `tenantId` changes (fresh nav = new tenant); prevents stale table context when returning to a restaurant from dashboard
- [x] Business app `TableManagementScreen` — spacing fixes: SectionLabel top padding reduced; header bottom padding reduced; "Close Session" button replaced with "Settle & Close • ₹X" green button
- [x] Business app `ProductListScreen` — removed double bottom padding (Scaffold FAB padding + `contentPadding` were stacking)

### UX improvements (2026-07-12)
- [x] Business app `DashboardScreen` — `MiniOrderCard` shows table/takeaway chip (TableRestaurant icon + table name, or ShoppingBag + "Takeaway"); `onNavigateToOrders` callback now passes `orderId: String`
- [x] Business app `HomeScreen` — `focusedOrderId` state threads dashboard tap → Orders tab; `DashboardScreen` sets orderId before switching to tab 1; `OrdersScreen` receives it
- [x] Business app `OrdersScreen` — unified single list replacing Individual/Tables sub-tabs: table sessions first (orange section header + nested `TableOrderCard`s + Settle & Close), then takeaway active orders, then completed/cancelled; `focusedOrderId` auto-scrolls to the tapped order card and highlights it with blue accent background

### Phase 8B — Hamburger Menu & Shop Settings ✅
- [x] Business app: `ModalNavigationDrawer` in `HomeScreen` — hamburger icon in top bar; shop name + email in drawer header
- [x] Drawer items: Edit Shop Details, Edit UPI, Credit Lines, Sign Out
- [x] `ShopSetupScreen` — `isEditMode=true` nav arg; pre-populated from `TenantViewModel.tenantState`; "Save changes" button calls `updateShop()`; pops back on success
- [x] `UpiSetupScreen` — `isEditMode=true` nav arg; pre-populated UPI ID; "Save" button; pops back on success
- [x] `TenantViewModel.updateShop(name, address, phone)` — calls `TenantRepository.updateTenant`, updates local state
- [x] `ui/credit/CreditScreen.kt` — placeholder shell with AccountBalance icon + "Coming in the next update" message
- [x] `Routes.kt` — added `EDIT_SHOP`, `EDIT_UPI`, `CREDIT_LINES`
- [x] `AppNavGraph.kt` — wired all three new routes; sign-out via `AuthViewModel.signOut()` clears full back stack

### Phase 8C — Credit Lines ✅
- [x] `shared/Models.kt` — added `CreditLine` data class, `CreditLineStatus` object (PENDING/ACTIVE/REJECTED), `PaymentMethod.CREDIT = "credit"`
- [x] Customer `CreditRepository` — `requestCredit`, `listenToCreditLine`, `applyOrderToCredit`; batch-writes to both tenant + user paths
- [x] Business `CreditRepository` — `listenToCreditLines`, `approveCredit` (with limit), `rejectCredit`, `markSettled` (resets balance)
- [x] Business `CreditViewModel` — splits lines into pendingLines / activeLines; loads tenant via `TenantRepository.getTenantByOwnerId`
- [x] Business `CreditScreen` — pending cards (Reject + Approve with limit dialog), active cards (balance progress bar, Settle button), empty state
- [x] Customer `CartViewModel` — `loadCreditLine(tenantId)` listener, `requestCredit`, `setPaymentMethod`; credit orders set `paymentStatus=PAID` and call `applyOrderToCredit` after placement
- [x] Customer `MenuScreen` header redesigned — chips moved to a dedicated `CardElevated` context bar row below shop name; table chip left, credit chip right; alpha 0.12→0.22, text 11sp→13sp SemiBold
- [x] Customer `CartScreen` — Cash | Credit payment toggle shown when active credit line; credit option shows remaining balance or "Limit exceeded"; Place Order button turns green for credit
- [x] `firestore.rules` — `creditLines/{customerId}` rules in tenant; `creditLines/{tenantId}` rules in user; order `create` rule allows `paymentStatus=paid` when `paymentMethod=credit`
- [x] Table description field — `Table.description: String`; business `TableManagementScreen` add/edit dialogs include description; customer `TablePickerBottomSheet` shows description as subtitle

### Work done (2026-07-13) ✅

#### MenuScreen header redesign
- [x] Header refactored from a flat `Row` to a `Column` with two sections: shop info row + context chips row
- [x] Context chips row has `CardElevated` background, `padding(16.dp, 10.dp)` — chips no longer crammed inside the shop name column
- [x] Table chip: `AccentBlue.copy(alpha=0.22f)` background, AccentBlue text 13sp SemiBold (was CardElevated bg, TextPrimary 12sp Medium — near-zero contrast)
- [x] Credit chips: alpha 0.12→0.22, text 11sp→13sp SemiBold; "Apply for credit" uses `CardBackground` on `CardElevated` for real contrast
- [x] Table chip left / credit chip right with `Spacer(weight(1f))`; credit chip left-aligned when no tables present

#### Version bump
- [x] Both apps bumped from `1.0 (versionCode 1)` → `2.0 (versionCode 2)`

## Key Decisions Made (Phase 8)

16. **Tables are opt-in via presence** — 0 tables in Firestore = feature invisible to customers; works for all business types (restaurants, medicine shops, retail). No toggle needed.
17. **Table selection is in-menu, not post-scan** — Scanner always navigates straight to `MENU/{tenantId}`; no per-table QR codes. Customers choose Takeaway or a table from a chip in the `MenuScreen` header (visible only when the shop has ≥ 1 table). `TablePickerViewModel.loadTables()` is called inside `MenuScreen` via `LaunchedEffect`; `pickTable(table)` creates/joins a Firestore session and updates `TableContextHolder`. `TablePickerScreen.kt` still compiles but is not in the nav graph.
18. **Hamburger menu** — `ModalNavigationDrawer` in business `HomeScreen`; edit screens reuse onboarding composables with an `isEditMode: Boolean` nav arg. No separate `SettingsRepository` needed — `TenantViewModel.updateShop()` writes directly via existing `TenantRepository.updateTenant()`.
19. **Credit mirroring** — every credit mutation batch-writes to both `tenants/{tenantId}/creditLines/{uid}` and `users/{uid}/creditLines/{tenantId}` to keep both apps in sync.
20. **No partial payments** — an order is fully cash or fully credit; no split payment.
21. **QR format unchanged** — shop QR still encodes plain `tenantId`; per-table QR deferred.

## Key Decisions Made (Phases 1–7)

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
13. **Product options / CartItem keying** — each unique (productId + selectedOptions) combo is a separate `CartItem`; `SelectedOption` is a data class so list equality works for matching; `quantityOf(productId)` sums across all combos for the "N in cart" badge; `addConfigured`/`removeConfigured` are the canonical mutators — `add`/`remove` delegate to them with `emptyList()`
14. **Category emoji tiles** — emoji auto-detected from category name via keyword lookup (`categoryEmoji()`); colour derived from `abs(name.hashCode()) % palette.size`; rendered as `Text(emoji, fontSize = 52.sp)` over a `color.copy(alpha = 0.18f)` background — no assets, no internet
15. **Switch size constraint bug** — never apply `Modifier.size()` to a `Switch`; the minimum touch target is 48×48dp and clipping it causes visual overlap with adjacent composables; use a `SpaceBetween` Row with label on left and Switch on right instead

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
3. **Active work**: Phase 8 fully complete (8A + 8B + 8C). PR open: `feature/credite-line` → `main`. Both apps at v2.0.
4. **Firestore rules** must be deployed before credit lines work: `firebase deploy --only firestore:rules`
5. **Pending post-Phase 8**: Play Store prep — Google Play Developer account ($25 one-time fee), then service account JSON for `release.yml` Play Store upload job
6. Cloud Functions already deployed to `countertap-dev` (asia-south1)
7. CI/CD: `release.yml` triggers on every push to main, auto-publishes APK + AAB to GitHub Releases
