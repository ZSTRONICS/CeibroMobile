package com.zstronics.ceibro.ui.admin.personaldetail

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IPersonalDetail {
    interface State : IBase.State {
        var userProfilePic: MutableLiveData<String>
        var userFirstName: MutableLiveData<String>
        var userSurname: MutableLiveData<String>
        var userEmail: MutableLiveData<String>
        var userContactNumber: MutableLiveData<String>
        var userCompanyName: MutableLiveData<String?>
        var userWorkContactNo: MutableLiveData<String>
        var userWorkEmail: MutableLiveData<String>
        var userRegisterDate: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}