import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// ✅ Đọc API key từ local.properties
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}
val vietmapApiKey: String = localProps.getProperty("VIETMAP_API_KEY") ?: ""

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ BuildConfig
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        buildConfigField("String", "VIETMAP_API_KEY", "\"$vietmapApiKey\"")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

}



dependencies {
    // --- AndroidX cơ bản ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.9.0")

    // --- Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)


    //navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //Map Navigation
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.cardview:cardview:1,0,0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.jakewharton:butterknife:10.2.3")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.github.vietmap-company:maps-sdk-android:2.6.0")
    implementation("com.github.vietmap-company:maps-sdk-navigation-ui-android:2.3.2")
    implementation("com.github.vietmap-company:maps-sdk-navigation-android:2.3.3")
    implementation("com.github.vietmap-company:vietmap-services-core:1.0.0")
    implementation("com.github.vietmap-company:vietmap-services-directions-models:1.0.1")
    implementation("com.github.vietmap-company:vietmap-services-turf-android:1.0.2")
    implementation("com.github.vietmap-company:vietmap-services-android:1.1.2")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.github.vietmap-company:vietmap-services-geojson-android:1.0.0")
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "3.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.0.0-beta4")


    // --- Google Play Services ---
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")



    // --- Networking ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // --- JWT Decoding ---
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // --- VietMap SDK ---
    implementation("com.github.vietmap-company:maps-sdk-android:2.0.4")
    implementation("com.github.vietmap-company:maps-sdk-plugin-localization-android:2.0.0")
    implementation("com.github.vietmap-company:vietmap-services-geojson-android:1.0.0")
    implementation("com.github.vietmap-company:vietmap-services-turf-android:1.0.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.10.1")

    // --- Image & View utilities ---
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.jakewharton:butterknife:10.2.3")

    // --- Lifecycle ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- Ngăn lỗi Duplicate class ---
    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
    }
}
