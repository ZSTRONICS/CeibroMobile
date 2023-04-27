package com.zstronics.ceibro.ui.signup.register

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterVM @Inject constructor(
    override val viewState: RegisterState,
) : HiltBaseViewModel<IRegister.State>(), IRegister.ViewModel {
}