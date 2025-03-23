import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.orllewin.ldwh"
    compileSdk = 35

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "com.orllewin.ldwh"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val mapboxProperties = project.rootProject.file("mapbox.properties")
        val properties = Properties()
        properties.load(mapboxProperties.inputStream())
        val mapboxAccessToken = properties.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""
        buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"$mapboxAccessToken\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "route"
    productFlavors {
        create("offas_dyke") {
            dimension = "route"
            resValue(type = "string", name = "app_name", value = "Offas Dyke")
            applicationIdSuffix = ".offas_dyke"
            versionNameSuffix = "-offas_dyke"
        }
        create("pennine_way") {
            dimension = "route"
            resValue(type = "string", name = "app_name", value = "Pennine Way")
            applicationIdSuffix = ".pennine_way"
            versionNameSuffix = "-pennine_way"
            isDefault = true
        }
        create("yorkshire_three_peaks") {
            dimension = "route"
            resValue(type = "string", name = "app_name", value = "Yorkshire 3 Peaks")
            applicationIdSuffix = ".yorkshire_three_peaks"
            versionNameSuffix = "-yorkshire_three_peaks"
            isDefault = true
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.android.maps.utils)
    implementation(libs.mapbox)
    implementation(libs.mapbox.compose)

    implementation(libs.androidx.lifecycle.extensions)
    annotationProcessor (libs.androidx.lifecycle.compiler)
}