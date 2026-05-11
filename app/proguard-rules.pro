# Keep kotlinx.serialization metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# App models that are serialized with @Serializable
-keep,includedescriptorclasses class com.navieat.app.**$$serializer { *; }
-keepclassmembers class com.navieat.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.navieat.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okio.**
-dontwarn javax.annotation.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory$* { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# PdfBox-Android uses reflection on a couple of font/image classes
-dontwarn org.apache.pdfbox.**
-keep class com.tom_roush.pdfbox.** { *; }

# MSAL
-keep class com.microsoft.identity.** { *; }
-dontwarn com.microsoft.identity.**
