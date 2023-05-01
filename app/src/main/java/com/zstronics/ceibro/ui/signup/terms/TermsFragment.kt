package com.zstronics.ceibro.ui.signup.terms

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTermsBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsFragment :
    BaseNavViewModelFragment<FragmentTermsBinding, ITerms.State, TermsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TermsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_terms
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.termsConfirmBtn -> navigate(R.id.signUpFragment)
        }
    }
}