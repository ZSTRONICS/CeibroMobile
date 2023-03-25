package com.zstronics.ceibro.ui.chat.chatmsgview

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.com.burhanrashid52.photoediting.EditImageActivity
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.messages.SocketReceiveMessageResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.MessageSeenSocketResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
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
            R.id.captureImage -> {
                captureImage { uri ->
                    startPhotoEditor()
                }
            }
            R.id.btPickFile -> checkPermission(
                immutableListOf(
                    Manifest.permission.CAMERA
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
//            R.id.questionLL -> navigateToQuestionarieNavGraph()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            initRecyclerView(adapter)

            viewModel.chatMessages.observe(viewLifecycleOwner) {
                if (it != null) {
                    adapter.setList(it)
                }

                /*
                    Saving the position of from which position new messages started loading.
                    based on currentPositionWhenLoadingMore scrollToPosition will receive the viewModel.MESSAGES_LIMIT + 3 from
                    the top because the list is not reverted so we need to maintain the position by this way.
                    3 is the number of messages to scroll down till.
                 */

                if (it != null) {
                    val position =
                        if (currentPositionWhenLoadingMore >= 0) currentPositionWhenLoadingMore + 2 else it.size - 1
                    if (position < it.size)
                        scrollToPosition(position, true)
                }
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
                        scrollToPosition(index, true)
                        mViewDataBinding.messagesRV.postDelayed({
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
            setupMessageInput()
            SocketHandler.getSocket()
                .on(SocketHandler.CHAT_EVENT_REP_OVER_SOCKET, this@MsgViewFragment)
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
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    when {
                        !recyclerView.canScrollVertically(-1) && dy < 0 -> {
                            //scrolled to TOP
                            if ((viewModel.chatMessages.value?.get(0)?.type ?: "") != "start-bot") {
                                viewModel.fetchMoreMessages()
                            }
                        }
                    }
                }
            })
        }
    }

    private fun scrollToPosition(position: Int, isFastScroll: Boolean = false) {
        if (position > 0)
            if (isFastScroll)
                mViewDataBinding.messagesRV.scrollToPosition(position)
            else
                mViewDataBinding.messagesRV.smoothScrollToPosition(position)
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
//        val imageUri: Uri? = intent?.data
//        arguments?.putParcelable("imageUri", imageUri);
//        navigate(R.id.action_msgViewFragment_to_photoEditorFragment, arguments)
    }

    override fun call(vararg args: Any?) {
        val socketEventTypeResponse: SocketEventTypeResponse = Gson().fromJson(
            args[0].toString(),
            object : TypeToken<SocketEventTypeResponse>() {}.type
        )
        when (socketEventTypeResponse.eventType) {
            EventType.RECEIVE_MESSAGE.name -> {
                val messageData = Gson().fromJson<SocketReceiveMessageResponse>(
                    args[0].toString(),
                    object : TypeToken<SocketReceiveMessageResponse>() {}.type
                ).data.messageData
                if (messageData.chat == viewModel.chatRoom?.id && messageData.from != viewModel.userId) {
                    viewModel.launch(Dispatcher.Main) {
                        adapter.appendMessage(messageData.message) { lastPosition ->
                            scrollToPosition(lastPosition)
                        }
                        viewModel.appendMessageInMessagesList(messageData.message)
                        viewModel.readMessage(
                            messageId = messageData.message.id,
                            roomId = messageData.message.chat,
                            eventType = EventType.MESSAGE_SEEN
                        )
                    }
                }
            }
            EventType.MESSAGE_SEEN.name -> {
                println("MESSAGE_SEEN")
                val messageSeen: MessageSeenSocketResponse =
                    Gson().fromJson(
                        args[0].toString(),
                        object : TypeToken<MessageSeenSocketResponse>() {}.type
                    )
                viewModel.updateOtherLastMessageSeen(messageSeen)
            }
            EventType.ALL_MESSAGE_SEEN.name -> {
                val messageSeen: MessageSeenSocketResponse =
                    Gson().fromJson(
                        args[0].toString(),
                        object : TypeToken<MessageSeenSocketResponse>() {}.type
                    )
                viewModel.updateOtherLastMessageSeen(messageSeen)
            }
        }
    }
    private fun startPhotoEditor() {
        launchActivity<EditImageActivity>(
            options = Bundle(),
            clearPrevious = false
        ) {
        }
    }
}