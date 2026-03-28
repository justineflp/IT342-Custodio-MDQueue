# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class edu.cit.custodio.mdqueue.api.models.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
