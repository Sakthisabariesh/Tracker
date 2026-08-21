# Room database
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.** { *; }

# Models
-keep class com.antigravity.expensetracker.data.model.** { *; }
-keep class com.antigravity.expensetracker.domain.model.** { *; }
