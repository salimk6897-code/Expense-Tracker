package com.example.expensetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MemberEntity::class, 
        SplitExpenseEntity::class, 
        GroupEntity::class, 
        GroupMemberCrossRef::class,
        SimpleExpenseEntity::class
    ], 
    version = 3, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun splitExpenseDao(): SplitExpenseDao
    abstract fun groupDao(): GroupDao
    abstract fun simpleExpenseDao(): SimpleExpenseDao
}
