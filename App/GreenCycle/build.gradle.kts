buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.3.14")
        classpath ("com.android.tools.build:gradle:8.4.0")

    }
}
plugins {
//    alias(libs.plugins.android.application) apply false
//    id("com.google.gms.google-services") version "4.4.1" apply false
    id ("com.android.application") version "8.4.0" apply false
    id ("com.android.library") version "8.4.0" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

