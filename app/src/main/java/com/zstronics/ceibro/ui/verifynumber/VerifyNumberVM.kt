package com.zstronics.ceibro.ui.verifynumber

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerifyNumberVM @Inject constructor(
    override val viewState: VerifyNumberState,
) : HiltBaseViewModel<IVerifyNumber.State>(), IVerifyNumber.ViewModel {
}