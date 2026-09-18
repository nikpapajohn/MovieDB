# kotlinx.serialization keeps the generated serializers of @Serializable classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.nikpapajohn.moviedb.** {
    *** Companion;
}
-keepclasseswithmembers class com.nikpapajohn.moviedb.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Signature, Exceptions
