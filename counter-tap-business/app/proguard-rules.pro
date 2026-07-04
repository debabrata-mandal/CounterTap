# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# Keep shared data models (needed for Firestore deserialization)
-keep class com.countertap.shared.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
