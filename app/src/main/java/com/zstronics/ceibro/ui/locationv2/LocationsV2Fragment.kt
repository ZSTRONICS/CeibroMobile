package com.zstronics.ceibro.ui.locationv2

import android.annotation.SuppressLint
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
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
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
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.repos.location.MarkerPointsData
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentLocationsV2Binding
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
class LocationsV2Fragment :
    BaseNavViewModelFragment<FragmentLocationsV2Binding, ILocationsV2.State, LocationsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: LocationsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_locations_v2
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

    companion object {

        private const val llTo = "llTo"
        private const val llFrom = "llFrom"
        private const val llHidden = "llHidden"
    }


    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                if (viewModel.cameFromProject) {
                    EventBus.getDefault().post(LocalEvents.LoadLocationProjectFragmentInLocation())
                } else {
                    EventBus.getDefault().post(LocalEvents.LoadDrawingFragmentInLocation())
                }
            }

            R.id.llCheckAll -> {
                mViewDataBinding.cbSelectAll.isChecked = !mViewDataBinding.cbSelectAll.isChecked
                if (mViewDataBinding.cbSelectAll.isChecked) {
                    addFiltersToList()
                } else {
                    removeFiltersToList()
                }
            }

            R.id.projectFilterBtn -> {
                viewState.isFilterVisible.value?.let { currentValue ->
                    val updateFlag = !currentValue
                    viewState.isFilterVisible.value = updateFlag
                    updateCompoundDrawable(
                        mViewDataBinding.filter,
                        mViewDataBinding.ivFilter,
                        updateFlag
                    )
                }
            }

            R.id.tvTo -> {
                layoutPressed = llTo
                if (isKeyboardShown) {
                    manageLayoutsVisibilityWithKeyboardShown()
                } else {
                    manageLayoutsVisibilityWithKeyboardHidden()
                }
            }

            R.id.tvfrom -> {
                layoutPressed = llFrom
                if (isKeyboardShown) {
                    manageLayoutsVisibilityWithKeyboardShown()
                } else {
                    manageLayoutsVisibilityWithKeyboardHidden()
                }
            }

            R.id.ToNewStateText -> {

                if (viewState.isToNewClicked.value == false) {
                    filtersList.add(
                        Pair(
                            TaskRootStateTags.ToMe.tagValue.lowercase(),
                            TaskStatus.NEW.name.lowercase()
                        )
                    )

                } else {

                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.ToMe.tagValue.lowercase(),
                            TaskStatus.NEW.name.lowercase()
                        )
                    )
                }
                viewState.isToNewClicked.value = !(viewState.isToNewClicked.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.ToOngoingStateText -> {

                if (viewState.isToOngoingClicked.value == false) {
                    filtersList.add(
                        Pair(
                            TaskRootStateTags.ToMe.tagValue.lowercase(),
                            TaskStatus.ONGOING.name.lowercase()
                        )
                    )
                } else {
                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.ToMe.tagValue.lowercase(),
                            TaskStatus.ONGOING.name.lowercase()
                        )
                    )
                }

                viewState.isToOngoingClicked.value = !(viewState.isToOngoingClicked.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.ToDoneStateText -> {

                if (viewState.isToDoneClicked.value == false) {
                    filtersList.add(
                        Pair(
                            TaskRootStateTags.ToMe.tagValue.lowercase(),
                            TaskStatus.DONE.name.lowercase()
                        )
                    )

                } else {
                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.ToMe.tagValue.lowercase(),
                            TaskStatus.DONE.name.lowercase()
                        )
                    )
                }
                viewState.isToDoneClicked.value = !(viewState.isToDoneClicked.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.fromUnredStateText -> {

                if (viewState.isFromUnreadClicked.value == false) {
                    filtersList.add(
                        Pair(
                            TaskRootStateTags.FromMe.tagValue.lowercase(),
                            TaskStatus.UNREAD.name.lowercase()
                        )
                    )

                } else {
                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.FromMe.tagValue.lowercase(),
                            TaskStatus.UNREAD.name.lowercase()
                        )
                    )
                }
                viewState.isFromUnreadClicked.value = !(viewState.isFromUnreadClicked.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.FromOngoingStateText -> {

                if (viewState.isFromOngoingClicked.value == false) {
                    filtersList.add(
                        Pair(
                            TaskRootStateTags.FromMe.tagValue.lowercase(),
                            TaskStatus.ONGOING.name.lowercase()
                        )
                    )
                } else {
                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.FromMe.tagValue.lowercase(),
                            TaskStatus.ONGOING.name.lowercase()
                        )
                    )
                }

                viewState.isFromOngoingClicked.value = !(viewState.isFromOngoingClicked.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.FromDoneStateText -> {

                if (viewState.isFromDoneClicked.value == false) {
                    filtersList.add(
                        Pair(
                            TaskRootStateTags.FromMe.tagValue.lowercase(),
                            TaskStatus.DONE.name.lowercase()
                        )
                    )

                } else {
                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.FromMe.tagValue.lowercase(),
                            TaskStatus.DONE.name.lowercase()
                        )
                    )
                }
                viewState.isFromDoneClicked.value = !(viewState.isFromDoneClicked.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.hiddenOngoingStateText -> {

                if (viewState.isHiddenOngoingClicked.value == false) {

                    filtersList.add(
                        Pair(
                            TaskRootStateTags.Hidden.tagValue.lowercase(),
                            TaskStatus.ONGOING.name.lowercase()
                        )
                    )

                } else {
                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.Hidden.tagValue.lowercase(),
                            TaskStatus.ONGOING.name.lowercase()
                        )
                    )
                }
                viewState.isHiddenOngoingClicked.value = !(viewState.isHiddenOngoingClicked.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.hiddenDoneStateText -> {


                if (viewState.isHiddenDoneClicked.value == false) {
                    filtersList.add(
                        Pair(
                            TaskRootStateTags.Hidden.tagValue.lowercase(),
                            TaskStatus.DONE.name.lowercase()
                        )
                    )
                } else {
                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.Hidden.tagValue.lowercase(),
                            TaskStatus.DONE.name.lowercase()
                        )
                    )
                }

                viewState.isHiddenDoneClicked.value = !(viewState.isHiddenDoneClicked.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.hiddenCancelledStateText -> {


                if (viewState.isHiddenCancelled.value == false) {
                    filtersList.add(
                        Pair(
                            TaskRootStateTags.Hidden.tagValue.lowercase(),
                            TaskStatus.CANCELED.name.lowercase()
                        )
                    )
                } else {
                    filtersList.remove(
                        Pair(
                            TaskRootStateTags.Hidden.tagValue.lowercase(),
                            TaskStatus.CANCELED.name.lowercase()
                        )
                    )
                }
                viewState.isHiddenCancelled.value = !(viewState.isHiddenCancelled.value!!)
                isAnyConditionFalse()
                viewModel.checkFilter(filtersList);
            }

            R.id.tvHidden -> {
                layoutPressed = llHidden
                if (isKeyboardShown) {
                    manageLayoutsVisibilityWithKeyboardShown()
                } else {
                    manageLayoutsVisibilityWithKeyboardHidden()
                }
            }

            R.id.cancelSearch -> {

                isKeyboardShown = false
                mViewDataBinding.projectSearchBar.hideKeyboard()
                mViewDataBinding.projectsSearchCard.visibility = View.GONE
                mViewDataBinding.projectSearchBtn.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBar.setQuery("", false)
            }

            R.id.projectSearchBtn -> {
                showKeyboard()
                isKeyboardShown = true
                Handler(Looper.getMainLooper()).postDelayed({
                    checkLayoutsVisibility()
                }, 100)
                mViewDataBinding.projectSearchBar.requestFocus()
                mViewDataBinding.projectsSearchCard.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBtn.visibility = View.INVISIBLE
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addFiltersToList()

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mViewDataBinding.locationSpinner.adapter = adapter
        mViewDataBinding.locationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    //  val selectedItem = spinnerItems[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        val rootView = requireView()
        rootView.viewTreeObserver.addOnPreDrawListener {
            val screenHeight = rootView.rootView.height
            val heightDiff = screenHeight - rootView.height
            val thresholdPercentage = 0.30 // Adjust as needed

            if (heightDiff >= screenHeight * thresholdPercentage) {
                isKeyboardShown = true
                checkLayoutsVisibility()
            } else {
                isKeyboardShown = false
            }

            true
        }

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
        viewModel.filterExistingDrawingPins.observe(viewLifecycleOwner) {
//            if (it.isNotEmpty()) {
            try {
                inViewPinsList.clear()
                inViewPinsList = mutableListOf()
                loadExistingMarkerPoints.addAll(it)
                if (pdfFileLoaded) {
                    mViewDataBinding.pdfView.invalidate()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
//            }
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
//                    try {
//                        val dummyJson =
//                            "{\"points\": [{\"type\": \"task\", \"xPoint\": 64.797615, \"yPoint\": 108.57685, \"width\": 1080, \"height\": 657}, {\"type\": \"task\", \"xPoint\": 133.198, \"yPoint\": 182.58434, \"width\": 1080, \"height\": 657}, {\"type\": \"task\", \"xPoint\": 858.5824, \"yPoint\": 437.82886, \"width\": 1080, \"height\": 657}]}"
//                        val data = Gson().fromJson(dummyJson, PinPointsData::class.java)
//                        val iterator = data.points.iterator()
//
//                        while (iterator.hasNext()) {
//                            val item = iterator.next()
//
//                            loadExistingMarkerPoints.add(
//                                FourTuple(
//                                    width = item.width,
//                                    height = item.height,
//                                    eventX = item.xPoint,
//                                    eventY = item.yPoint
//                                )
//                            )
//                        }
//                    } catch (e: Exception) {
//                        // Handle any exceptions that occur during JSON parsing or calculations
//                        e.printStackTrace()
//                    }
                        Handler(Looper.getMainLooper()).postDelayed({
//                    println("PDFView onLoad: called")
//                    val docWidth = pdfView.optimalPageWidth
//                    val docHeight = pdfView.optimalPageHeight
//
//                    val aspectRatio = docWidth.toFloat() / docHeight.toFloat()
//                    val adjustedHeight = (pdfView.width / aspectRatio).toInt()
//
//                    val params = pdfView.layoutParams
//                    params.height = adjustedHeight
//                    pdfView.layoutParams = params
//                    pdfView.requestLayout()
//                    pdfView.invalidate()


//                    data.points.map { points ->
//                        val pageWidth = pdfView.measuredWidth
//                        val pageHeight = pdfView.measuredHeight
//
//                        val zoom = pdfView.zoom // Get the current zoom level
//
//                        val normalizedX = points.xPoint / pdfView.width * pageWidth / zoom
//                        val normalizedY = points.yPoint / pdfView.height * pageHeight / zoom
//
//                        sampleMarkerPoints1.add(FiveTuple(normalizedX, normalizedY, zoom, points.xPoint, points.yPoint))
//                    }


//                    progressBar.visibility = View.INVISIBLE
//                    pdfView.invalidate()
                            println("PDFView loadExistingMarkerPoints: ${loadExistingMarkerPoints}")
                        }, 500)

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

//                        if (zoom == 12.0f) {
//                            mViewDataBinding.pdfView.zoomCenteredTo(14.0f, PointF(event.x, event.y))
//                        } else {
//                            mViewDataBinding.pdfView.zoomCenteredTo(12.0f, PointF(event.x, event.y))
//                        }

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
            val newX = adjustedX + transX + (scaledBitmap.width - 17)
            val newY = adjustedY + transY + (scaledBitmap.height - 17)

            println("PDFView adjustedX: ${scaledX}/ ${adjustedX}/ ${newX}/ ${transX} -> adjustedY: ${scaledY}/ ${adjustedY}/ ${newY}/ ${transY}")
//            taskPopupMenu(mViewDataBinding.pdfView, newX, newY, point)

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
//            val currentXOffset = mViewDataBinding.pdfView.currentXOffset
//            val currentYOffset = mViewDataBinding.pdfView.currentYOffset
//            val xMovementPoint = currentXOffset - (point.x / currentZoom)
//            val yMovementPoint = currentYOffset - (point.y / currentZoom)
//
//            println("PDFView SETTING-OFFSET-> XOffset= ${currentXOffset} / YOffset= ${currentYOffset} / point= ${point} / xMovementPoint= ${xMovementPoint} = ${yMovementPoint}")
//
//            mViewDataBinding.pdfView.zoomCenteredTo(currentZoom, point)

            showNewItemBottomSheet(markerIndex, marker, pageWidth, pageHeight)

//            for (marker in markers) {
//                if (marker.first == point.x && marker.second == point.y) {
//                    val index = markers.indexOf(Triple(marker.first, marker.second, "new"))
//                }
//            }
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


    private fun showNewItemBottomSheet(
        markerIndex: Int,
        marker: MarkerPointsData,
        pageWidth: Float,
        pageHeight: Float
    ) {
        val sheet = LocationNewItemBottomSheet()
        sheet.dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        sheet.onSheetDismiss = {
            try {
                inViewPinsList.removeAt(markerIndex)
            } catch (_: Exception) {
            }
            mViewDataBinding.pdfView.invalidate()
        }

        sheet.onAddTaskBtnClicked = {
//            shortToastNow("Coming Soon")
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
                File(context?.externalCacheDir, "pdf_view_SS_${System.currentTimeMillis()}.png")
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
            bundle.putParcelable("locationTaskData", locationTask)
            navigate(R.id.newTaskV2Fragment, bundle)
            sheet.dismiss()
        }
        sheet.isCancelable = true
        sheet.show(childFragmentManager, "ChangePasswordSheet")
    }


    @SuppressLint("SuspiciousIndentation")
    private fun taskPopupMenu(
        v: View,
        adjustedX: Float,
        adjustedY: Float,
        point: PointF
    ): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_new_task_point, null)

        //following code is to make popup at top if the view is at bottom
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        popupWindow.setOnDismissListener {
//            shortToastNow("Dismiss Listener")
//            for (marker in markers) {
//                if (marker.first == point.x && marker.second == point.y) {
//                    val index = markers.indexOf(Triple(marker.first, marker.second, "new"))
//                    markers[index] = Triple(marker.first, marker.second, "")
//                }
//            }
        }
        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
//        val height = displayMetrics.heightPixels * 2 / 3

//        if (positionOfIcon > height) {
//            popupWindow.showAsDropDown(v, 0, -375)
//        } else {
        popupWindow.showAsDropDown(v, adjustedX.toInt(), adjustedY.toInt())
//        }
//        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, adjustedX.toInt(), adjustedY.toInt())
        //////////////////////

//        val viewBtn = view.findViewById<LinearLayoutCompat>(R.id.viewBtn)
//
//        viewBtn.setOnClick {
//            navigateToPersonalDetails(data)
//            popupWindow.dismiss()
//        }

        return popupWindow
    }


    private fun manageLayoutsVisibilityWithKeyboardShown() {

        if (layoutPressed == llTo) {
            if (mViewDataBinding.llTo.isVisible()) {
                mViewDataBinding.llTo.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvTo)
                return
            } else {
                mViewDataBinding.llTo.visibility = View.VISIBLE
                updateLayoutCompoundDrawable(true, mViewDataBinding.tvTo)
            }
            mViewDataBinding.llFrom.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
            mViewDataBinding.llHidden.visibility = View.GONE
        } else if (layoutPressed == llFrom) {
            if (mViewDataBinding.llFrom.isVisible()) {
                mViewDataBinding.llFrom.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
                return
            } else {
                mViewDataBinding.llFrom.visibility = View.VISIBLE
                updateLayoutCompoundDrawable(true, mViewDataBinding.tvfrom)
            }
            mViewDataBinding.llTo.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvTo)
            mViewDataBinding.llHidden.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
        } else if (layoutPressed == llHidden) {
            if (mViewDataBinding.llHidden.isVisible()) {
                mViewDataBinding.llHidden.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
                return
            } else {
                mViewDataBinding.llHidden.visibility = View.VISIBLE
                updateLayoutCompoundDrawable(true, mViewDataBinding.tvHidden)
            }

            mViewDataBinding.llTo.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvTo)
            mViewDataBinding.llFrom.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
        }

    }

    private fun manageLayoutsVisibilityWithKeyboardHidden() {
        if (layoutPressed == llTo) {
            if (mViewDataBinding.llTo.isVisible()) {
                mViewDataBinding.llTo.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvTo)
                return
            }
            mViewDataBinding.llTo.visibility = View.VISIBLE
            updateLayoutCompoundDrawable(true, mViewDataBinding.tvTo)
            if (mViewDataBinding.llHidden.visibility == View.VISIBLE && mViewDataBinding.llFrom.visibility == View.VISIBLE)
                mViewDataBinding.llHidden.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
        } else if (layoutPressed == llFrom) {
            if (mViewDataBinding.llFrom.isVisible()) {
                mViewDataBinding.llFrom.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
                return
            }

            mViewDataBinding.llFrom.visibility = View.VISIBLE
            updateLayoutCompoundDrawable(true, mViewDataBinding.tvfrom)
            if (mViewDataBinding.llTo.visibility == View.VISIBLE && mViewDataBinding.llFrom.visibility == View.VISIBLE) {
                mViewDataBinding.llHidden.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
            }
        } else if (layoutPressed == llHidden) {

            if (mViewDataBinding.llHidden.isVisible()) {
                mViewDataBinding.llHidden.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
                return
            }
            mViewDataBinding.llHidden.visibility = View.VISIBLE
            updateLayoutCompoundDrawable(true, mViewDataBinding.tvHidden)
            if (mViewDataBinding.llTo.visibility == View.VISIBLE && mViewDataBinding.llHidden.visibility == View.VISIBLE) {
                mViewDataBinding.llFrom.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
            }
        } else {
            return
        }
    }

    private fun checkLayoutsVisibility() {
        if (mViewDataBinding.llTo.isVisible()) {
            mViewDataBinding.llFrom.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
            mViewDataBinding.llHidden.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
        } else if (mViewDataBinding.llFrom.isVisible() && mViewDataBinding.llHidden.isVisible())
            mViewDataBinding.llHidden.visibility = View.GONE
        updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
    }

    private fun updateCompoundDrawable(filter: TextView, ivFilter: ImageView, flag: Boolean) {
        val drawableResId = if (!flag) R.drawable.icon_filter_blue else R.drawable.ic_cross_blue
        if (!flag) filter.visibility = View.VISIBLE else filter.visibility = View.GONE
        ivFilter.setImageResource(drawableResId)
    }

    private fun updateLayoutCompoundDrawable(tag: Boolean, filter: TextView) {

        val drawableResId = if (!tag) R.drawable.icon_drop_down else R.drawable.arrow_drop_up

        val drawable = ContextCompat.getDrawable(mViewDataBinding.root.context, drawableResId)

        filter.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            drawable,
            null
        )
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.drawingFile.value == null || CookiesManager.openingNewLocationFile) {
            viewModel.cameFromProject = CookiesManager.cameToLocationViewFromProject
            CookiesManager.openingNewLocationFile = false
            CookiesManager.cameToLocationViewFromProject = false
            CookiesManager.drawingFileForLocation.value?.let {
                viewModel.getDrawingPins(it._id)
                viewModel._drawingFile.postValue(it)
            } ?: run {
                shortToastNow("No file to display. Please select any drawing file.")
            }

        } else {
            viewModel.drawingFile.value?.let {
                viewModel.getDrawingPins(it._id)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        viewModel._drawingFile.value = null
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

    private fun addFiltersToList() {
        filtersList.clear()
        filtersList.add(
            Pair(
                TaskRootStateTags.ToMe.tagValue.lowercase(),
                TaskStatus.NEW.name.lowercase()
            )
        )
        filtersList.add(
            Pair(
                TaskRootStateTags.ToMe.tagValue.lowercase(),
                TaskStatus.ONGOING.name.lowercase()
            )
        )
        filtersList.add(
            Pair(
                TaskRootStateTags.ToMe.tagValue.lowercase(),
                TaskStatus.DONE.name.lowercase()
            )
        )
        filtersList.add(
            Pair(
                TaskRootStateTags.FromMe.tagValue.lowercase(),
                TaskStatus.UNREAD.name.lowercase()
            )
        )
        filtersList.add(
            Pair(
                TaskRootStateTags.FromMe.tagValue.lowercase(),
                TaskStatus.ONGOING.name.lowercase()
            )
        )
        filtersList.add(
            Pair(
                TaskRootStateTags.FromMe.tagValue.lowercase(),
                TaskStatus.DONE.name.lowercase()
            )
        )
        filtersList.add(
            Pair(
                TaskRootStateTags.Hidden.tagValue.lowercase(),
                TaskStatus.ONGOING.name.lowercase()
            )
        )
        filtersList.add(
            Pair(
                TaskRootStateTags.Hidden.tagValue.lowercase(),
                TaskStatus.DONE.name.lowercase()
            )
        )
        filtersList.add(
            Pair(
                TaskRootStateTags.Hidden.tagValue.lowercase(),
                TaskStatus.CANCELED.name.lowercase()
            )
        )


        viewModel.viewState.isToNewClicked.value = true
        viewModel.viewState.isToOngoingClicked.value = true
        viewModel.viewState.isToDoneClicked.value = true
        viewModel.viewState.isFromUnreadClicked.value = true
        viewModel.viewState.isFromOngoingClicked.value = true
        viewModel.viewState.isFromDoneClicked.value = true
        viewModel.viewState.isHiddenOngoingClicked.value = true
        viewModel.viewState.isHiddenDoneClicked.value = true
        viewModel.viewState.isHiddenCancelled.value = true

        viewModel.checkFilter(filtersList)

    }

    private fun removeFiltersToList() {
        filtersList.clear()
        viewModel.viewState.isToNewClicked.value = false
        viewModel.viewState.isToOngoingClicked.value = false
        viewModel.viewState.isToDoneClicked.value = false
        viewModel.viewState.isFromUnreadClicked.value = false
        viewModel.viewState.isFromOngoingClicked.value = false
        viewModel.viewState.isFromDoneClicked.value = false
        viewModel.viewState.isHiddenOngoingClicked.value = false
        viewModel.viewState.isHiddenDoneClicked.value = false
        viewModel.viewState.isHiddenCancelled.value = false
        viewModel.checkFilter(filtersList)
    }

    private fun isAnyConditionFalse() {
        val viewState = viewModel.viewState
        val flag = (viewState.isToNewClicked.value == true &&
                viewState.isToOngoingClicked.value == true &&
                viewState.isToDoneClicked.value == true &&
                viewState.isFromUnreadClicked.value == true &&
                viewState.isFromOngoingClicked.value == true &&
                viewState.isFromDoneClicked.value == true &&
                viewState.isHiddenOngoingClicked.value == true &&
                viewState.isHiddenDoneClicked.value == true &&
                viewState.isHiddenCancelled.value == true)
        mViewDataBinding.cbSelectAll.isChecked = flag
    }
}