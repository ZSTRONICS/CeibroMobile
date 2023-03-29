package com.zstronics.ceibro.ui.admin.personaldetail

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class PersonalDetailState @Inject constructor() : BaseState(), IPersonalDetail.State {
    override var userProfilePic: MutableLiveData<String> = MutableLiveData("")
    override var userFirstName: MutableLiveData<String> = MutableLiveData("")
    override var userSurname: MutableLiveData<String> = MutableLiveData("")
    override var userEmail: MutableLiveData<String> = MutableLiveData("")
    override var userContactNumber: MutableLiveData<String> = MutableLiveData("")
    override var userCompanyName: MutableLiveData<String?> = MutableLiveData("")
    override var userWorkContactNo: MutableLiveData<String> = MutableLiveData("")
    override var userWorkEmail: MutableLiveData<String> = MutableLiveData("")
    override var userRegisterDate: MutableLiveData<String> = MutableLiveData("")
}