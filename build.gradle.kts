// Root build file — configuration shared across all sub-projects

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library)     apply false
    alias(libs.plugins.kotlin.android)      apply false
    alias(libs.plugins.kotlin.compose)      apply false
    alias(libs.plugins.google.services)     apply false
    alias(libs.plugins.hilt)                apply false
}
