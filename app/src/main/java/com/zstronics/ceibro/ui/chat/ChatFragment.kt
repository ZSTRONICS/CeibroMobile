package com.zstronics.ceibro.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.databinding.FragmentChatBinding
import com.zstronics.ceibro.ui.chat.adapter.ChatRoomAdapter
import com.zstronics.ceibro.ui.chat.bottomsheet.FragmentChatRoomActionSheet
import com.zstronics.ceibro.ui.enums.ChatActions.*
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment :
    BaseNavViewModelFragment<FragmentChatBinding, IChat.State, ChatVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ChatVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_chat
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.individual -> navigateToSingleNewChat()
            R.id.group -> navigateToNewChat()
            R.id.viewAllBtn -> viewModel.loadChat("all",false)
            R.id.unreadBtn -> viewModel.loadChat("unread",false)
            R.id.favBtnText -> viewModel.loadChat("favorites",true)
        }
    }

    @Inject
    lateinit var adapter: ChatRoomAdapter

    private fun navigateToNewChat() {
        navigate(R.id.newChatFragment)
    }
    private fun navigateToSingleNewChat() {
        navigate(R.id.action_chatFragment_to_singleNewChatFragment)
    }

    private fun navigateToMsgView(chat: ChatRoom) {
        val bundle = Bundle()
        bundle.putParcelable("chatRoom", chat)
        navigate(R.id.msgViewFragment, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*Set socket and establish connection*/
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        initRecyclerView(adapter)

        viewModel.chatRooms.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        adapter.itemClickListener = { _: View, position: Int, data: ChatRoom ->
            navigateToMsgView(data)
        }
        adapter.childItemClickListener = { view: View, position: Int, data: ChatRoom ->
            if (view.id == R.id.chatFavIcon)
                viewModel.addChatToFav(data.id)
        }

        SocketHandler.getSocket().on(SocketHandler.CHAT_EVENT_REP_OVER_SOCKET) { args ->
//            val gson = Gson()
//            val messageTYpe = object : TypeToken<SocketReceiveMessageResponse>() {}.type
//            val message: SocketReceiveMessageResponse = gson.fromJson(args[0].toString(), messageTYpe)
            viewModel.loadChat("all",false)
        }
    }

    private fun initRecyclerView(adapter: ChatRoomAdapter) {
        mViewDataBinding.chatRV.adapter = adapter

        adapter.itemLongClickListener =
            { _: View, _: Int, data: ChatRoom ->
                showChatActionSheet(data)
            }
    }

    private fun showChatActionSheet(chatRoom: ChatRoom) {
        val sheet = FragmentChatRoomActionSheet()
        sheet.actionClick = { view, data ->
            when (data) {
                MARK_AS_UNREAD -> {}
                MUTE_CHAT -> {}
                ADD_TO_FAV -> viewModel.addChatToFav(chatRoom.id)
                DELETE_CHAT -> {}
                NO_ACTION -> {}
            }
        }
        sheet.show(childFragmentManager, "FragmentChatRoomActionSheet")
    }
}