package com.zstronics.ceibro.ui.tasks.v2.newtask.drawing

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewDrawingV2VM @Inject constructor(
    override val viewState: ViewDrawingV2State,
    private val drawingPinsDao: DrawingPinsV2Dao,
) : HiltBaseViewModel<IViewDrawingV2.State>(), IViewDrawingV2.ViewModel {

    var _drawingFile: MutableLiveData<DrawingV2?> =
        MutableLiveData()
    val drawingFile: LiveData<DrawingV2?> = _drawingFile

    private val _existingDrawingPins: MutableLiveData<MutableList<CeibroDrawingPins>> =
        MutableLiveData()
    val existingDrawingPins: LiveData<MutableList<CeibroDrawingPins>> = _existingDrawingPins


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

    }

    fun getDrawingPins(drawingId: String) {
        launch {
            val drawingPins = drawingPinsDao.getAllDrawingPins(drawingId)
            if (drawingPins.isNotEmpty()) {
                _existingDrawingPins.postValue(drawingPins.toMutableList())
            } else {
                _existingDrawingPins.postValue(mutableListOf())
            }
        }
    }
}