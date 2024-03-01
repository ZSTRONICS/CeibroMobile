package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2

@Dao
interface ProjectsV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(projectsData: CeibroProjectV2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleProject(projectsData: List<CeibroProjectV2>)

    @Query("SELECT * FROM projects_v2 WHERE _id = :projectId")
    suspend fun getProjectByProjectId(projectId: String): CeibroProjectV2?

    @Query("SELECT * FROM projects_v2 ORDER BY updatedAt DESC")
    suspend fun getAllProjectsForTask(): List<CeibroProjectV2>

    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe ORDER BY updatedAt DESC")
    suspend fun getAllHiddenProjects(isHiddenByMe: Boolean = true): List<CeibroProjectV2>

    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe AND isFavoriteByMe = :isFavoriteByMe AND isRecentlyUsedByMe = :isRecentlyUsedByMe ORDER BY updatedAt DESC")
    suspend fun getAllProjects(
        isHiddenByMe: Boolean = false,
        isRecentlyUsedByMe: Boolean = false,
        isFavoriteByMe: Boolean = false,
    ): List<CeibroProjectV2>?

 @Query("SELECT * FROM projects_v2 WHERE  isFavoriteByMe = :isFavoriteByMe  ORDER BY updatedAt DESC")
    suspend fun getAllProjectsNotFavorite(
     isFavoriteByMe: Boolean = false
    ): List<CeibroProjectV2>?

    @Query("SELECT * FROM projects_v2 ORDER BY updatedAt DESC")
    suspend fun getAllProjectsWithoutCondition(): List<CeibroProjectV2>?

    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe AND isRecentlyUsedByMe = :isRecentlyUsedByMe AND isFavoriteByMe = :isFavoriteByMe ORDER BY updatedAt DESC")
    suspend fun getAllRecentUsedProjects(
        isHiddenByMe: Boolean = false,
        isRecentlyUsedByMe: Boolean = true,
        isFavoriteByMe: Boolean = false,
    ): List<CeibroProjectV2>

    @Query("SELECT * FROM projects_v2 WHERE isHiddenByMe = :isHiddenByMe AND isFavoriteByMe = :isFavoriteByMe ORDER BY updatedAt DESC")
    suspend fun getAllFavoriteProjects(
        isHiddenByMe: Boolean = false,
        isFavoriteByMe: Boolean = true
    ): List<CeibroProjectV2>

    @Query("DELETE FROM projects_v2")
    suspend fun deleteAll()
}
