# Proguard rules for SIMWarga
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
