###############################################
# Firebase (only keep analytics + crashlytics)
###############################################
-keep class com.google.firebase.analytics.FirebaseAnalytics { *; }
-keep class com.google.firebase.crashlytics.FirebaseCrashlytics { *; }
-keepattributes SourceFile,LineNumberTable
-dontwarn com.google.firebase.**

###############################################
# Google Play Services (only Ads + Consent SDK)
###############################################
-keep class com.google.android.gms.ads.AdView { *; }
-keep class com.google.android.gms.ads.AdRequest { *; }
-keep class com.google.android.gms.ads.AdSize { *; }
-keep class com.google.android.gms.ads.MobileAds { *; }
-keep class com.google.android.gms.ads.initialization.** { *; }

-keep class com.google.ads.mediation.admob.AdMobAdapter { *; }
-keep class com.google.android.ump.** { *; }

-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.ump.**

###############################################
# Android Browser Helper (TWA)
###############################################
-keep class com.google.androidbrowserhelper.trusted.** { *; }
-dontwarn com.google.androidbrowserhelper.**

###############################################
# Your App Package
###############################################
-keep class com.gmail.omsjsr.smartcamera.** { *; }

###############################################
# Debug info (optional)
###############################################
# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable

# Hide original source file names
-renamesourcefileattribute SourceFile

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
