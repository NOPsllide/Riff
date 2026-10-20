-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class io.github.rootscripts.riff.core.model.** { *; }
