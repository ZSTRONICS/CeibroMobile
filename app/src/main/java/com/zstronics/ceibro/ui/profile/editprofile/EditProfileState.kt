package com.zstronics.ceibro.ui.profile.editprofile

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class EditProfileState @Inject constructor() : BaseState(), IEditProfile.State {
    override var userFirstName: MutableLiveData<String> = MutableLiveData("")
    override var userSurname: MutableLiveData<String> = MutableLiveData("")
    override var userEmail: MutableLiveData<String> = MutableLiveData("")
    override var userPhoneNumber: MutableLiveData<String> = MutableLiveData("")
    override var userCompanyName: MutableLiveData<String?> = MutableLiveData("")
    override var userJobTitle: MutableLiveData<String?> = MutableLiveData("")
    override var userProfilePic: MutableLiveData<String> = MutableLiveData("")
}