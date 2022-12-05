package com.zstronics.ceibro.base.navgraph.host

import android.os.Bundle
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class NavHostPresenterVM @Inject constructor(override val viewState: NavHostPresenterState) :

    HiltBaseViewModel<INavHostPresenter.State>(), INavHostPresenter.ViewModel {

    override fun onFirsTimeUiCreate(bundle: Bundle?) {

    }

    override fun handleOnClick(id: Int) {
    }

}