-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class io.github.nopsllide.riff.core.model.** { *; }
