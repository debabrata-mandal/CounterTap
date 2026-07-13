# ── CounterTap Business — ProGuard / R8 rules ────────────────────────────────

# Keep all CounterTap classes (shared models, business models, repositories)
# Required: Firestore uses reflection (toObject/toObjects) to deserialize data
# classes; any renamed class loses its no-argument constructor and crashes.
-keep class com.countertap.** { *; }
-keepclassmembers class com.countertap.** { *; }

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keep class kotlinx.coroutines.** { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── ZXing (QR code generation) ────────────────────────────────────────────────
-keep class com.google.zxing.** { *; }

# ── Coil (image loading) ──────────────────────────────────────────────────────
-keep class coil.** { *; }

# ── Google Sign-In / Credential Manager ──────────────────────────────────────
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }

# ── Compose ───────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }

# ── Suppress harmless warnings from transitive dependencies ───────────────────
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
