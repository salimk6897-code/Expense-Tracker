package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "memberName"],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["name"],
            childColumns = ["memberName"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GroupMemberCrossRef(
    val groupId: Long,
    val memberName: String
)
