package com.zstronics.ceibro.ui.chat.chatmsgview

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yap.permissionx.PermissionX
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.messages.SocketReceiveMessageResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.AllMessageSeenSocketResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.MessageSeenSocketResponse
import com.zstronics.ceibro.databinding.FragmentMsgViewBinding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.chat.adapter.MessagesAdapter
import com.zstronics.ceibro.ui.chat.adapter.swipe.MessageSwipeController
import com.zstronics.ceibro.ui.chat.adapter.swipe.SwipeControllerActions
import com.zstronics.ceibro.ui.chat.bottomsheet.FragmentMessageActionSheet
import com.zstronics.ceibro.ui.enums.EventType
import com.zstronics.ceibro.ui.enums.MessageActions.*
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import io.socket.emitter.Emitter
import okhttp3.internal.immutableListOf
import javax.inject.Inject


@AndroidEntryPoint
class MsgViewFragment :
    BaseNavViewModelFragment<FragmentMsgViewBinding, IMsgView.State, MsgViewVM>(),
    Emitter.Listener {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MsgViewVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_msg_view
    override fun toolBarVisibility(): Boolean = false

    @Inject
    lateinit var adapter: MessagesAdapter

    override fun onClick(id: Int) {
        when (id) {
            R.id.sendMessageButton -> {
                viewModel.composeAndSendMessage(viewState.messageBoxBody.value, adapter) {
                    scrollToPosition(it)
                }
            }
            R.id.msgViewGroupOrUserNameLayout -> navigateToGroupInfo()
            R.id.closeBtn -> navigateBack()
            R.id.cancelQuoted -> {
                viewModel.hideQuoted()
            }
            R.id.btPickFile -> checkPermission(
                immutableListOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            ) {
                chooseFile(
                    arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg",
                        "image/*"
                    )
                )
            }
            R.id.questionLL -> navigateToQuestionarieNavGraph()
        }
    }

    private fun checkPermission(permissionsList: List<String>, function: () -> Unit) {
        PermissionX.init(this).permissions(
            permissionsList
        ).explainReasonBeforeRequest().onExplainRequestReason { scope, deniedList, beforeRequest ->
            if (beforeRequest)
                scope.showRequestReasonDialog(
                    deniedList,
                    "${getString(R.string.common_text_permission)}",
                    getString(R.string.common_text_allow),
                    getString(R.string.common_text_deny)
                )
        }.onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                permissions = deniedList,
                message = getString(R.string.message_camera_permission_denied),
                positiveText = getString(R.string.open_setting), cancelAble = true
            )
        }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    function.invoke()
                } else {
                    toast(getString(R.string.common_text_permissions_denied))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            initRecyclerView(adapter)

            viewModel.chatMessages.observe(viewLifecycleOwner) {
                adapter.setList(it)
                fastScrollToPosition(it.size - 1)
            }
            adapter.itemClickListener =
                { _: View, position: Int, data: MessagesResponse.ChatMessage ->

                }

            adapter.itemLongClickListener =
                { _: View, position: Int, data: MessagesResponse.ChatMessage ->
                    showMessageActionSheet(data, position)
                }


            adapter.quotedClickListener =
                { _: View, position: Int, data: MessagesResponse.ReplyOf? ->
                    val chatMessages = viewModel.chatMessages.value

                    val quotedMessage = chatMessages?.find { it.id == data?.id }
                    if (quotedMessage != null) {
                        val index = chatMessages.indexOf(quotedMessage)
                        fastScrollToPosition(index)
                        mViewDataBinding.messagesRV.postDelayed(Runnable {
                            val viewHolder =
                                mViewDataBinding.messagesRV.findViewHolderForLayoutPosition(index)
                            val view = viewHolder?.itemView
                            val expandIn: Animation = AnimationUtils.loadAnimation(
                                mViewDataBinding.messagesRV.context,
                                R.anim.modal_in
                            )
                            view?.startAnimation(expandIn)
                        }, 200)
                    }
                }
            adapter.quickReplyClick =
                { _: View, _: Int, data: MessagesResponse.ChatMessage, messageBody: String ->
                    viewModel.showQuoted()
                    viewState.quotedMessage.value = data

                    val messageRes: MessagesResponse.ChatMessage =
                        composeLocalMessage(data, messageBody)
                    viewModel.sendMessage(messageBody, viewModel.chatRoom?.id)

                    adapter.appendMessage(messageRes) { lastPosition ->
                        scrollToPosition(lastPosition)
                    }

                    viewModel.hideQuoted()
                    viewModel.appendMessageInMessagesList(messageRes)
                }

            val messageSwipeController = MessageSwipeController(requireContext(), object :
                SwipeControllerActions {
                override fun showReplyUI(position: Int) {
                    viewModel.chatMessages.value?.let {
                        println("Position for array $position")
                        showQuotedMessage(it[position])
                    }
                }
            })
            val itemTouchHelper = ItemTouchHelper(messageSwipeController)
            itemTouchHelper.attachToRecyclerView(mViewDataBinding.messagesRV)
            SocketHandler.getSocket()
                .on(SocketHandler.CHAT_EVENT_REP_OVER_SOCKET, this@MsgViewFragment)

            setupMessageInput()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.chatRoom = null
        SocketHandler.getSocket()
            .off(SocketHandler.CHAT_EVENT_REP_OVER_SOCKET, this@MsgViewFragment)
    }

    private fun setupMessageInput() {
        with(mViewDataBinding.etMsgTypingField) {
            inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            imeOptions = EditorInfo.IME_ACTION_SEND
            setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    val message: String = text.toString()
                    if (!TextUtils.isEmpty(message))
                        viewModel.composeAndSendMessage(message, adapter) {
                            scrollToPosition(it)
                        }
                    return@OnEditorActionListener true
                }
                false
            })
        }

    }

    private fun initRecyclerView(adapter: MessagesAdapter) {
        with(mViewDataBinding.messagesRV) {
            this.adapter = adapter
            addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (bottom < oldBottom) {
                    postDelayed({
                        scrollToPosition((this.adapter as MessagesAdapter).itemCount - 1)
                    }, 100)
                }
            }
        }
    }

    private fun scrollToPosition(position: Int) {
        if (position > 0)
            mViewDataBinding.messagesRV.smoothScrollToPosition(position)
    }

    private fun fastScrollToPosition(position: Int) {
        if (position > 0)
            mViewDataBinding.messagesRV.scrollToPosition(position)
    }

    private fun showQuotedMessage(message: MessagesResponse.ChatMessage) {
        mViewDataBinding.etMsgTypingField.requestFocus()
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(
            mViewDataBinding.etMsgTypingField,
            InputMethodManager.SHOW_IMPLICIT
        )
        viewState.quotedMessage.value = message
        viewModel.showQuoted()
    }

    private fun showMessageActionSheet(message: MessagesResponse.ChatMessage, position: Int) {
        val sheet = FragmentMessageActionSheet()
        sheet.actionClick = { view, data ->
            when (data) {
                REPLY_TO_MESSAGE -> showQuotedMessage(message)
                VIEW_CHAT_MEMBERS -> navToChatMembers(message)
                VIEW_MESSAGE_INFO -> navToMessageInfo(message, position)
                FORWARD_MESSAGE -> {}
                MAKE_A_TASK -> {}
                ADD_SUBTASK_TO_EXISTING_TASK -> {}
                ADD_TEMP_MEMBER -> {}
                DELETE_CONVERSATION -> {}
                NO_ACTION -> {}
            }
        }
        sheet.show(childFragmentManager, "FragmentMessageActionSheet")
        //Delay is used so that the sheet binding object gets initialized and then change the visibility
        Handler().postDelayed({
            if (sheet.isVisible) {
                if (message.sender.id != viewModel.userId) {
                    sheet.binding.chatMsgMessageInfo.visibility = View.GONE
                }
            }
        }, 90)
    }

    private fun navToMessageInfo(message: MessagesResponse.ChatMessage, position: Int) {
        arguments?.putParcelable("message", message)
        navigate(R.id.action_msgViewFragment_to_messageInfoFragment, arguments)
    }

    private fun navToChatMembers(message: MessagesResponse.ChatMessage) {
        navigate(R.id.action_msgViewFragment_to_chatMembersFragment, arguments)
    }

    private fun navigateToGroupInfo() {
        navigate(R.id.action_msgViewFragment_to_groupInfoFragment, arguments)
    }

    private fun navigateToQuestionarieNavGraph() =
        navigate(R.id.action_msgViewFragment_to_questionarie_nav_graph, arguments)

    private fun chooseFile(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            "Send File ", mimeTypes,
            completionHandler = fileCompletionHandler
        )
    }

    private var fileCompletionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = { _, intent ->
        val file = FileUtils.getFile(requireContext(), intent?.data)
        viewModel.composeFileMessageToSend(file)
    }

    override fun call(vararg args: Any?) {
        when {
            args[0].toString().contains(EventType.RECEIVE_MESSAGE.name) -> {
                println("RECEIVE_MESSAGE")
                val newMessage: SocketReceiveMessageResponse =
                    Gson().fromJson(
                        args[0].toString(),
                        object : TypeToken<SocketReceiveMessageResponse>() {}.type
                    )

                if (newMessage.data.messageData.chat == viewModel.chatRoom?.id && newMessage.data.messageData.from != viewModel.userId) {
                    viewModel.launch(Dispatcher.Main) {
                        adapter.appendMessage(newMessage.data.messageData.message) { lastPosition ->
                            scrollToPosition(lastPosition)
                        }
                        viewModel.addMessageToMutableMessageList(newMessage.data.messageData.message)

                        viewModel.readMessage(
                            messageId = newMessage.data.messageData.message.id,
                            roomId = newMessage.data.messageData.message.chat,
                            eventType = EventType.MESSAGE_SEEN
                        )
                    }
                }
            }
            args[0].toString().contains(EventType.MESSAGE_SEEN.name) -> {
                println("MESSAGE_SEEN")
                val messageSeen: MessageSeenSocketResponse =
                    Gson().fromJson(
                        args[0].toString(),
                        object : TypeToken<MessageSeenSocketResponse>() {}.type
                    )
                viewModel.updateOtherLastMessageSeen(messageSeen)
            }
            args[0].toString().contains(EventType.ALL_MESSAGE_SEEN.name) -> {
//                val gson = Gson()
//                val messageType = object : TypeToken<AllMessageSeenSocketResponse>() {}.type
//                val message: AllMessageSeenSocketResponse =
//                    gson.fromJson(args[0].toString(), messageType)
            }
        }
    }
}