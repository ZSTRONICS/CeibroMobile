package com.zstronics.ceibro.data.database.dao

import androidx.room.*
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsV2DatabaseEntity

@Dao
interface ProjectsV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(projectsData: ProjectsV2DatabaseEntity)

    @Query("SELECT * FROM projects_v2")
    suspend fun getAll(): ProjectsV2DatabaseEntity?

    @Query("DELETE FROM projects_v2")
    suspend fun deleteAll()
}
