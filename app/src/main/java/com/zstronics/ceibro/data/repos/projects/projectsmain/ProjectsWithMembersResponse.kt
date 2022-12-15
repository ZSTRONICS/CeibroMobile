package com.zstronics.ceibro.data.repos.projects.projectsmain


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.Member

@Keep
data class ProjectsWithMembersResponse(
    @SerializedName("projectDetails") val projectDetails: List<ProjectDetail>
) : BaseResponse() {
    @Keep
    data class ProjectDetail(
        @SerializedName("groups") val groups: List<Group>,
        @SerializedName("_id") val id: String,
        @SerializedName("location") val location: String,
        @SerializedName("projectMembers") val projectMembers: List<Member>,
        @SerializedName("title") val title: String
    ) {
        @Keep
        data class Group(
            @SerializedName("_id") val id: String,
            @SerializedName("members") val members: List<Any>,
            @SerializedName("name") val name: String
        )

    }
}