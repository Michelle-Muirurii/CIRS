// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // KSP generates Room's and Moshi's code while the app builds
    id("com.google.devtools.ksp") version "2.3.10" apply false
}