package com.zstronics.ceibro.ui.chat.individualchat

import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SingleNewChatVM @Inject constructor(
    override val viewState: SingleNewChatState,
    private val dashboardRepository: DashboardRepository,
    private val chatRepository: IChatRepository
) : HiltBaseViewModel<ISingleNewChat.State>(), ISingleNewChat.ViewModel {

    private val _allConnections: MutableLiveData<MutableList<MyConnection>> = MutableLiveData()
    val allConnections: LiveData<MutableList<MyConnection>> = _allConnections

    private val _selectedMember: MutableLiveData<String?> = MutableLiveData()
    val selectedMember: LiveData<String?> = _selectedMember

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        loadConnections()
    }
    override fun loadConnections() {
        launch {
            loading(true)
            when (val response = dashboardRepository.getAllConnections()) {
                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    _allConnections.postValue(data.myConnections as MutableList<MyConnection>?)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

    fun onMemberSelection(connection: MyConnection) {
        val id = if (connection.sentByMe)
            connection.to.id
        else
            connection.from?.id
        _selectedMember.value = id
    }

    override fun createIndividualChat() {
        launch {
            loading(true)
            when (val response =
                chatRepository.createIndividualChat(selectedMember.value.toString())) {
                is ApiResponse.Success -> {
                    val data = response.data.newChat
//                    println("newChatGroup: ${response.data.newChat}")
                    loading(false)
                    val handler = Handler()
                    handler.postDelayed(Runnable {
                        clickEvent?.postValue(111)
                    }, 60)

                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }
}