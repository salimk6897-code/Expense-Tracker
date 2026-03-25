package com.example.expensetracker.data

import android.content.Context
import androidx.room.Room

object Graph {
    lateinit var database: AppDatabase
        private set

    fun initialize(context: Context) {
        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "expense-tracker-db"
        )
        .fallbackToDestructiveMigration() // Temporary for development
        .build()
    }

    val memberDao by lazy { database.memberDao() }
    val splitExpenseDao by lazy { database.splitExpenseDao() }
    val groupDao by lazy { database.groupDao() }
    val simpleExpenseDao by lazy { database.simpleExpenseDao() }
}
