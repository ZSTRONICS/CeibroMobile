package com.zstronics.ceibro.ui.chat.newchat

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.messages.NewGroupChatRequest
import com.zstronics.ceibro.databinding.FragmentNewChatBinding
import com.zstronics.ceibro.ui.questioner.createquestion.members.ParticipantsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NewChatFragment :
    BaseNavViewModelFragment<FragmentNewChatBinding, INewChat.State, NewChatVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: NewChatVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_new_chat
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            111, R.id.closeBtn -> navigateBack()
        }
    }

    lateinit var adapter: ParticipantsAdapter

    @Inject
    lateinit var groupsAdapter: GroupsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.projectNames.observe(viewLifecycleOwner) {
            val arrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    it
                )

            arrayAdapter.setDropDownViewResource(
                android.R.layout
                    .simple_spinner_dropdown_item
            )
            mViewDataBinding.spProjectSelect.adapter = arrayAdapter
            mViewDataBinding.startGroupChat.setOnClickListener { view ->
                val selectedMembers = adapter.dataList.filter { data -> data.isChecked }
                val members = selectedMembers.map { member -> member.id }
                val request = NewGroupChatRequest(
                    members = members,
                    name = viewState.name.value,
                    projectId = viewModel.projectId
                )
                viewModel.createGroupChat(request)
            }
        }

        mViewDataBinding.spProjectSelect.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    viewModel.onProjectSelect(position)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        viewModel.projectMembers.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        viewModel.projectGroups.observe(viewLifecycleOwner) {
            groupsAdapter.setList(it)
        }
        adapter = ParticipantsAdapter()
        mViewDataBinding.recyclerView.adapter = adapter
        mViewDataBinding.groupsRecyclerView.adapter = groupsAdapter
    }
}