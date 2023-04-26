package com.zstronics.ceibro.ui.forgotpassword

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordVM @Inject constructor(
    override val viewState: ForgotPasswordState,
) : HiltBaseViewModel<IForgotPassword.State>(), IForgotPassword.ViewModel {
}