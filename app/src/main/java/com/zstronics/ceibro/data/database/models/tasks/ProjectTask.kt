package com.zstronics.ceibro.data.database.models.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zstronics.ceibro.data.database.TableNames

@Entity(tableName = TableNames.Tasks)
data class ProjectTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val isMultiTask: Boolean,
    val assignedTo: List<String>,
    val admins: List<String>,
    val creator: String,
    val project: String,
    val dueDate: String,
    val state: String,
    val advanceOptions: AdvanceOptions
)
