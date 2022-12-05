package com.zstronics.ceibro.base.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.base.interfaces.CanHandleOnClick
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.OnClickHandler
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel

abstract class BaseBindingViewModelActivity<VB : ViewDataBinding, VS : IBase.State, VM : HiltBaseViewModel<VS>> :
    BaseViewModelActivity<VS, VM>(), CanHandleOnClick {

    lateinit var mViewDataBinding: VB
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindingView(this, layoutResId) {
            mViewDataBinding = it
            performDataBinding(savedInstanceState)
        }
    }

    private fun setupBindingView(
        fragmentActivity: FragmentActivity,
        layoutResId: Int,
        set: (VB) -> Unit
    ): VB {
        return DataBindingUtil.setContentView<VB>(fragmentActivity, layoutResId).also {
            set(it)
        }
    }

    override fun performDataBinding(savedInstanceState: Bundle?) {

//        viewModel.value.fetchExtras(arguments)
        //viewModel.c = requireContext()
        registerStateListeners()
        if (bindingViewStateVariableId <= 0)
            throw IllegalArgumentException("The state  binding variable should not null or zero. Check your activity. Activity should override bindingViewStateVariableId and provide valid binding variable for state ")
        mViewDataBinding.setVariable(bindingViewStateVariableId, viewModel.viewState)
        if (bindingVariableId <= 0)
            throw IllegalArgumentException("The state  binding variable should not null or zero. Check your activity. Activity should override bindingVariableId and provide valid binding variable for viewModel")
        mViewDataBinding.setVariable(bindingVariableId, viewModel)
        mViewDataBinding.lifecycleOwner = this
        init(savedInstanceState)
        postInit()
        mViewDataBinding.executePendingBindings()
        if (viewModel is IValidator) {
            (viewModel as IValidator).validator?.targetViewBinding = mViewDataBinding
        }
        if (viewModel is OnClickHandler) {
            viewModel.clickEvent?.observe(this, { onClick(it) })
        }
        postExecutePendingBindings(savedInstanceState)
        viewModel.onCreate(intent.extras)
    }

    private fun registerStateListeners() {
        if (viewModel is HiltBaseViewModel<*>) {
            viewModel.registerLifecycleOwner(this)
        }
    }

    private fun unregisterStateListeners() {
        if (viewModel is HiltBaseViewModel<*>) {
            viewModel.unregisterLifecycleOwner(this)
        }
    }

    override fun onDestroy() {
        unregisterStateListeners()
        super.onDestroy()
    }

    override fun showToast(msg: String) {
        this.toast(msg)
    }
}
