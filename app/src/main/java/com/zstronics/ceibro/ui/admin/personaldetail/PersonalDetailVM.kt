package com.zstronics.ceibro.ui.admin.personaldetail

import android.os.Bundle
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PersonalDetailVM @Inject constructor(
    override val viewState: PersonalDetailState,
) : HiltBaseViewModel<IPersonalDetail.State>(), IPersonalDetail.ViewModel {



    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val userData: AdminUsersResponse.AdminUserData? = bundle?.getParcelable("adminUserData")

        with(viewState) {
            if (userData != null) {
                userFirstName.value = userData.firstName
                userSurname.value = userData.surName
                userEmail.value = userData.email
                userContactNumber.value = userData.phone
                userCompanyName.value = userData.companyName
                userWorkEmail.value = userData.workEmail
                userWorkContactNo.value = userData.companyPhone
                userProfilePic.value = userData.profilePic
                userRegisterDate.value = DateUtils.reformatStringDate(
                    date = userData.createdAt,
                    DateUtils.SERVER_DATE_FULL_FORMAT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )
            }
        }
    }

}