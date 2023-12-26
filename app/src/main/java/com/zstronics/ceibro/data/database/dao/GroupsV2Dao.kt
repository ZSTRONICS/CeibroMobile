package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2

@Dao
interface GroupsV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(groupData: CeibroGroupsV2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleGroups(groupsData: List<CeibroGroupsV2>)

    @Query("SELECT * FROM groups_v2 WHERE projectId = :projectId ORDER BY updatedAt DESC")
    suspend fun getAllProjectGroups(projectId: String): List<CeibroGroupsV2>


    @Query("SELECT * FROM groups_v2 WHERE _id = :groupId")
    suspend fun getGroupByGroupId(groupId: String): CeibroGroupsV2?

//    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe ORDER BY updatedAt DESC")
//    suspend fun getAllHiddenProjects(isHiddenByMe: Boolean = true): List<CeibroProjectV2>
//
//    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe ORDER BY updatedAt DESC")
//    suspend fun getAllProjects(isHiddenByMe: Boolean = false): List<CeibroProjectV2>
//
//    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe AND isRecentlyUsedByMe = :isRecentlyUsedByMe ORDER BY updatedAt DESC")
//    suspend fun getAllRecentUsedProjects(isHiddenByMe: Boolean = false, isRecentlyUsedByMe: Boolean = true): List<CeibroProjectV2>
//
//    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe AND isFavoriteByMe = :isFavoriteByMe ORDER BY updatedAt DESC")
//    suspend fun getAllFavoriteProjects(isHiddenByMe: Boolean = false, isFavoriteByMe: Boolean = true): List<CeibroProjectV2>

    @Query("DELETE FROM groups_v2 WHERE _id = :groupId")
    suspend fun deleteGroupById(groupId: String)

    @Query("DELETE FROM groups_v2")
    suspend fun deleteAll()
}
