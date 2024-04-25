package com.zstronics.ceibro.data.database.models.tasks


import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNamesV2
import kotlinx.parcelize.Parcelize


@Entity(tableName = TableNamesV2.TaskBasic, primaryKeys = ["id"])
@Parcelize
@Keep
data class CeibroTaskV2(
    @SerializedName("access")
    val access: List<String>,
    @SerializedName("assignedToState")
    var assignedToState: MutableList<AssignedToState>,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("creator")
    val creator: TaskMemberDetail,
    @SerializedName("creatorState")
    var creatorState: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("doneCommentsRequired")
    val doneCommentsRequired: Boolean,
    @SerializedName("doneImageRequired")
    val doneImageRequired: Boolean,
    @SerializedName("dueDate")
    val dueDate: String,
    @SerializedName("hiddenBy")
    var hiddenBy: List<String>,
    @SerializedName("_id")
    val id: String,
    @SerializedName("isCanceled")
    var isCanceled: Boolean,
    @SerializedName("invitedNumbers")
    var invitedNumbers: List<InvitedNumbers>,
    @SerializedName("locations")
    val locations: List<String>?,
    @SerializedName("project")
    val project: ProjectOfTask?,
    @SerializedName("seenBy")
    var seenBy: List<String>,
    @SerializedName("taskUID")
    val taskUID: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("confirmer")
    val confirmer: TaskMemberDetail?,
    @SerializedName("tags")
    val tags: List<Tag>?,
    @SerializedName("viewer")
    val viewer: List<TaskMemberDetail>?,
    @SerializedName("updatedAt")
    var updatedAt: String,
    @SerializedName("isTaskInApproval")
    var isTaskInApproval: Boolean,
    @SerializedName("files")
    var files: List<TaskFiles> = emptyList(),
    @SerializedName("taskRootState")
    var taskRootState: String,
    @SerializedName("rootState")
    var rootState: String,
    @SerializedName("userSubState")
    var userSubState: String,
    @SerializedName("isAssignedToMe")
    val isAssignedToMe: Boolean,
    @SerializedName("isCreator")
    var isCreator: Boolean,
    @SerializedName("isTaskConfirmer")
    var isTaskConfirmer: Boolean,
    @SerializedName("isTaskViewer")
    var isTaskViewer: Boolean,
    @SerializedName("isHiddenByMe")
    var isHiddenByMe: Boolean,
    @SerializedName("isSeenByMe")
    var isSeenByMe: Boolean,
    @SerializedName("fromMeState")
    var fromMeState: String,
    @SerializedName("toMeState")
    var toMeState: String,
    @SerializedName("hiddenState")
    var hiddenState: String,
    @SerializedName("eventsCount")
    var eventsCount: Int,
    @SerializedName("hasPinData")
    var hasPinData: Boolean,
    @SerializedName("pinData")
    var pinData: CeibroDrawingPins?,

    //only for room DB Column
    @ColumnInfo(name = "isBeingDoneByAPI") @Transient var isBeingDoneByAPI: Boolean = false
) : Parcelable {
    override fun hashCode(): Int {
        return super.hashCode()
    }
}


@Entity(
    tableName = TableNamesV2.TaskEvents, primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = CeibroTaskV2::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE, // Define the behavior on delete
            onUpdate = ForeignKey.NO_ACTION  // Define the behavior on update
        )
    ], indices = [Index(value = ["taskId"])]
)
@Parcelize
@Keep
data class Events(
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("eventData")
    val eventData: List<EventData>?,
    @SerializedName("invitedMembers")
    val invitedMembers: List<EventData>?,
    @SerializedName("commentData")
    val commentData: CommentData?,
    @SerializedName("eventType")
    val eventType: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("initiator")
    val initiator: TaskMemberDetail,
    @SerializedName("taskId")
    val taskId: String,
    @SerializedName("updatedAt")
    var updatedAt: String,
    @SerializedName("eventNumber")
    val eventNumber: Int,
    @SerializedName("eventSeenBy")
    var eventSeenBy: List<String>? = emptyList(),
    @SerializedName("isPinned")
    var isPinned: Boolean?
) : Parcelable


@Parcelize
@Keep
data class Tag(
    @SerializedName("_id")
    val id: String,
    @SerializedName("topic")
    val topic: String
) : Parcelable

@Parcelize
@Keep
data class ProjectOfTask(
    @SerializedName("_id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("location")
    val location: String
) : Parcelable


@Parcelize
@Keep
data class TaskMemberDetail(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("surName") val surName: String,
    @SerializedName("profilePic") val profilePic: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("companyName") val companyName: String?,
    @SerializedName("_id") val id: String,
    @SerializedName("_id")
    var jobTitle: String = "",
    @Transient
    var isChecked: Boolean = false,
    ) : Parcelable


@Parcelize
@Keep
data class AssignedToState(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("profilePic")
    val profilePic: String?,
    @SerializedName("state")
    var state: String,
    @SerializedName("surName")
    val surName: String,
    @SerializedName("userId")
    val userId: String
) : Parcelable


@Parcelize
@Keep
data class TaskFiles(
    @SerializedName("access")
    val access: List<String>,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("fileTag")
    val fileTag: String,
    @SerializedName("fileType")
    val fileType: String,
    @SerializedName("fileUrl")
    val fileUrl: String,
    @SerializedName("hasComment")
    val hasComment: Boolean,
    @SerializedName("_id")
    val id: String,
    @SerializedName("moduleId")
    val moduleId: String,
    @SerializedName("moduleType")
    val moduleType: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("uploadStatus")
    val uploadStatus: String,
    @SerializedName("uploadedBy")
    val uploadedBy: TaskMemberDetail,
    @SerializedName("__v")
    val v: Int?,
    @SerializedName("version")
    val version: Int?
) : Parcelable


@Parcelize
@Keep
data class EventFiles(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("fileTag")
    val fileTag: String,
    @SerializedName("fileType")
    val fileType: String,
    @SerializedName("fileUrl")
    val fileUrl: String,
    @SerializedName("hasComment")
    val hasComment: Boolean,
    @SerializedName("_id")
    val id: String,
    @SerializedName("moduleId")
    val moduleId: String,
    @SerializedName("moduleType")
    val moduleType: String,
    @SerializedName("uploadStatus")
    val uploadStatus: String
) : Parcelable


@Parcelize
@Keep
data class EventData(
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("profilePic")
    val profilePic: String?,
    @SerializedName("surName")
    val surName: String?
) : Parcelable


@Parcelize
@Keep
data class ForwardData(
    @SerializedName("invitedNumbers")
    val invitedNumbers: List<InvitedNumbers>,
    @SerializedName("assignedToState")
    val assignedToState: List<AssignedToState>
) : Parcelable


@Parcelize
@Keep
data class CommentData(
    @SerializedName("files")
    val files: List<EventFiles>,
    @SerializedName("_id")
    val id: String,
    @SerializedName("isFileAttached")
    val isFileAttached: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("taskId")
    val taskId: String?
) : Parcelable

@Parcelize
@Keep
data class InvitedNumbers(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("surName")
    val surName: String
) : Parcelable