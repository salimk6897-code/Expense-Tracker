package com.example.expensetracker

import android.app.Application
import com.example.expensetracker.data.Graph

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.initialize(this)
    }
}
