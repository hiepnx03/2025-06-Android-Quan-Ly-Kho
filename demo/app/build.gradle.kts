plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.demo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    // AppCompat for material design and backward compatibility
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    // Material Components for UI
    implementation("com.google.android.material:material:1.12.0")

    // ConstraintLayout for UI design
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Unit testing
    testImplementation("junit:junit:4.13.2")

    // Android instrumentation testing
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Lombok for reducing boilerplate code
    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

