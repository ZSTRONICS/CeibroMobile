package com.zstronics.ceibro.ui.signup.terms

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TermsVM @Inject constructor(
    override val viewState: TermsState,
) : HiltBaseViewModel<ITerms.State>(), ITerms.ViewModel {
}