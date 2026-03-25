package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMemberToGroup(crossRef: GroupMemberCrossRef)

    @Query("SELECT * FROM groups ORDER BY timestamp DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Transaction
    @Query("SELECT * FROM members JOIN group_members ON members.name = group_members.memberName WHERE group_members.groupId = :groupId")
    fun getMembersInGroup(groupId: Long): Flow<List<MemberEntity>>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): GroupEntity?

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)
}
