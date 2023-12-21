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
import android.net.Uri
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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.isVisible
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.databinding.FragmentLocationsV2Binding
import com.zstronics.ceibro.ui.locationv2.usage.PinPointsData
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import stkq.draw.FiveTuple
import stkq.draw.FourTuple
import java.io.File
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

    private var markers: MutableList<Triple<Float, Float, String>> = mutableListOf()
    private var sampleMarkerPoints1: MutableList<FiveTuple<Float, Float, Float, Float, Float>> = mutableListOf()
    private var loadMarkerPoints: MutableList<FourTuple<Int, Int, Float, Float>> = mutableListOf()
    private val PIN_TAP_THRESHOLD = 6
    private var loadingOldData = true

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
                    viewState.isToOngoingClicked.value = false
                    viewState.isToDoneClicked.value = false
                }
                viewState.isToNewClicked.value = !(viewState.isToNewClicked.value!!)
            }

            R.id.ToOngoingStateText -> {

                if (viewState.isToOngoingClicked.value == false) {
                    viewState.isToNewClicked.value = false
                    viewState.isToDoneClicked.value = false
                }

                viewState.isToOngoingClicked.value = !(viewState.isToOngoingClicked.value!!)
            }

            R.id.ToDoneStateText -> {

                if (viewState.isToDoneClicked.value == false) {
                    viewState.isToNewClicked.value = false
                    viewState.isToOngoingClicked.value = false
                }
                viewState.isToDoneClicked.value = !(viewState.isToDoneClicked.value!!)
            }

            R.id.fromUnredStateText -> {

                if (viewState.isFromUnreadClicked.value == false) {
                    viewState.isFromOngoingClicked.value = false
                    viewState.isFromDoneClicked.value = false
                }
                viewState.isFromUnreadClicked.value = !(viewState.isFromUnreadClicked.value!!)
            }

            R.id.FromOngoingStateText -> {

                if (viewState.isFromOngoingClicked.value == false) {
                    viewState.isFromUnreadClicked.value = false
                    viewState.isFromDoneClicked.value = false
                }

                viewState.isFromOngoingClicked.value = !(viewState.isFromOngoingClicked.value!!)
            }

            R.id.FromDoneStateText -> {

                if (viewState.isFromDoneClicked.value == false) {
                    viewState.isFromUnreadClicked.value = false
                    viewState.isFromOngoingClicked.value = false
                }
                viewState.isFromDoneClicked.value = !(viewState.isFromDoneClicked.value!!)
            }

            R.id.hiddenOngoingStateText -> {

                if (viewState.isHiddenOngoingClicked.value == false) {
                    viewState.isHiddenDoneClicked.value = false
                    viewState.isHiddenCancelled.value = false
                }
                viewState.isHiddenOngoingClicked.value = !(viewState.isHiddenOngoingClicked.value!!)
            }

            R.id.hiddenDoneStateText -> {


                if (viewState.isHiddenDoneClicked.value == false) {
                    viewState.isHiddenOngoingClicked.value = false
                    viewState.isHiddenCancelled.value = false
                }

                viewState.isHiddenDoneClicked.value = !(viewState.isHiddenDoneClicked.value!!)
            }

            R.id.hiddenCancelledStateText -> {


                if (viewState.isHiddenCancelled.value == false) {
                    viewState.isHiddenOngoingClicked.value = false
                    viewState.isHiddenDoneClicked.value = false
                }
                viewState.isHiddenCancelled.value = !(viewState.isHiddenCancelled.value!!)
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

        viewModel.drawingFile.observe(viewLifecycleOwner) {
            mViewDataBinding.progressBar.visibility = View.VISIBLE
            mViewDataBinding.tvFloorName.text = "${it.floor.floorName} Floor"
            mViewDataBinding.tvFileName.text = "${it.fileName}"
            mViewDataBinding.tvGroupName.text = "${it.fileTag.toCamelCase()}"
//            mViewDataBinding.progressBar.isIndeterminate = true


            val file = File(it.uploaderLocalFilePath)
//            val fileUri = Uri.fromFile(file)
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
//                            loadMarkerPoints.add(
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
                        println("PDFView loadMarkerPoints: ${loadMarkerPoints}")
                    }, 500)

                }
                .onTap { event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {

                        val pageWidth = mViewDataBinding.pdfView.measuredWidth
                        val pageHeight = mViewDataBinding.pdfView.measuredHeight

                        val zoom = mViewDataBinding.pdfView.zoom // Get the current zoom level

                        val normalizedX = event.x / mViewDataBinding.pdfView.width * pageWidth / zoom
                        val normalizedY = event.y / mViewDataBinding.pdfView.height * pageHeight / zoom

                        println("normalizedX ${normalizedX} PDFView onTap : ${event.x} / ${mViewDataBinding.pdfView.width} * ${pageWidth} / ${zoom}")
                        println("normalizedY${normalizedY} PDFView onTap : ${event.y} / ${mViewDataBinding.pdfView.height} * ${pageHeight} / ${zoom}")

//                    if (zoom > 1.0) {
//                        sampleMarkerPoints.add(Triple(normalizedX, normalizedY, zoom))
//                        pdfView.invalidate()
//
//                    } else {
                        sampleMarkerPoints1.add(FiveTuple(normalizedX, normalizedY, zoom, event.x, event.y))
//                        sampleMarkerPoints.add(Triple(normalizedX, normalizedY, zoom))
                        mViewDataBinding.pdfView.invalidate()
//                    }

                    }
                    false
                }
                .onDraw { canvas, pageWidth, pageHeight, displayedPage ->
                    mViewDataBinding.progressBar.visibility = View.INVISIBLE
                    val matrixValues = FloatArray(9)
                    canvas.matrix.getValues(matrixValues)
                    var transX = matrixValues[Matrix.MTRANS_X]
                    var transY = matrixValues[Matrix.MTRANS_Y]

                    if (sampleMarkerPoints1.isNotEmpty()) {
                        val samplePointsMark = sampleMarkerPoints1
                        for (samplePoints in samplePointsMark) {

                            canvas.matrix.getValues(matrixValues)
                            transX = matrixValues[Matrix.MTRANS_X]
                            transY = matrixValues[Matrix.MTRANS_Y]

                            //Store these x and y points to DB to load the points again on the file
                            val xPoint = samplePoints.actualX - (transX / samplePoints.zoomLevel)         // we are doing minus because transX or transY are always in negative if zoomed
                            val yPoint = samplePoints.actualY - (transY / samplePoints.zoomLevel)        //so, minus minus becomes plus (+), so we are actually doing addition

                            println("${samplePoints.zoomLevel} PDFView pdfCanvas.transX: ${samplePoints.actualX} = ${transX} = ${xPoint} -> pdfCanvas.transY: ${samplePoints.actualY} = ${transY} = ${yPoint} -> EVENT.X: ${samplePoints.eventX} EVENT.Y= ${samplePoints.eventY}")

                            val pdfBounds = calculatePDFBounds(pageWidth, pageHeight, transX, transY)
                            if (samplePoints.eventX >= pdfBounds.left && samplePoints.eventX <= pdfBounds.right &&
                                samplePoints.eventY >= pdfBounds.top && samplePoints.eventY <= pdfBounds.bottom) {      //if in bounds then marker is placed
                                var isExistingPinTapped = false
                                for (existingPin in markers) {
                                    val distance = calculateDistance(existingPin.first, existingPin.second, xPoint, yPoint)
                                    if (distance < PIN_TAP_THRESHOLD) {
                                        println("PDFView distance: ${distance}")
                                        isExistingPinTapped = true
                                        shortToastNow("Existing point tapped")
//                                        Toast.makeText(this, "Existing point tapped", Toast.LENGTH_SHORT).show()
                                        break
                                    }
                                }
                                if (loadingOldData) {
                                    markers.add(Triple(xPoint, yPoint, ""))
                                }
                                else if (!isExistingPinTapped) {
                                    markers.add(Triple(xPoint, yPoint, "new"))
                                }

                            }

                            val index = samplePointsMark.indexOf(samplePoints)
                            if (index == samplePointsMark.size-1) {
                                sampleMarkerPoints1.clear()
//                            pdfView.invalidate()
                            }
                        }
                    }
                    if (loadMarkerPoints.isNotEmpty()) {
                        val loadPointsMark = loadMarkerPoints
                        for (loadPoints in loadPointsMark) {
                            val zoom = mViewDataBinding.pdfView.zoom // Get the current zoom level

                            canvas.matrix.getValues(matrixValues)
                            transX = matrixValues[Matrix.MTRANS_X]
                            transY = matrixValues[Matrix.MTRANS_Y]

                            val currentPageWidth = mViewDataBinding.pdfView.measuredWidth
                            val currentPageHeight = mViewDataBinding.pdfView.measuredHeight
                            val originalPointWidth = loadPoints.width
                            val originalPointHeight = loadPoints.height

                            val xScaleFactor = pageWidth / originalPointWidth
                            val yScaleFactor = pageHeight / originalPointHeight

                            val pointXOfCurrentDevice = (loadPoints.eventX * xScaleFactor)
                            val pointYOfCurrentDevice = (loadPoints.eventY * yScaleFactor)

                            println("PDFView currentPageWidth: ${pageWidth} currentPageHeight= ${pageHeight} originalPointWidth= ${originalPointWidth} originalPointHeight= ${originalPointHeight}")
                            println("PDFView x and y ScaleFactor: ${xScaleFactor} = ${yScaleFactor} pointXOfCurrentDevice= ${pointXOfCurrentDevice} ${loadPoints.eventX} pointYOfCurrentDevice= ${pointYOfCurrentDevice} ${loadPoints.eventY}")


                            val actualX = pointXOfCurrentDevice + transX
                            val actualY = pointYOfCurrentDevice + transY        // this will give actual y point because we were saving yPoint after zoom calculation

                            val normalizedX = actualX / mViewDataBinding.pdfView.width * mViewDataBinding.pdfView.measuredWidth / zoom
                            val normalizedY = actualY / mViewDataBinding.pdfView.height * mViewDataBinding.pdfView.measuredHeight / zoom

                            val xPoint = normalizedX - (transX / zoom)         // we are doing minus because transX or transY are always in negative if zoomed
                            val yPoint = normalizedY - (transY / zoom)        //so, minus minus becomes plus (+), so we are actually doing addition

                            println("PDFView pdfCanvas.transX: ${actualX} = ${normalizedX} = ${xPoint} = ${transX} -> pdfCanvas.transY: ${actualY} = ${normalizedY} = ${yPoint} = ${transY}")

                            markers.add(Triple(xPoint, yPoint, ""))

                            val index = loadPointsMark.indexOf(loadPoints)
                            if (index == loadPointsMark.size-1) {
                                loadMarkerPoints.clear()
//                            pdfView.invalidate()
                            }
                        }
                    }
                    loadingOldData = false


                    for (marker in markers) {
                        val point = PointF(marker.first, marker.second)
                        mapPdfCoordinatesToCanvas(point, canvas, marker.third)
//                    canvas.drawCircle(point.x, point.y, 10f, paint)
                    }

                }
                .enableAnnotationRendering(true)
                .load()
        }

    }

    //First Solution
    private fun mapPdfCoordinatesToCanvas(
        point: PointF,
        canvas: Canvas,
        isNew: String
    ) {
        val currentZoom = mViewDataBinding.pdfView.zoom // Use your method to get the current zoom level

        val matrixValues = FloatArray(9)
        canvas.matrix.getValues(matrixValues)
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]
//        println("PDFView transX: ${transX} -> transY: ${transY}")

        // Scale the point's coordinates by the zoom level
        val scaledX = (point.x * currentZoom)
        val scaledY = (point.y * currentZoom)
