import java.io.InputStream
import java.util.Properties

import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.Pmd

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    checkstyle
    pmd
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists())
{
    localPropertiesFile.inputStream().use { inputStream: InputStream ->
        localProperties.load(inputStream)
    }
}

val plantNetApiKey: String =
    localProperties.getProperty("PLANTNET_API_KEY", "")

android {
    namespace = "com.example.florascan"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.florascan"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "PLANTNET_API_KEY",
            "\"$plantNetApiKey\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))

    implementation(libs.activity.compose)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.exifinterface:exifinterface:1.4.2")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.mockito:mockito-core:5.12.0")

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")

    debugImplementation(libs.ui.test.manifest)
    debugImplementation(libs.ui.tooling)
}

checkstyle {
    toolVersion = "10.21.4"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
}

pmd {
    toolVersion = "7.9.0"
    ruleSetFiles = files(
        rootProject.file("config/pmd/pmd.xml")
    )
    ruleSets = emptyList()
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
}

tasks.register<Checkstyle>("checkstyleMain") {
    group = "verification"
    description = "Runs Checkstyle on main Java sources."

    source("src/main/java")

    include("**/*.java")

    classpath = files()
}

tasks.register<Pmd>("pmdMain") {
    group = "verification"
    description = "Runs PMD on all Java sources."

    source = fileTree("src") {
        include("main/java/**/*.java")
        include("test/java/**/*.java")
        include("androidTest/java/**/*.java")
    }

    classpath = files()

    ruleSetFiles = files(
        rootProject.file("config/pmd/pmd.xml")
    )

    ruleSets = emptyList()
}
