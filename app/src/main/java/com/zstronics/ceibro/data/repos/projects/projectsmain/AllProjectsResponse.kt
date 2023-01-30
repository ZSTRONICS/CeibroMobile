package com.zstronics.ceibro.data.repos.projects.projectsmain

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
data class AllProjectsResponse(
    @SerializedName("result")
    val result: Result
) : BaseResponse() {
    @Keep
    @Parcelize
    data class Result(
        @SerializedName("limit")
        val limit: Int,
        @SerializedName("page")
        val page: Int,
        @SerializedName("results")
        val projects: List<Projects>,
        @SerializedName("totalPages")
        val totalPages: Int,
        @SerializedName("totalResults")
        val totalResults: Int
    ) : BaseResponse(), Parcelable {
        @Keep
        @Parcelize
        data class Projects(
            @SerializedName("chatCount")
            val chatCount: Int,
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
            @SerializedName("isDefault")
            val isDefault: Boolean,
            @SerializedName("location")
            val location: String,
            @SerializedName("owner")
            val owner: List<Owner>,
            @SerializedName("publishStatus")
            val publishStatus: String,
            @SerializedName("tasksCount")
            val tasksCount: Int,
            @SerializedName("title")
            val title: String,
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
                @SerializedName("currentlyRepresenting")
                val currentlyRepresenting: Boolean,
                @SerializedName("email")
                val email: String,
                @SerializedName("firstName")
                val firstName: String,
                @SerializedName("_id")
                val id: String,
                @SerializedName("isEmailVerified")
                val isEmailVerified: Boolean,
                @SerializedName("isOnline")
                val isOnline: Boolean,
                @SerializedName("lockedUntil")
                val lockedUntil: String,
                @SerializedName("mutedChat")
                val mutedChat: List<String>,
                @SerializedName("phone")
                val phone: String,
                @SerializedName("pinnedChat")
                val pinnedChat: List<String>,
                @SerializedName("pinnedMessages")
                val pinnedMessages: List<String>,
                @SerializedName("role")
                val role: String,
                @SerializedName("socketId")
                val socketId: String,
                @SerializedName("surName")
                val surName: String,
                @SerializedName("workEmail")
                val workEmail: String
            ) : BaseResponse(), Parcelable
        }
    }
}