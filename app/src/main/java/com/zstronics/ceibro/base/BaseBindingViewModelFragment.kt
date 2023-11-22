package com.zstronics.ceibro.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.activity.BaseActivity
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.interfaces.CanHandleOnClick
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.OnClickHandler
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseBindingViewModelFragment<VB : ViewDataBinding, VS : IBase.State, VM : HiltBaseViewModel<VS>> :
    BaseViewModelFragment<VS, VM>(), CanHandleOnClick {

    lateinit var mViewDataBinding: VB
        private set

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return setupBindingView(inflater, container, layoutResId) {

//            performDataBinding(savedInstanceState)
            //it.setVariable(brViewVariableId, this)
//            it.setVariable(bindingVariableId, viewModel)
            //  it.setVariable(brViewStateVariableId, viewModel.value.viewState)
//            it.lifecycleOwner = this.viewLifecycleOwner
            //  _binding = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val wasViewCreated = isViewCreated
        isViewCreated = true

        // performing the initialization only in cases when the view was created for the first time
        if (!wasViewCreated) {
            init(savedInstanceState)
            postInit()
        }
        performDataBinding(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startObservingInternetConnection()
    }

    private fun startObservingInternetConnection() {
        if (CeibroApplication.isNetworkObserverRegistered) return
        CeibroApplication.isNetworkObserverRegistered = true
        viewModel.viewModelScope.launch {
            networkConnectivityObserver.observe()
                .distinctUntilChanged()
                .collect { connectionStatus ->
                    when (connectionStatus) {
                        NetworkConnectivityObserver.Status.Available -> {
                            viewModel.syncDraftTask(requireContext())
                        }

                        else -> {
                        }
                    }
                }
        }
    }

    override fun performDataBinding(savedInstanceState: Bundle?) {
        registerStateListeners()
        viewModel.fetchExtras(arguments)
        //viewModel.c = requireContext()
        if (bindingViewStateVariableId <= 0)
            throw IllegalArgumentException("The state  binding variable should not null or zero. Check your fragment ${javaClass.simpleName}. Fragment should override bindingViewStateVariableId and provide valid binding variable for state ")
        mViewDataBinding.setVariable(bindingViewStateVariableId, viewModel.viewState)
        if (bindingVariableId <= 0)
            throw IllegalArgumentException("The state  binding variable should not null or zero. Check your fragment ${javaClass.simpleName}. Fragment should override bindingVariableId and provide valid binding variable for viewModel")
        mViewDataBinding.setVariable(bindingVariableId, viewModel)
        mViewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        mViewDataBinding.executePendingBindings()
        if (viewModel is
                    IValidator
        ) {
            (viewModel as IValidator).validator?.targetViewBinding = mViewDataBinding
        }
        if (viewModel is OnClickHandler) {
            viewModel.clickEvent?.observe(viewLifecycleOwner, { onClick(it) })
        }
        postExecutePendingBindings(savedInstanceState)
        viewModel.onCreate(arguments)
    }

    private fun setupBindingView(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        layoutResId: Int,
        set: (VB) -> Unit
    ): View {
        mViewDataBinding =
            DataBindingUtil.inflate<VB>(layoutInflater, layoutResId, container, false).also {
                set(it)
            }
        return mViewDataBinding.root
    }

    fun setupToolbar(
        toolbar: Toolbar?,
        toolbarMenu: Int? = null, setActionBar: Boolean = true,
        navigationOnClickListener: ((View) -> Unit?)? = null
    ) {
        toolbar?.apply {
            title = ""
            setHomeAsUpIndicator()?.let { setNavigationIcon(it) }
            (activity as BaseActivity).setSupportActionBar(this)
            navigationOnClickListener?.let { l -> this.setNavigationOnClickListener { l.invoke(it) } }
            if (setActionBar) {
                (activity as BaseActivity).supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(setDisplayHomeAsUpEnabled() ?: true)
                    setHomeButtonEnabled(setDisplayHomeAsUpEnabled() ?: true)
                    setDisplayShowCustomEnabled(setDisplayHomeAsUpEnabled() ?: true)
                    setHomeAsUpIndicator()?.let { setHomeAsUpIndicator(it) }
                }
            }
            toolbarMenu?.let { this.inflateMenu(it) }
        }
    }

    private fun registerStateListeners() {
        viewModel.registerLifecycleOwner(this)
    }

    private fun unregisterStateListeners() {
        viewModel.unregisterLifecycleOwner(this)
    }

    override fun onDestroyView() {
        unregisterStateListeners()
        hideKeyboard()
        super.onDestroyView()
    }

    override fun getFragmentResult(): Pair<String?, Bundle?>? = null
}
