package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "split_expenses",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val totalAmount: Double,
    val payers: Map<String, Double>, // Member Name -> Amount Paid
    val shares: Map<String, Double>, // Member Name -> Amount Owed
    val groupId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
