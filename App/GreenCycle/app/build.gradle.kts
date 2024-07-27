plugins {
    id ("com.android.application")
    id ("com.google.gms.google-services")
}

android {
    namespace = "sg.edu.np.mad.greencycle"
    compileSdk = 34



    defaultConfig {
        applicationId = "sg.edu.np.mad.greencycle"
        minSdk = 29
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
    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes.add("META-INF/NOTICE.md")
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/io.netty.versions.properties")
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/DEPENDENCIES")
        }
    }


}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.biometric:biometric:1.1.0")
    implementation ("androidx.appcompat:appcompat:1.3.1")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-ml-modeldownloader")
    implementation ("com.google.firebase:firebase-auth:21.0.6")
    implementation("org.tensorflow:tensorflow-lite:2.3.0")
    implementation ("com.google.firebase:firebase-database:20.1.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.google.firebase:firebase-firestore:24.1.0")
    implementation ("com.github.bumptech.glide:glide:4.14.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.0-beta02")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.kizitonwose.calendar:view:2.0.0")
    implementation ("me.relex:circleindicator:2.1.6")
    implementation ("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation ("com.google.android.material:material:1.8.0")
    implementation ("com.sun.mail:android-mail:1.6.6")
    implementation ("com.sun.mail:android-activation:1.6.6")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("org.json:json:20201115")
    implementation("com.azure:azure-storage-blob:12.21.0") {
        exclude(group = "jakarta.activation", module = "jakarta.activation-api")
    }
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4") {
        exclude(group = "org.apache.xmlgraphics", module = "batik-all")
    }
    implementation("org.apache.xmlbeans:xmlbeans:5.1.1")
    implementation("com.sun.mail:android-mail:1.6.6") {
        exclude(group = "jakarta.activation", module = "jakarta.activation-api")
    }
    implementation("net.sourceforge.jexcelapi:jxl:2.6.12")
    implementation ("org.apache.poi:poi-ooxml-lite:5.2.4")



}
