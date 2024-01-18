package com.zstronics.ceibro.ui.tasks.v2.newtask.drawing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.isVisible
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.repos.location.MarkerPointsData
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentLocationsV2Binding
import com.zstronics.ceibro.databinding.FragmentViewDrawingV2Binding
import com.zstronics.ceibro.ui.locationv2.usage.AddLocationTask
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import stkq.draw.FiveTuple
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow

@AndroidEntryPoint
class ViewDrawingV2Fragment :
    BaseNavViewModelFragment<FragmentViewDrawingV2Binding, IViewDrawingV2.State, ViewDrawingV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ViewDrawingV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_view_drawing_v2
    private var layoutPressed = ""
    private var isKeyboardShown = false
    private val spinnerItems = arrayOf("Floor", "Kitchen", "Garden")
    private val filtersList: ArrayList<Pair<String, String>> = arrayListOf()

    private var inViewPinsList: MutableList<MarkerPointsData> = mutableListOf()
    private var addNewMarkerPoints: MutableList<FiveTuple<Float, Float, Float, Float, Float>> =
        mutableListOf()
    private var existingPointTapped: MutableList<Triple<Float, Float, Float>> =
        mutableListOf()
    private var loadExistingMarkerPoints: MutableList<CeibroDrawingPins> = mutableListOf()
    private val PIN_TAP_THRESHOLD = 6
    private var loadingOldData = true
    private var pdfFileLoaded = false

    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.pdfView.useBestQuality(true)

        if (viewModel.drawingFile.value == null) {
            mViewDataBinding.progressBar.visibility = View.GONE
        }

        viewModel.existingDrawingPins.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                try {
//                    val dummyJson =
//                        "{\"points\": [{\"type\": \"task\", \"xPoint\": 64.797615, \"yPoint\": 108.57685, \"width\": 1080, \"height\": 657}, {\"type\": \"task\", \"xPoint\": 133.198, \"yPoint\": 182.58434, \"width\": 1080, \"height\": 657}, {\"type\": \"task\", \"xPoint\": 858.5824, \"yPoint\": 437.82886, \"width\": 1080, \"height\": 657}]}"
//                    val data = Gson().fromJson(dummyJson, PinPointsData::class.java)
                    inViewPinsList.clear()
                    inViewPinsList = mutableListOf()
                    loadExistingMarkerPoints.addAll(it)
                    if (pdfFileLoaded) {
                        mViewDataBinding.pdfView.invalidate()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        viewModel.drawingFile.observe(viewLifecycleOwner) { drawing ->

            drawing?.let {
                mViewDataBinding.progressBar.visibility = View.VISIBLE
                mViewDataBinding.tvFloorName.text = "${it.floor.floorName} Floor"
                mViewDataBinding.tvFileName.text = "${it.fileName}"
                mViewDataBinding.tvGroupName.text = "${it.fileTag.toCamelCase()}"
//            mViewDataBinding.progressBar.isIndeterminate = true


                val file = File(it.uploaderLocalFilePath)

                mViewDataBinding.pdfView.fromFile(file)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .enableAntialiasing(true)
                    .onLoad {
                    }
                    .onTap { event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {

                            existingPointTapped.clear()

                            val pageWidth = mViewDataBinding.pdfView.measuredWidth
                            val pageHeight = mViewDataBinding.pdfView.measuredHeight

                            val zoom = mViewDataBinding.pdfView.zoom // Get the current zoom level

                            val normalizedX =
                                event.x / mViewDataBinding.pdfView.width * pageWidth / zoom
                            val normalizedY =
                                event.y / mViewDataBinding.pdfView.height * pageHeight / zoom

                            existingPointTapped.add(
                                Triple(
                                    normalizedX,
                                    normalizedY,
                                    zoom
                                )
                            )

                            mViewDataBinding.pdfView.invalidate()

                        }
                        false
                    }
                    .onLongPress { event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {

                            val pageWidth = mViewDataBinding.pdfView.measuredWidth
                            val pageHeight = mViewDataBinding.pdfView.measuredHeight

                            val zoom = mViewDataBinding.pdfView.zoom // Get the current zoom level

                            val normalizedX =
                                event.x / mViewDataBinding.pdfView.width * pageWidth / zoom
                            val normalizedY =
                                event.y / mViewDataBinding.pdfView.height * pageHeight / zoom

                            println("normalizedX ${normalizedX} PDFView onTap : ${event.x} / ${mViewDataBinding.pdfView.width} * ${pageWidth} / ${zoom}")
                            println("normalizedY${normalizedY} PDFView onTap : ${event.y} / ${mViewDataBinding.pdfView.height} * ${pageHeight} / ${zoom}")

                            addNewMarkerPoints.add(
                                FiveTuple(
                                    normalizedX,
                                    normalizedY,
                                    zoom,
                                    event.x,
                                    event.y
                                )
                            )

                            mViewDataBinding.pdfView.invalidate()

                        }
                    }
                    .onDraw { canvas, pageWidth, pageHeight, displayedPage ->
                        println("PDFView pageWidth: ${pageWidth} pageHeight: ${pageHeight} zoom: ${mViewDataBinding.pdfView.zoom}")
                        pdfFileLoaded = true
                        mViewDataBinding.progressBar.visibility = View.INVISIBLE

                        val matrixValues = FloatArray(9)
                        canvas.matrix.getValues(matrixValues)
                        var transX = matrixValues[Matrix.MTRANS_X]
                        var transY = matrixValues[Matrix.MTRANS_Y]

                        if (existingPointTapped.isNotEmpty()) {
                            val tappedPoints = existingPointTapped
                            for (tappedPoint in tappedPoints) {
                                existingPointTapped.clear()
                                existingPointTapped = mutableListOf()

                                canvas.matrix.getValues(matrixValues)
                                transX = matrixValues[Matrix.MTRANS_X]
                                transY = matrixValues[Matrix.MTRANS_Y]

                                //Store these x and y points to DB to load the points again on the file
                                val xPoint =
                                    tappedPoint.first - (transX / tappedPoint.third)         // we are doing minus because transX or transY are always in negative if zoomed
                                val yPoint =
                                    tappedPoint.second - (transY / tappedPoint.third)        //so, minus minus becomes plus (+), so we are actually doing addition


                                for (pinInfo in inViewPinsList) {

                                    if (pinInfo.loadedBitmap != null) {
                                        if (xPoint >= (pinInfo.xPointToDisplay - (pinInfo.loadedBitmap.width / 2) / tappedPoint.third) && xPoint <= (pinInfo.xPointToDisplay + (pinInfo.loadedBitmap.width / 2) / tappedPoint.third) &&
                                            yPoint >= (pinInfo.yPointToDisplay - (pinInfo.loadedBitmap.height / 2) / tappedPoint.third) && yPoint <= (pinInfo.yPointToDisplay + (pinInfo.loadedBitmap.height / 2) / tappedPoint.third)
                                        ) {
                                            shortToastNow("Existing Pin: ${pinInfo.loadedPinData?.taskData?.taskUID}")
                                            break
                                        }
                                    }
                                }
                            }
                        }

                        if (addNewMarkerPoints.isNotEmpty()) {
                            val samplePointsMark = addNewMarkerPoints
                            for (samplePoints in samplePointsMark) {

                                canvas.matrix.getValues(matrixValues)
                                transX = matrixValues[Matrix.MTRANS_X]
                                transY = matrixValues[Matrix.MTRANS_Y]

                                //Store these x and y points to DB to load the points again on the file
                                val xPoint =
                                    samplePoints.actualX - (transX / samplePoints.zoomLevel)         // we are doing minus because transX or transY are always in negative if zoomed
                                val yPoint =
                                    samplePoints.actualY - (transY / samplePoints.zoomLevel)        //so, minus minus becomes plus (+), so we are actually doing addition

                                println("${samplePoints.zoomLevel} PDFView pdfCanvas.transX: ${samplePoints.actualX} = ${transX} = ${xPoint} -> pdfCanvas.transY: ${samplePoints.actualY} = ${transY} = ${yPoint} -> EVENT.X: ${samplePoints.eventX} EVENT.Y= ${samplePoints.eventY}")

                                val pdfBounds =
                                    calculatePDFBounds(pageWidth, pageHeight, transX, transY)
                                if (samplePoints.eventX >= pdfBounds.left && samplePoints.eventX <= pdfBounds.right &&
                                    samplePoints.eventY >= pdfBounds.top && samplePoints.eventY <= pdfBounds.bottom
                                ) {      //if in bounds then marker is placed
                                    var isExistingPinTapped = false
                                    for (existingPin in inViewPinsList) {
                                        val distance = calculateDistance(
                                            existingPin.xPointToDisplay,
                                            existingPin.yPointToDisplay,
                                            xPoint,
                                            yPoint
                                        )
                                        if (distance < PIN_TAP_THRESHOLD) {
                                            println("PDFView distance: ${distance}")
                                            isExistingPinTapped = true
                                            shortToastNow("Existing point tapped")
//                                        Toast.makeText(this, "Existing point tapped", Toast.LENGTH_SHORT).show()
                                            break
                                        }
                                    }
                                    if (loadingOldData) {
                                        inViewPinsList.add(
                                            MarkerPointsData(
                                                xPointToDisplay = xPoint,
                                                yPointToDisplay = yPoint,
                                                actualEventX = samplePoints.eventX,
                                                actualEventY = samplePoints.eventY,
                                                isNewPoint = "",
                                                loadedPinData = null,
                                                loadedBitmap = null
                                            )
                                        )
                                    } else if (!isExistingPinTapped) {
                                        inViewPinsList.add(
                                            MarkerPointsData(
                                                xPointToDisplay = xPoint,
                                                yPointToDisplay = yPoint,
                                                actualEventX = samplePoints.eventX,
                                                actualEventY = samplePoints.eventY,
                                                isNewPoint = "new",
                                                loadedPinData = null,
                                                loadedBitmap = null
                                            )
                                        )
                                    }

                                }

                                val index = samplePointsMark.indexOf(samplePoints)
                                if (index == samplePointsMark.size - 1) {
                                    addNewMarkerPoints.clear()
//                            pdfView.invalidate()
                                }
                            }
                        }

                        if (loadExistingMarkerPoints.isNotEmpty()) {
                            val loadPointsMark = loadExistingMarkerPoints
                            for (loadPoints in loadPointsMark) {
                                val zoom =
                                    mViewDataBinding.pdfView.zoom // Get the current zoom level

                                canvas.matrix.getValues(matrixValues)
                                transX = matrixValues[Matrix.MTRANS_X]
                                transY = matrixValues[Matrix.MTRANS_Y]

                                val currentPageWidth = mViewDataBinding.pdfView.measuredWidth
                                val currentPageHeight = mViewDataBinding.pdfView.measuredHeight
                                val originalPageWidth = loadPoints.page_width
                                val originalPageHeight = loadPoints.page_height

                                val xScaleFactor = pageWidth / originalPageWidth
                                val yScaleFactor = pageHeight / originalPageHeight

                                val pointXOfCurrentDevice =
                                    (loadPoints.x_coord * xScaleFactor).toFloat()
                                val pointYOfCurrentDevice =
                                    (loadPoints.y_coord * yScaleFactor).toFloat()

                                println("PDFView currentPageWidth: ${pageWidth} currentPageHeight= ${pageHeight} originalPointWidth= ${originalPageWidth} originalPointHeight= ${originalPageHeight}")
                                println("PDFView x and y ScaleFactor: ${xScaleFactor} = ${yScaleFactor} pointXOfCurrentDevice= ${pointXOfCurrentDevice} ${loadPoints.x_coord} pointYOfCurrentDevice= ${pointYOfCurrentDevice} ${loadPoints.y_coord}")


                                val actualX = pointXOfCurrentDevice + transX
                                val actualY =
                                    pointYOfCurrentDevice + transY        // this will give actual y point because we were saving yPoint after zoom calculation

                                val normalizedX =
                                    actualX / mViewDataBinding.pdfView.width * mViewDataBinding.pdfView.measuredWidth / zoom
                                val normalizedY =
                                    actualY / mViewDataBinding.pdfView.height * mViewDataBinding.pdfView.measuredHeight / zoom

                                val xPoint =
                                    normalizedX - (transX / zoom)         // we are doing minus because transX or transY are always in negative if zoomed
                                val yPoint =
                                    normalizedY - (transY / zoom)        //so, minus minus becomes plus (+), so we are actually doing addition

                                println("PDFView pdfCanvas.transX: ${actualX} = ${normalizedX} = ${xPoint} = ${transX} -> pdfCanvas.transY: ${actualY} = ${normalizedY} = ${yPoint} = ${transY}")

                                inViewPinsList.add(
                                    MarkerPointsData(
                                        xPointToDisplay = xPoint,
                                        yPointToDisplay = yPoint,
                                        actualEventX = actualX,
                                        actualEventY = actualY,
                                        isNewPoint = "",
                                        loadedPinData = loadPoints,
                                        loadedBitmap = null
                                    )
                                )

                                val index = loadPointsMark.indexOf(loadPoints)
                                if (index == loadPointsMark.size - 1) {
                                    loadExistingMarkerPoints.clear()
//                            pdfView.invalidate()
                                }
                            }
                        }

                        loadingOldData = false


                        inViewPinsList.mapIndexed { index, marker ->
                            mapPdfCoordinatesToCanvas(
                                canvas,
                                marker,
                                index,
                                pageWidth,
                                pageHeight,
                                marker.loadedPinData
                            )
                        }
//                    for (marker in markers) {
//
////                    canvas.drawCircle(point.x, point.y, 10f, paint)
//                    }

                    }
                    .enableAnnotationRendering(true)
                    .load()

            }
        }

    }

    //First Solution
    private fun mapPdfCoordinatesToCanvas(
        canvas: Canvas,
        marker: MarkerPointsData,
        markerIndex: Int,
        pageWidth: Float,
        pageHeight: Float,
        pinData: CeibroDrawingPins?
    ) {
        val currentZoom =
            mViewDataBinding.pdfView.zoom // Use your method to get the current zoom level

        val matrixValues = FloatArray(9)
        canvas.matrix.getValues(matrixValues)
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]
//        println("PDFView transX: ${transX} -> transY: ${transY}")

        // Scale the point's coordinates by the zoom level
        val scaledX = (marker.xPointToDisplay * currentZoom)
        val scaledY = (marker.yPointToDisplay * currentZoom)
//        println("PDFView scaledX: ${scaledX} -> scaledY: ${scaledY} -> currentZoom: ${currentZoom}")

        val vectorDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.icon_pin_point_circle, null)
        val bitmap1 = Bitmap.createBitmap(55, 55, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(bitmap1)
        vectorDrawable?.setBounds(0, 0, tempCanvas.width, tempCanvas.height)
        vectorDrawable?.draw(tempCanvas)

        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap1,
            ((30 + (currentZoom * 3.5) + 3).toInt()),
            ((30 + (currentZoom * 3.5) + 3).toInt()),
            true
        )

        val adjustedX = scaledX - scaledBitmap.width / 2
        val adjustedY = scaledY - scaledBitmap.height / 2

