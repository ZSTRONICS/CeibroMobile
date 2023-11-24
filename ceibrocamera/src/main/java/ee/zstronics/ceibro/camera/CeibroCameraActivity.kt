package ee.zstronics.ceibro.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalZeroShutterLag
class CeibroCameraActivity : BaseActivity() {
    lateinit var binding: ActivityCeibroCameraBinding
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var camera: Camera? = null
    lateinit var imageCapture: ImageCapture

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private var isTorchOn: Boolean = false
    private var isFlashEnabled: Boolean = false
    var sourceName = ""
    val oldImages = arrayListOf<PickedImages>()
    var whiteTint = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.activity_ceibro_camera, null, false)
        setContentView(binding.root)
        sourceName = intent.getStringExtra("source_name") ?: ""

        if (sourceName != CeibroImageViewerActivity::class.java.name) {
            val allImages = intent.getBundleExtra("allImagesBundle")
            val imagesList = allImages?.getParcelableArrayList<PickedImages>("allImagesList")
            imagesList?.let { oldImages.addAll(it) }
        }

        binding.imagesPicker.visibility =
            if (sourceName == CeibroImageViewerActivity::class.java.name) {
                View.GONE
            } else {
                View.VISIBLE
            }
        // Request camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }

        // Button click listener to flip the camera
        binding.flipCameraButton.setOnClickListener {
            flipCamera()
        }

        // Button click listener to capture a photo
        binding.captureButton.setOnClickListener {
            Log.d("Photo capture started  ", getCurrentTimeStamp())
            capturePhoto()
            binding.captureButton.isEnabled = false
            binding.flipCameraButton.isEnabled = false
            binding.flashButton.isEnabled = false
            binding.toggleTorchButton.isEnabled = false
            val tint = ContextCompat.getColor(this, R.color.appGrey3)
            binding.captureButton.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
        }

        binding.toggleTorchButton.setOnClickListener {
            toggleTorch()
        }
        binding.flashButton.setOnClickListener {
            toggleFlash()
        }
        binding.imagesPicker.setOnClickListener {
            checkPermission(
                listOf(
                    Manifest.permission.CAMERA,
                )
            ) {
                pickImageFiles { listOfPickedImages ->
                    val newList: java.util.ArrayList<PickedImages> = arrayListOf()
                    if (sourceName == CeibroImageViewerActivity::class.java.name) {
                        val ceibroImagesIntent =
                            Intent()
                        val newBundle = Bundle()
                        oldImages.map {oldImage ->
                            val foundImage = listOfPickedImages.find { it.fileName == oldImage.fileName }
                            if (foundImage != null) {
                                val index = listOfPickedImages.indexOf(foundImage)
                                listOfPickedImages.removeAt(index)
                                cancelAndMakeToast(this, "Removed duplicate images as received", Toast.LENGTH_SHORT)
                            }
                        }
                        newList.addAll(listOfPickedImages)
                        newList.addAll(oldImages)
//                        listOfPickedImages.addAll(oldImages)

                        newBundle.putParcelableArrayList("images", newList)
                        ceibroImagesIntent.putExtras(newBundle)
                        setResult(RESULT_OK, ceibroImagesIntent)
                        finish()
                    } else {
                        val ceibroCamera =
                            Intent(applicationContext, CeibroImageViewerActivity::class.java)
                        val bundle = Bundle()
                        oldImages.map {oldImage ->
                            val foundImage = listOfPickedImages.find { it.fileName == oldImage.fileName }
                            if (foundImage != null) {
                                val index = listOfPickedImages.indexOf(foundImage)
                                listOfPickedImages.removeAt(index)
                                cancelAndMakeToast(this, "Removed duplicate images as received", Toast.LENGTH_SHORT)
                            }
                        }
                        newList.addAll(listOfPickedImages)
                        newList.addAll(oldImages)

                        bundle.putParcelableArrayList("images", newList)
                        ceibroCamera.putExtras(bundle)
                        ceibroImageViewerLauncher.launch(ceibroCamera)
                    }
                }
            }
        }
        binding.close.setOnClickListener {
            finish()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onRestart() {
        super.onRestart()
        startCamera()
        val torchIcon = R.drawable.ic_torch_off
        binding.toggleTorchButton.setImageResource(torchIcon)
        val handler = Handler()
        handler.postDelayed(Runnable {
            binding.captureButton.isEnabled = true
            binding.captureButton.isEnabled = true
            binding.flipCameraButton.isEnabled = true
            binding.flashButton.isEnabled = true
            binding.toggleTorchButton.isEnabled = true

            val tint = ContextCompat.getColor(this, R.color.white)
            whiteTint = tint
            binding.captureButton.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
        }, 270)
    }

    private val ceibroImageViewerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK, result.data)
                finish()
            }
        }
    private val cameraPreviewLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK, result.data)
                finish()
            }
        }

    private val cameraPreviewLauncherNoFinish =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newList: ArrayList<PickedImages> = arrayListOf()
                val listOfPickedImages =
                    result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                val ceibroCamera =
                    Intent(applicationContext, CeibroImageViewerActivity::class.java)
                val bundle = Bundle()
                if (listOfPickedImages != null) {
                    newList.addAll(listOfPickedImages)
                }
                newList.addAll(oldImages)

                bundle.putParcelableArrayList("images", newList)
                ceibroCamera.putExtras(bundle)
                ceibroImageViewerLauncher.launch(ceibroCamera)
            }
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            try {
                cameraProvider.unbindAll()
            } catch (ex: Exception) {
                Toast.makeText(this, "Error starting camera: ${ex.message}", Toast.LENGTH_SHORT)
                    .show()
            }

