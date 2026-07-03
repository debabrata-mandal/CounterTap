# CounterTap — Pre-Coding Setup Guide

Complete these steps before writing any code. Each section builds on the previous one.
Estimated time: 30–45 minutes.

---

## Step 1 — GitHub Private Repository

### 1.1 Create the repo
1. Go to https://github.com/new
2. Fill in:
   - **Repository name:** `CounterTap`
   - **Visibility:** Private
   - **Initialize with:** Add a README ✓
   - **Add .gitignore:** Android
   - **License:** MIT (optional)
3. Click **Create repository**

### 1.2 Clone it locally
```bash
git clone https://github.com/YOUR_USERNAME/CounterTap.git
cd CounterTap
```

### 1.3 Create the monorepo folder structure
```
CounterTap/
├── counter-tap-business/     ← owner app (create empty folder for now)
├── counter-tap/              ← customer app (create empty folder for now)
├── shared/                   ← shared models (create empty folder for now)
├── backend/                  ← Firebase functions (create empty folder for now)
├── PLAN.md
└── SETUP.md
```

### 1.4 Add branch protection
1. Go to your repo → **Settings** → **Branches**
2. Click **Add branch protection rule**
3. Branch name pattern: `main`
4. Check:
   - Require a pull request before merging ✓
   - Require status checks to pass before merging ✓
   - Do not allow bypassing the above settings ✓
5. Click **Create**

> This ensures no code goes to `main` without passing CI checks.

---

## Step 2 — Firebase Project

### 2.1 Create the project
1. Go to https://console.firebase.google.com
2. Click **Add project**
3. Project name: `countertap-dev`
   - This is your development project
4. Enable Google Analytics: **No** (not needed for now)
5. Click **Create project** — wait ~30 seconds
6. Click **Continue**

> You will create a second project `countertap-prod` later for production releases.
> For now, everything uses `countertap-dev`.

### 2.2 Upgrade to Blaze plan (pay-as-you-go)
You need this for Cloud Storage and Cloud Functions.
**Cost: Rs. 0/month at your scale** — you only pay if you exceed free quotas.

1. In Firebase console, click **Upgrade** at the bottom left
2. Select **Blaze plan**
3. Add a billing account (requires a credit/debit card — but nothing is charged at your scale)
4. Click **Purchase**

---

## Step 3 — Android Apps in Firebase

You need to register two Android apps — one for each app.

### 3.1 Register counter-tap-business

1. In Firebase console → **Project Overview** → click the **Android icon**
2. Fill in:
   - **Android package name:** `com.countertap.business`
   - **App nickname:** `CounterTap Business`
   - **Debug signing certificate SHA-1:** (see Step 3.3 below)
3. Click **Register app**
4. Download `google-services.json`
5. Save it as: `counter-tap-business/app/google-services.json`
   (you will add it to the project once the Android project is created)
6. Click **Next** through the remaining steps — skip the SDK setup for now

### 3.2 Register counter-tap (customer app)

1. **Project Overview** → **Add app** → Android icon
2. Fill in:
   - **Android package name:** `com.countertap.customer`
   - **App nickname:** `CounterTap`
   - **Debug signing certificate SHA-1:** (same SHA-1 as above for debug)
3. Download `google-services.json`
4. Save it as: `counter-tap/app/google-services.json`

### 3.3 Get your debug SHA-1 fingerprint

Run this in your terminal (on the machine where you will develop):

**Windows:**
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Mac / Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Look for the line that says:
```
SHA1: AA:BB:CC:DD:...
```

Copy that value and paste it into both Firebase app registrations above.

> Android Studio generates the debug keystore automatically the first time you build.
> If the file doesn't exist yet, open Android Studio first, create any project, build it once, then run the command above.

---

## Step 4 — Firebase Authentication

### 4.1 Enable Google Sign-In
1. Firebase console → **Authentication** (left sidebar)
2. Click **Get started**
3. Go to **Sign-in method** tab
4. Click **Google**
5. Toggle **Enable** → ON
6. **Project support email:** select your email
7. Click **Save**

That's it. Google Sign-In is now enabled for both apps since they share the same Firebase project.

