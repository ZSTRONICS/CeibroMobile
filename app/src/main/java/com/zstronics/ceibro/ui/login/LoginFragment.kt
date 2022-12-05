package com.zstronics.ceibro.ui.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment :
    BaseNavViewModelFragment<FragmentLoginBinding, ILogin.State, LoginVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: LoginVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_login
    override fun toolBarVisibility(): Boolean = false
    private var isPassShown = false
    override fun onClick(id: Int) {
        when (id) {
            100 -> navigateToDashboard()
            R.id.signUpTextBtn -> navigate(R.id.signUpFragment)
            R.id.loginPasswordEye -> {
                isPassShown = !isPassShown
                showOrHidePassword(isPassShown)
            }
        }
    }

    private fun showOrHidePassword(passShown: Boolean) {
        if (passShown) {
            mViewDataBinding.editTextPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            mViewDataBinding.loginPasswordEye.setImageResource(R.drawable.visibility_on)
        }
        else {
            mViewDataBinding.editTextPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            mViewDataBinding.loginPasswordEye.setImageResource(R.drawable.visibility_off)
        }
        mViewDataBinding.editTextPassword.setSelection(mViewDataBinding.editTextPassword.text.toString().length)
    }

    private fun navigateToDashboard() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.homeFragment
            )
        }
    }
}