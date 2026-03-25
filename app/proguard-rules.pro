# Rules for Room
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static final androidx.room.RoomDatabase$Callback Companion;
}
-keep class * extends androidx.room.Entity
-keepclassmembers class * extends androidx.room.Entity {
    <fields>;
    <methods>;
}
-keep class * extends androidx.room.Dao
-keep class * extends androidx.room.TypeConverter

# Rules for Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.example.expensetracker.data.** { *; }
-keepclassmembers class com.example.expensetracker.data.** { *; }