//        canvas.drawCircle(adjustedX, adjustedY, 50, paint)
        if (pinData != null) {
            if (pinData.type.equals("task", true)) {
                mViewDataBinding.taskRootState.text = pinData.taskData.rootState.toCamelCase()
                mViewDataBinding.taskUID.text = pinData.taskData.taskUID

                mViewDataBinding.taskUID.background = if (pinData.taskData.rootState.equals(
                        TaskRootStateTags.FromMe.tagValue,
                        true
                    )
                ) {
                    if (pinData.taskData.isHiddenByMe) {
                        mViewDataBinding.taskRootState.text =
                            TaskRootStateTags.Hidden.tagValue.toCamelCase()
                        if (pinData.taskData.hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                            resources.getDrawable(R.drawable.status_ongoing_filled_with_border)
                        } else if (pinData.taskData.hiddenState.equals(
                                TaskStatus.DONE.name,
                                true
                            )
                        ) {
                            resources.getDrawable(R.drawable.status_done_filled_with_border)
                        } else {
                            resources.getDrawable(R.drawable.status_draft_outline)
                        }
                    } else if (pinData.taskData.creatorState.equals(
                            TaskStatus.CANCELED.name,
                            true
                        )
                    ) {
                        mViewDataBinding.taskRootState.text =
                            TaskRootStateTags.Canceled.tagValue.toCamelCase()
                        resources.getDrawable(R.drawable.status_cancelled_filled_with_border)
                    } else {
                        mViewDataBinding.taskRootState.text =
                            pinData.taskData.rootState.toCamelCase()
                        if (pinData.taskData.fromMeState.equals(TaskStatus.UNREAD.name, true)) {
                            resources.getDrawable(R.drawable.status_new_filled_with_border)
                        } else if (pinData.taskData.fromMeState.equals(
                                TaskStatus.ONGOING.name,
                                true
                            )
                        ) {
                            resources.getDrawable(R.drawable.status_ongoing_filled_with_border)
                        } else if (pinData.taskData.fromMeState.equals(
                                TaskStatus.DONE.name,
                                true
                            )
                        ) {
                            resources.getDrawable(R.drawable.status_done_filled_with_border)
                        } else {
                            resources.getDrawable(R.drawable.status_draft_outline)
                        }
                    }
                } else if (pinData.taskData.rootState.equals(
                        TaskRootStateTags.ToMe.tagValue,
                        true
                    )
                ) {
                    if (pinData.taskData.isHiddenByMe) {
                        mViewDataBinding.taskRootState.text =
                            TaskRootStateTags.Hidden.tagValue.toCamelCase()
                        if (pinData.taskData.hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                            resources.getDrawable(R.drawable.status_ongoing_filled_with_border)
                        } else if (pinData.taskData.hiddenState.equals(
                                TaskStatus.DONE.name,
                                true
                            )
                        ) {
                            resources.getDrawable(R.drawable.status_done_filled_with_border)
                        } else {
                            resources.getDrawable(R.drawable.status_draft_outline)
                        }
                    } else if (pinData.taskData.userSubState.equals(
                            TaskStatus.CANCELED.name,
                            true
                        )
                    ) {
                        mViewDataBinding.taskRootState.text =
                            TaskRootStateTags.Canceled.tagValue.toCamelCase()
                        resources.getDrawable(R.drawable.status_cancelled_filled_with_border)
                    } else {
                        mViewDataBinding.taskRootState.text =
                            pinData.taskData.rootState.toCamelCase()
                        if (pinData.taskData.toMeState.equals(TaskStatus.NEW.name, true)) {
                            resources.getDrawable(R.drawable.status_new_filled_with_border)
                        } else if (pinData.taskData.toMeState.equals(
                                TaskStatus.ONGOING.name,
                                true
                            )
                        ) {
                            resources.getDrawable(R.drawable.status_ongoing_filled_with_border)
                        } else if (pinData.taskData.toMeState.equals(TaskStatus.DONE.name, true)) {
                            resources.getDrawable(R.drawable.status_done_filled_with_border)
                        } else {
                            resources.getDrawable(R.drawable.status_draft_outline)
                        }
                    }
                } else if (pinData.taskData.rootState.equals(
                        TaskRootStateTags.Hidden.tagValue,
                        true
                    )
                ) {
                    if (pinData.taskData.hiddenState.equals(TaskStatus.CANCELED.name, true)) {
                        resources.getDrawable(R.drawable.status_cancelled_filled_with_border)
                    } else if (pinData.taskData.hiddenState.equals(TaskStatus.ONGOING.name, true)) {
                        resources.getDrawable(R.drawable.status_ongoing_filled_with_border)
                    } else if (pinData.taskData.hiddenState.equals(TaskStatus.DONE.name, true)) {
                        resources.getDrawable(R.drawable.status_done_filled_with_border)
                    } else {
                        resources.getDrawable(R.drawable.status_draft_outline)
                    }
                } else {
                    resources.getDrawable(R.drawable.status_draft_outline)
                }


                val taskBitmap = createBitmapForTaskView(mViewDataBinding.taskSmallView)

                val taskAdjustedX = scaledX - taskBitmap.width / 2
                val taskAdjustedY = scaledY - taskBitmap.height / 2
                canvas.drawBitmap(taskBitmap, taskAdjustedX, taskAdjustedY, null)
                inViewPinsList[markerIndex] =
                    MarkerPointsData(
                        xPointToDisplay = marker.xPointToDisplay,
                        yPointToDisplay = marker.yPointToDisplay,
                        actualEventX = marker.actualEventX,
                        actualEventY = marker.actualEventY,
                        isNewPoint = "",
                        loadedPinData = marker.loadedPinData,
                        loadedBitmap = taskBitmap
                    )
            }
        } else {
            canvas.drawBitmap(scaledBitmap, adjustedX, adjustedY, null)
        }

        if (marker.isNewPoint.equals("new", true)) {

            inViewPinsList[markerIndex] =
                MarkerPointsData(
                    xPointToDisplay = marker.xPointToDisplay,
                    yPointToDisplay = marker.yPointToDisplay,
                    actualEventX = marker.actualEventX,
                    actualEventY = marker.actualEventY,
                    isNewPoint = "",
                    loadedPinData = marker.loadedPinData,
                    loadedBitmap = marker.loadedBitmap
                )

            showNewPinDialog(markerIndex, marker, pageWidth, pageHeight)

        }
        mViewDataBinding.pdfView.matrix.mapPoints(matrixValues)
        mViewDataBinding.pdfView.matrix.set(canvas.matrix)
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    private fun drawTaskViewOnCanvas(
        canvas: Canvas,
        pinData: CeibroDrawingPins?,
        x: Float,
        y: Float
    ) {

        val bitmap = createBitmapForTaskView(mViewDataBinding.taskSmallView)
//        drawBitmapOnCanvas(canvas,bitmap, x, y)
        canvas.drawBitmap(bitmap, x, y, null)

    }

    private fun createBitmapForTaskView(layout: ConstraintLayout): Bitmap {
        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val width = layout.measuredWidth
        val height = layout.measuredHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        layout.layout(0, 0, width, height)
        layout.draw(canvas)

        return bitmap
    }

    private fun calculatePDFBounds(
        pageWidth: Float,
        pageHeight: Float,
        transX: Float,
        transY: Float
    ): RectF {
        val zoom = mViewDataBinding.pdfView.zoom // Get the current zoom level

        println("PDFView calculatePDFBounds : pdfWidth=${pageWidth} -- pdfHeight=${pageHeight} -- zoom=${zoom} -- transX=${transX} -- transY=${transY}")
        // Calculate the boundaries of the displayed PDF
        val left = transX / zoom // Calculate the left boundary
        var top = transY / zoom // Calculate the top boundary
        if (zoom > 1.05) {
            top = transY
        }
        val right = left + (pageWidth * zoom) // Calculate the right boundary
        var bottom = top + (pageHeight * zoom) // Calculate the bottom boundary
        if (zoom > 1.05) {
            bottom = (pageHeight + transY)
        }
        println("PDFView calculatePDFBounds : left=${left} -- top=${top} -- right=${right} -- bottom=${bottom}")

        return RectF(left, top, right, bottom)
    }


    private fun showNewPinDialog(
        markerIndex: Int,
        marker: MarkerPointsData,
        pageWidth: Float,
        pageHeight: Float
    ) {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext()).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = requireContext().resources.getString(R.string.do_you_want_to_place_pin_over_here_for_the_new_task)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.setCancelable(false)
        alertDialog.show()

        yesBtn.setOnClickListener {
            alertDialog.dismiss()
            val actualPageWidth = pageWidth / mViewDataBinding.pdfView.zoom
            val actualPageHeight = pageHeight / mViewDataBinding.pdfView.zoom

            val bitmap = Bitmap.createBitmap(
                mViewDataBinding.pdfView.width,
                mViewDataBinding.pdfView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            mViewDataBinding.pdfView.draw(canvas)

            // Save the bitmap to a file
            val file =
                File(context?.externalCacheDir, "drawing_SS_${System.currentTimeMillis()}.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()


            val locationTask = AddLocationTask(
                xCord = marker.xPointToDisplay,
                yCord = marker.yPointToDisplay,
                pageWidth = actualPageWidth,
                pageHeight = actualPageHeight,
                locationImgFile = file,
                drawingId = viewModel.drawingFile.value?._id,
                drawingName = file.name ?: "Unknown",
                projectId = viewModel.drawingFile.value?.projectId,
                groupId = viewModel.drawingFile.value?.groupId
            )

            val bundle = Bundle()
            bundle.putParcelable("newLocationTaskData", locationTask)
            navigateBackWithResult(Activity.RESULT_OK, bundle)
        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
            try {
                inViewPinsList.removeAt(markerIndex)
            } catch (_: Exception) {
            }
            mViewDataBinding.pdfView.invalidate()
        }
    }



    override fun onResume() {
        super.onResume()

        CookiesManager.drawingFileForNewTask.value?.let {
            viewModel.getDrawingPins(it._id)
            viewModel._drawingFile.postValue(it)
        } ?: run {
            shortToastNow("No file to display. Please select any drawing file.")
        }

    }


    // Capture screenshot of the view with zoom
    fun captureScreenshotWithZoom(point: PointF): File {
//        mViewDataBinding.pdfView.zoomCenteredTo(13.0f, point) // Adjust the zoom level as needed
        val bitmapWithZoom = Bitmap.createBitmap(
            mViewDataBinding.pdfView.width,
            mViewDataBinding.pdfView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmapWithZoom)
        mViewDataBinding.pdfView.draw(canvas)

        val imageFile = captureAndSaveScreenshot(
            bitmapWithZoom,
            "pdf_view_with_zoom_${System.currentTimeMillis()}.png"
        )
        return imageFile
    }

    // Capture screenshot of the full view without zoom
    fun captureScreenshotWithoutZoom(): File {
        mViewDataBinding.pdfView.zoomTo(1.1f)
        val bitmapWithoutZoom = Bitmap.createBitmap(
            mViewDataBinding.pdfView.width,
            mViewDataBinding.pdfView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmapWithoutZoom)
        mViewDataBinding.pdfView.draw(canvas)

        val imageFile = captureAndSaveScreenshot(
            bitmapWithoutZoom,
            "pdf_view_without_zoom_${System.currentTimeMillis()}.png"
        )
        return imageFile
    }

    // Function to capture screenshot and save as file
    private fun captureAndSaveScreenshot(bitmap: Bitmap, fileName: String): File {
        val file = File(context?.externalCacheDir, fileName)
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return file
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshDrawingPins(event: LocalEvents.RefreshDrawingPins?) {
        val pinData = event?.pinData
        if (pinData != null) {
            val drawingFile = viewModel.drawingFile.value
            if (drawingFile != null) {
                if (drawingFile._id == pinData.drawingId) {
                    viewModel.getDrawingPins(drawingFile._id)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateDrawingPins(event: LocalEvents.UpdateDrawingPins?) {
        val pinData = event?.pinData
        if (pinData != null) {
            val drawingFile = viewModel.drawingFile.value
            if (drawingFile != null) {
                if (drawingFile._id == pinData.drawingId) {
                    viewModel.getDrawingPins(drawingFile._id)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}