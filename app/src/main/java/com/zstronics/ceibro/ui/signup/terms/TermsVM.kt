package com.zstronics.ceibro.ui.signup.terms

import android.os.Bundle
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TermsVM @Inject constructor(
    override val viewState: TermsState,
) : HiltBaseViewModel<ITerms.State>(), ITerms.ViewModel {

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        with(viewState) {
            phoneNumber.value = bundle?.getString("phoneNumber")
            phoneCode.value = bundle?.getString("phoneCode")
        }
    }

}