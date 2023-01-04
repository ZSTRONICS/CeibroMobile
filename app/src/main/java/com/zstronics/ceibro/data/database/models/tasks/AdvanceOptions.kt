package com.zstronics.ceibro.data.database.models.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zstronics.ceibro.data.database.TableNames

@Entity(tableName = TableNames.AdvanceOptions)
data class AdvanceOptions(
    @PrimaryKey
    val id: Int,
    val confirmNeeded: List<String>,
    val viewer: List<String>,
    val categories: List<String>,
    val manPower: Int,
    val location: String,
    val priority: String,
    val timeLog: Boolean,
    val checkList: List<String>,
    val isAdditionalWork: Boolean,
    val startDate: String
)