---

## Step 5 — Firestore Database

### 5.1 Create the database
1. Firebase console → **Firestore Database** (left sidebar)
2. Click **Create database**
3. Choose **Start in test mode** (we will add proper security rules when coding)

   > Test mode allows all reads/writes for 30 days. Safe for development only.

4. **Location:** `asia-south1` (Mumbai — closest to India, lowest latency)
5. Click **Enable**

### 5.2 Note the database ID
It will be `(default)`. You will use this in the app config.

---

## Step 6 — Firebase Storage

### 6.1 Enable Storage
1. Firebase console → **Storage** (left sidebar)
2. Click **Get started**
3. Choose **Start in test mode**
4. **Location:** `asia-south1` (same as Firestore)
5. Click **Done**

---

## Step 7 — Firebase Cloud Messaging (FCM)

FCM is already enabled by default when you create a Firebase project. No extra steps needed.

It will be used to send push notifications to the owner when a new order arrives.

---

## Step 8 — GitHub Secrets (for CI/CD)

Once you have the Firebase config files and a signing keystore, you will store them as GitHub Secrets so the CI/CD pipeline can use them securely without committing sensitive files.

### 8.1 Secrets to add later
Go to your GitHub repo → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

| Secret name | What to put |
|---|---|
| `GOOGLE_SERVICES_JSON_BUSINESS` | Contents of `counter-tap-business/app/google-services.json` |
| `GOOGLE_SERVICES_JSON_CUSTOMER` | Contents of `counter-tap/app/google-services.json` |
| `KEYSTORE_FILE` | Base64-encoded release keystore (created during release setup) |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Your key alias |
| `KEY_PASSWORD` | Your key password |

> You will add these secrets in Phase 6 (CI/CD + Release). For now just remember where to put them.

---

## Step 9 — Android Studio Setup

### 9.1 Install Android Studio
Download from: https://developer.android.com/studio
Install with default settings.

### 9.2 Install required SDK
1. Open Android Studio
2. Go to **Tools** → **SDK Manager**
3. Under **SDK Platforms**, check:
   - Android 14 (API 34) ✓
   - Android 8.0 (API 26) ✓  ← minimum we will support
4. Under **SDK Tools**, check:
   - Android SDK Build-Tools ✓
   - Android Emulator ✓
   - Android SDK Platform-Tools ✓
5. Click **Apply** → **OK**

### 9.3 Create a test emulator
1. **Tools** → **Device Manager**
2. Click **Create Device**
3. Select: **Pixel 6** (or any Pixel)
4. System image: **Android 14 (API 34)**
5. Click **Finish**

---

## Step 10 — Verify Everything

Before telling me you are ready, confirm each item:

- [ ] GitHub private repo `CounterTap` created
- [ ] Folder structure created (`counter-tap-business/`, `counter-tap/`, `shared/`, `backend/`)
- [ ] Branch protection on `main` enabled
- [ ] Firebase project `countertap-dev` created
- [ ] Upgraded to Blaze plan
- [ ] Android app `com.countertap.business` registered in Firebase
- [ ] Android app `com.countertap.customer` registered in Firebase
- [ ] `google-services.json` downloaded for both apps and saved to correct folders
- [ ] SHA-1 debug fingerprint added to both Firebase app registrations
- [ ] Google Sign-In enabled in Firebase Auth
- [ ] Firestore database created (asia-south1, test mode)
- [ ] Firebase Storage enabled (asia-south1, test mode)
- [ ] Android Studio installed with API 34 SDK
- [ ] Emulator created (Pixel 6, API 34)

Once all boxes are checked, come back and say **"Setup done"** — I will start writing Phase 1 code.

---

## Quick Reference — Package Names

| App | Package name |
|---|---|
| counter-tap-business | `com.countertap.business` |
| counter-tap | `com.countertap.customer` |

## Quick Reference — Firebase Project

| Item | Value |
|---|---|
| Project ID | `countertap-dev` |
| Firestore region | `asia-south1` |
| Storage region | `asia-south1` |
| Auth provider | Google Sign-In |