//            // Retrieve the default camera resolution and aspect ratio
//            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
//            val cameraIds = cameraManager.cameraIdList
//            val defaultCameraId = cameraIds.firstOrNull()
//            val characteristics = cameraManager.getCameraCharacteristics(defaultCameraId!!)
//            val sensorSize =
//                characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
//            val targetResolution = sensorSize?.let {
//                Size(it.width(), it.height())
//            }
//            val targetAspectRatio = sensorSize?.let {
//                it.width() / it.height()
//            }

            // Set up the preview configuration
//                .setTargetResolution(targetResolution ?: Size(640, 480)) // Set desired resolution or aspect ratio (only one of them at a time in same config)
            val preview = Preview.Builder()
                .setTargetAspectRatio(RATIO_4_3)
                .build()
            // Set up the image capture use case
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(RATIO_4_3)
                .build()

            // Create a preview use case and bind it to the PreviewView
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            try {
                // Unbind any previous use cases
//                cameraProvider.unbindAll()
                // Bind the camera to the lifecycle
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (ex: Exception) {
                Toast.makeText(this, "Error starting camera: ${ex.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleTorch() {
        if (isFlashEnabled) {
            toggleFlash()
        }
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            isTorchOn = !isTorchOn
            camera?.cameraControl?.enableTorch(isTorchOn)

            // Update the torch button image based on the torch state
            val torchIcon = if (isTorchOn) R.drawable.ic_torch else R.drawable.ic_torch_off
            binding.toggleTorchButton.setImageResource(torchIcon)
        }
    }

    private fun toggleFlash() {
        if (isTorchOn) {
            toggleTorch()
        }
        isFlashEnabled = !isFlashEnabled
        val flashIcon = if (isFlashEnabled) R.drawable.ic_flash_on else R.drawable.ic_flash_off
        binding.flashButton.setImageResource(flashIcon)
    }

    private fun flipCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture
        if (isFlashEnabled)
            imageCapture.flashMode = ImageCapture.FLASH_MODE_ON

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputDirectory = File(applicationContext.filesDir, "photos")
        outputDirectory.mkdirs()
        val photoFile = File(outputDirectory, "IMG_$timeStamp.jpg")

        // Configure the output options for the image capture
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    val intent = Intent(
                        this@CeibroCameraActivity,
                        if (sourceName == CeibroImageViewerActivity::class.java.name)
                            CapturedPreviewActivity::class.java
                        else
                            CeibroCapturedPreviewActivity::class.java
                    )
                    intent.putExtra("capturedUri", savedUri)
                    Log.d("capturing photo ended", getCurrentTimeStamp())
                    if (sourceName == CeibroImageViewerActivity::class.java.name)
                        cameraPreviewLauncher.launch(intent)
                    else
                        cameraPreviewLauncherNoFinish.launch(intent)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CeibroCameraActivity,
                        "Please wait for camera to launch properly",
                        Toast.LENGTH_SHORT
                    ).show()
                    enableButtons()
                    println("Error capturing photo: ${exception.message}")
                }
            }
        )
    }

    fun enableButtons() {
        val handler = Handler()
        handler.postDelayed(Runnable {
            binding.captureButton.isEnabled = true
            binding.captureButton.isEnabled = true
            binding.flipCameraButton.isEnabled = true
            binding.flashButton.isEnabled = true
            binding.toggleTorchButton.isEnabled = true

            val tint = ContextCompat.getColor(this, R.color.white)
            binding.captureButton.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
        }, 90)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
    }
}