package com.zstronics.ceibro.ui.profile.editprofile

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class EditProfileState @Inject constructor() : BaseState(), IEditProfile.State {
    override var userFirstName: MutableLiveData<String> = MutableLiveData("")
    override var userSurname: MutableLiveData<String> = MutableLiveData("")
    override var userEmail: MutableLiveData<String> = MutableLiveData("")
    override var userContactNumber: MutableLiveData<String> = MutableLiveData("")
    override var userPassword: MutableLiveData<String> = MutableLiveData("")
    override var userConfirmPassword: MutableLiveData<String> = MutableLiveData("")
    override var userCompanyName: MutableLiveData<String?> = MutableLiveData("")
    override var userCompanyVAT: MutableLiveData<String> = MutableLiveData("")
    override var userCompanyLocation: MutableLiveData<String> = MutableLiveData("")
    override var userCompanyContactNo: MutableLiveData<String> = MutableLiveData("")
    override var userCompanyWorkEmail: MutableLiveData<String> = MutableLiveData("")
    override var currentlyRepresenting: MutableLiveData<Boolean> = MutableLiveData(false)
    override var userProfilePic: MutableLiveData<String> = MutableLiveData("")
}