//        println("PDFView scaledX: ${scaledX} -> scaledY: ${scaledY} -> currentZoom: ${currentZoom}")

        val vectorDrawable = ResourcesCompat.getDrawable(resources, R.drawable.icon_pin_point_circle, null)
        val bitmap1 = Bitmap.createBitmap(55, 55, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(bitmap1)
        vectorDrawable?.setBounds(0, 0, tempCanvas.width, tempCanvas.height)
        vectorDrawable?.draw(tempCanvas)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap1, ((30 + (currentZoom * 3.5) + 5).toInt()), ((30 + (currentZoom * 3.5) + 5).toInt()), true)

        val adjustedX = scaledX - scaledBitmap.width / 2
        val adjustedY = scaledY - scaledBitmap.height / 2

//        canvas.drawCircle(adjustedX, adjustedY, 50, paint)
        canvas.drawBitmap(scaledBitmap, adjustedX, adjustedY, null)

        if (isNew.equals("new", true)) {
            val newX = adjustedX + transX + (scaledBitmap.width - 17)
            val newY = adjustedY + transY + (scaledBitmap.height - 17)

            println("PDFView adjustedX: ${scaledX}/ ${adjustedX}/ ${newX}/ ${transX} -> adjustedY: ${scaledY}/ ${adjustedY}/ ${newY}/ ${transY}")
            taskPopupMenu(mViewDataBinding.pdfView, newX, newY, point)
            for (marker in markers) {
                if (marker.first == point.x && marker.second == point.y) {
                    val index = markers.indexOf(Triple(marker.first, marker.second, "new"))
                    markers[index] = Triple(marker.first, marker.second, "")
                }
            }
        }
        mViewDataBinding.pdfView.matrix.mapPoints(matrixValues)
        mViewDataBinding.pdfView.matrix.set(canvas.matrix)
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    private fun calculatePDFBounds(pageWidth: Float, pageHeight: Float, transX: Float, transY: Float): RectF {
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
                viewModel._drawingFile.postValue(it)
            }

            println("CookiesManager.drawingFileForLocation11: ${CookiesManager.drawingFileForLocation.value}")
        }
    }
}
