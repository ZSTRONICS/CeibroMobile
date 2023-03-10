package com.zstronics.ceibro.data.repos.projects.projectsmain

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AllProjectsResponse(
    @SerializedName("results")
    val projects: List<Projects>
) : BaseResponse(), Parcelable {
    @Keep
    @Parcelize
    data class Projects(
        @SerializedName("access")
        val access: List<String>,
        @SerializedName("chatCount")
        val chatCount: Int,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("creator")
        val creator: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("docsCount")
        val docsCount: Int,
        @SerializedName("dueDate")
        val dueDate: String,
        @SerializedName("extraStatus")
        val extraStatus: List<String>,
        @SerializedName("_id")
        val id: String,
        @SerializedName("inDraftState")
        val inDraftState: Boolean,
        @SerializedName("isDefault")
        val isDefault: Boolean,
        @SerializedName("location")
        val location: String,
        @SerializedName("owner")
        val owner: List<Owner>,
        @SerializedName("projectPhoto")
        val projectPhoto: String,
        @SerializedName("publishStatus")
        val publishStatus: String,
        @SerializedName("tasksCount")
        val tasksCount: Int,
        @SerializedName("title")
        val title: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("usersCount")
        val usersCount: Int
    ) : BaseResponse(), Parcelable {
        @Keep
        @Parcelize
        data class Owner(
            @SerializedName("companyLocation")
            val companyLocation: String,
            @SerializedName("companyName")
            val companyName: String,
            @SerializedName("companyPhone")
            val companyPhone: String,
            @SerializedName("companyVat")
            val companyVat: String,
            @SerializedName("createdAt")
            val createdAt: String,
            @SerializedName("currentlyRepresenting")
            val currentlyRepresenting: Boolean,
            @SerializedName("email")
            val email: String,
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("_id")
            val id: String,
            @SerializedName("isOnline")
            val isOnline: Boolean,
            @SerializedName("phone")
            val phone: String,
            @SerializedName("pinnedChat")
            val pinnedChat: List<String>,
            @SerializedName("profilePic")
            val profilePic: String,
            @SerializedName("role")
            val role: String,
            @SerializedName("surName")
            val surName: String,
            @SerializedName("updatedAt")
            val updatedAt: String,
            @SerializedName("workEmail")
            val workEmail: String
        ) : BaseResponse(), Parcelable
    }
}