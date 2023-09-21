package com.zstronics.ceibro.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfileFragment :
    BaseNavViewModelFragment<FragmentProfileBinding, IProfile.State, ProfileVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProfileVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_profile
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.userProfileConnections -> navigateToConnections()
            R.id.userProfileInvitations -> navigateToInvitations()
            R.id.editProfileBtn -> navigateToEditProfile()
            107 -> navigateToAdminsPanel()
            R.id.logoutBtn -> {
                viewModel.endUserSession(requireContext())
                launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                    options = Bundle(),
                    clearPrevious = true
                ) {
                    putExtra(NAVIGATION_Graph_ID, R.navigation.onboarding_nav_graph)
                    putExtra(
                        NAVIGATION_Graph_START_DESTINATION_ID,
                        R.id.loginFragment
                    )
                }
                Thread { activity?.let { Glide.get(it).clearDiskCache() } }.start()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateVersionUI()
    }

    private fun navigateToEditProfile() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = false
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.editProfileFragment
            )
        }
    }

    private fun navigateToConnections() {
        navigate(R.id.connectionsFragment)
    }

    private fun navigateToInvitations() {
        navigate(R.id.invitationsFragment)
    }

    private fun navigateToAdminsPanel() {
        navigate(R.id.mainAdminFragment)
    }

    private fun populateVersionUI() {
        if (BuildConfig.DEBUG && BuildConfig.FLAVOR != "live") {
            mViewDataBinding.versionTv.visible()
            mViewDataBinding.versionTv.text =
                "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) - ${BuildConfig.FLAVOR}Flavour"
        }
    }

}