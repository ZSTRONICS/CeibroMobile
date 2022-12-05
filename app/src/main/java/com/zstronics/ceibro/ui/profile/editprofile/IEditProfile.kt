package com.zstronics.ceibro.ui.profile.editprofile

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IEditProfile {
    interface State : IBase.State {
        var userFirstName: MutableLiveData<String>
        var userSurname: MutableLiveData<String>
        var userEmail: MutableLiveData<String>
        var userContactNumber: MutableLiveData<String>
        var userPassword: MutableLiveData<String>
        var userConfirmPassword: MutableLiveData<String>
        var userCompanyName: MutableLiveData<String>
        var userCompanyVAT: MutableLiveData<String>
        var userCompanyLocation: MutableLiveData<String>
        var userCompanyContactNo: MutableLiveData<String>
        var userCompanyWorkEmail: MutableLiveData<String>
        var currentlyRepresenting: MutableLiveData<Boolean>
        var userProfilePic: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun onUpdate()
        fun updateProfile(firstName: String, surname: String, email: String, contactNo: String, password: String, confirmPassword: String, companyName: String
                          , companyVAT: String, companyLocation: String, companyContactNo: String, companyWorkEmail: String, currentlyRepresenting: Boolean?)
    }
}