-dontwarn kotlinx.datetime.**
-dontwarn okhttp3.**
-dontwarn org.slf4j.**
-dontwarn org.apache.log4j.**
-dontwarn ch.qos.logback.**
-dontwarn io.github.g00fy2.**

-keep class org.sqlite.** { *; }

# fix error while log to file
-keep class logger.** { *; }
-keep class ch.qos.logback.** { *; }

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class **.entity.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class **.entity.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class **.entity.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}

# Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class entity.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class entity.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class entity.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
