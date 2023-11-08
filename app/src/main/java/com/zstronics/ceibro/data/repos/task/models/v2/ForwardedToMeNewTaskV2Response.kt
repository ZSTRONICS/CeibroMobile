package com.zstronics.ceibro.data.repos.task.models.v2


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.CommentData
import com.zstronics.ceibro.data.database.models.tasks.EventData
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.ForwardData
import com.zstronics.ceibro.data.database.models.tasks.InvitedNumbers
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class ForwardedToMeNewTaskV2Response(
    @SerializedName("task")
    val task: CeibroTaskV2,
    @SerializedName("taskEvents")
    val taskEvents: List<Events>
) : BaseResponse(), Parcelable