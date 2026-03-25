package com.example.expensetracker.repository

import com.example.expensetracker.data.GroupDao
import com.example.expensetracker.data.GroupEntity
import com.example.expensetracker.data.GroupMemberCrossRef
import com.example.expensetracker.data.MemberEntity
import kotlinx.coroutines.flow.Flow

class GroupRepository(private val groupDao: GroupDao) {
    val allGroups: Flow<List<GroupEntity>> = groupDao.getAllGroups()

    suspend fun createGroup(name: String, description: String, members: List<String>): Long {
        val groupId = groupDao.insertGroup(GroupEntity(name = name, description = description))
        members.forEach { memberName ->
            groupDao.addMemberToGroup(GroupMemberCrossRef(groupId, memberName))
        }
        return groupId
    }

    fun getMembersInGroup(groupId: Long): Flow<List<MemberEntity>> = groupDao.getMembersInGroup(groupId)

    suspend fun getGroupById(groupId: Long): GroupEntity? = groupDao.getGroupById(groupId)

    suspend fun deleteGroup(groupId: Long) = groupDao.deleteGroup(groupId)
}
