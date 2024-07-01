import com.android.build.gradle.internal.dsl.decorator.SupportedPropertyType.Collection.List.type

buildscript {
    repositories {
        // Remove the 'google()' repository definition from here
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.3.14") // Specify the version explicitly
        classpath("com.android.tools.build:gradle:8.0.0") // Use a valid Gradle plugin version
        classpath(kotlin("gradle-plugin", version = "1.8.21")) // Ensure Kotlin version matches the one in settings.gradle.kts
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    // Apply other plugin aliases if needed
}

subprojects {
//    repositories {
//        // Remove the 'google()' repository definition from here
//        mavenCentral()
//    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
