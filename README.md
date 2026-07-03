# CounterTap

A two-app Android ordering system for small and medium shops — tea stalls, restaurants, bakeries, and more.

## Apps

| App | Package | Description |
|---|---|---|
| **counter-tap-business** | `com.countertap.business` | Shop owner app — manage products, view live orders, generate QR |
| **counter-tap** | `com.countertap.customer` | Customer app — scan QR, browse menu, place orders, pay via UPI |

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Backend:** Firebase (Firestore, Auth, Storage, FCM)
- **Auth:** Google Sign-In (free, unlimited)
- **Payments:** UPI deep link
- **CI/CD:** GitHub Actions

## Project Structure

```
CounterTap/
├── counter-tap-business/   # Owner Android app
├── counter-tap/            # Customer Android app
├── shared/                 # Shared Kotlin models & utilities
├── backend/
│   └── functions/          # Firebase Cloud Functions
└── .github/
    └── workflows/          # CI/CD pipelines
```

## Getting Started

See [SETUP.md](SETUP.md) for the full pre-coding setup guide.

## CI/CD

- **On pull request** → lint + unit tests (blocks merge on failure)
- **On git tag `v*.*.*`** → build signed APKs → publish to GitHub Releases

## Firebase Cost

At 10 shops with moderate traffic, monthly Firebase cost is **Rs. 0**. See [PLAN.md](PLAN.md) for full cost breakdown.

## Roadmap

| Phase | Description | Timeline |
|---|---|---|
| 1 | Foundation — auth, base architecture | Week 1–2 |
| 2 | Business app core — products, QR | Week 3–4 |
| 3 | Customer app core — scanner, cart | Week 5–6 |
| 4 | Real-time orders + FCM notifications | Week 7 |
| 5 | UPI payments | Week 8 |
| 6 | CI/CD + GitHub Releases | Week 9 |
| 7 | Polish + Google Play Store | Week 10+ |
