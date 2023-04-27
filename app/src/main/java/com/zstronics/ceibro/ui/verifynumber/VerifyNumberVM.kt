package com.zstronics.ceibro.ui.verifynumber

import android.os.Bundle
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.ui.chat.extensions.getChatTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerifyNumberVM @Inject constructor(
    override val viewState: VerifyNumberState,
) : HiltBaseViewModel<IVerifyNumber.State>(), IVerifyNumber.ViewModel {


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        with(viewState) {
            previousFragment.value = bundle?.getString("fromFragment")
        }
    }

}