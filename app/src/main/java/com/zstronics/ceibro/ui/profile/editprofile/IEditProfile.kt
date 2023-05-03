package com.zstronics.ceibro.ui.profile.editprofile

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IEditProfile {
    interface State : IBase.State {
        var userFirstName: MutableLiveData<String>
        var userSurname: MutableLiveData<String>
        var userEmail: MutableLiveData<String>
        var userPhoneNumber: MutableLiveData<String>
        var userCompanyName: MutableLiveData<String?>
        var userJobTitle: MutableLiveData<String?>
        var userProfilePic: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun updateProfile(firstName: String, surname: String, email: String, phoneNumber: String, companyName: String,
                          jobTitle: String, onProfileUpdated: () -> Unit)
        fun updateProfilePhoto(file: String, context: Context)
        fun changePassword(oldPassword: String, newPassword: String, onPasswordChanged: () -> Unit)
        fun changePhoneNumber(newNumber: String, countryCode: String, password: String, onNumberChanged: () -> Unit)
        fun changePhoneNumberVerifyOtp(newNumber: String, otp: String, onNumberVerified: () -> Unit)
        fun resendOtp(phoneNumber: String, onOtpResend: () -> Unit)
        fun endUserSession()
    }
}