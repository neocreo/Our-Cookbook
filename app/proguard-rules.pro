# Add project specific ProGuard rules here.
# By default, the tools ignore the obfuscation rules in a library aar file (a file
# ending with .aar). The flag -dontskipnonpubliclibraryclassmembers applies
# these rules to classes in each aar.
-dontskipnonpubliclibraryclassmembers

# When building a library, you may want to preserve the original line numbers
# of your source files when debugging.
# -keepattributes SourceFile,LineNumberTable

# If you have code that needs to keep the original line number information,
# uncomment the following line.
# -keepattributes *Annotation*

# Preserve the special static methods that are used by the Android runtime.
-keep class * extends java.util.ListResourceBundle {
    static java.util.List<String> getKeys();
}

# Keep all Activities, Services, BroadcastReceivers and ContentProviders
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep all classes that have a @Keep annotation
-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep <methods>;
}

# Keep all classes that have a @Keep annotation (for older Android versions)
-keep @com.google.android.gms.common.annotation.Keep class *
-keepclassmembers class * {
    @com.google.android.gms.common.annotation.Keep <methods>;
}

# Room Database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.Database { *; }
-keep class * implements androidx.room.Dao { *; }
-keep class * implements androidx.room.Entity { *; }

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class * extends net.sqlcipher.database.SQLiteDatabase { *; }

# Hilt
-keep class com.google.dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.Hilt_* { *; }
-keep class * implements dagger.hilt.android.internal.modules.ApplicationContextModule { *; }

# Keep all classes that have a @Inject annotation
-keep class * { @com.google.inject.Inject <methods>; }
-keep class * { @javax.inject.Inject <methods>; }
-keep class * { @dagger.inject.Inject <methods>; }

# Keep all classes that have a @Module or @Provides annotation
-keep class * { @dagger.Module <methods>; }
-keep class * { @dagger.Provides <methods>; }

# Keep all classes that have a @Singleton annotation
-keep class * { @javax.inject.Singleton <methods>; }

# Keep all classes that have a @Named annotation
-keep class * { @javax.inject.Named <methods>; }

# Keep all classes that have a @Binds annotation
-keep class * { @dagger.Binds <methods>; }

# Keep all classes that have a @IntoSet or @IntoMap annotation
-keep class * { @dagger.multibindings.IntoSet <methods>; }
-keep class * { @dagger.multibindings.IntoMap <methods>; }

# Keep all classes that have a @ElementsIntoSet or @ElementsIntoMap annotation
-keep class * { @dagger.multibindings.ElementsIntoSet <methods>; }
-keep class * { @dagger.multibindings.ElementsIntoMap <methods>; }

# Keep all classes that have a @Multibinds or @IntoMap annotation
-keep class * { @dagger.multibindings.Multibinds <methods>; }

# Keep all classes that have a @Subcomponent annotation
-keep class * { @dagger.Subcomponent <methods>; }

# Keep all classes that have a @Component annotation
-keep class * { @dagger.Component <methods>; }

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class * extends androidx.compose.ui.platform.ComposeView { *; }

# Navigation Component
-keep class androidx.navigation.** { *; }
-keep class * extends androidx.navigation.NavHostController { *; }
-keep class * extends androidx.navigation.NavController { *; }

# Keep all classes that have a @Composable annotation
-keep class * { @androidx.compose.runtime.Composable <methods>; }

# Keep all classes that have a @Preview annotation
-keep class * { @androidx.compose.ui.tooling.preview.Preview <methods>; }

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# CameraX
-keep class androidx.camera.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }

# Keep all classes that have a @Keep annotation for Google Play Services
-keep @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembers class * {
    @com.google.android.gms.common.annotation.KeepName <methods>;
}

# Keep all classes that have a @Keep annotation for Google Play Services Auth
-keep @com.google.android.gms.auth.api.signin.GoogleSignInClient class *
-keepclassmembers class * {
    @com.google.android.gms.auth.api.signin.GoogleSignInClient <methods>;
}

# Keep all classes that have a @Keep annotation for Google Drive API
-keep @com.google.android.gms.drive.DriveClient class *
-keepclassmembers class * {
    @com.google.android.gms.drive.DriveClient <methods>;
}

# Keep all classes for Google Credential Manager
-keep class androidx.credentials.** { *; }

# Keep all classes for Accompanist
-keep class com.google.accompanist.** { *; }

# Keep all classes for Coil
-keep class io.coil.** { *; }

# Keep all classes for ZXing
-keep class com.journeyapps.** { *; }

# Keep all classes for iText
-keep class com.itextpdf.** { *; }

# Keep all classes for Jackson
-keep class com.fasterxml.jackson.** { *; }

# Keep all classes for Kotlin
-keep class kotlinx.** { *; }

# Keep all classes for Coroutines
-keep class kotlinx.coroutines.** { *; }

# R8: suppress warnings for missing optional classes (iText bouncycastle, AWT, SLF4J)
-dontwarn com.itextpdf.bouncycastle.**
-dontwarn com.itextpdf.bouncycastlefips.**
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn javax.imageio.**
-dontwarn org.slf4j.impl.**
