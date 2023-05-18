package ee.zstronics.ceibro.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.AspectRatio.RATIO_16_9
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
import kotlin.math.absoluteValue

class CeibroCameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCeibroCameraBinding
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var camera: Camera? = null
    lateinit var imageCapture: ImageCapture

    /** Declare worker thread at the class level so it can be reused after config changes */
    private val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }

    /** Internal reference of the [DisplayManager] */
    private val displayManager by lazy {
        applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private var isTorchOn: Boolean = false
    private var isFlashEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.activity_ceibro_camera, null, false)
        setContentView(binding.root)

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
            capturePhoto()
        }

        binding.toggleTorchButton.setOnClickListener {
            toggleTorch()
        }
        binding.flashButton.setOnClickListener {
            toggleFlash()
        }
        binding.close.setOnClickListener {
            finish()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Retrieve the default camera resolution and aspect ratio
            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList
            val defaultCameraId = cameraIds.firstOrNull()
            val characteristics = cameraManager.getCameraCharacteristics(defaultCameraId!!)
            val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            val targetResolution = sensorSize?.let {
                Size(it.width(), it.height())
            }
            val targetAspectRatio = sensorSize?.let {
                it.width() / it.height()
            }

            // Set up the preview configuration
            val preview = Preview.Builder()
//                .setTargetResolution(targetResolution ?: Size(640, 480)) // Set desired resolution or aspect ratio (only one of them at a time in same config)
                .setTargetAspectRatio(RATIO_4_3)
                .build()
            // Set up the image capture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Create a preview use case and bind it to the PreviewView
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            try {
                // Unbind any previous use cases
                cameraProvider.unbindAll()
                // Bind the camera to the lifecycle
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (ex: Exception) {
                Toast.makeText(this, "Error starting camera: ${ex.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleTorch() {
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

        val flashMode =
            if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        imageCapture.flashMode = flashMode

//        val imageCaptureBuilder = ImageCapture.Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            .setFlashMode(if (flashMode) ImageCapture.FLASH_MODE_AUTO else ImageCapture.FLASH_MODE_OFF)

//        val imageCapture = imageCaptureBuilder.build()
        // Create a file to save the captured image in the internal storage directory
        val outputDirectory = File(applicationContext.filesDir, "photos")
        outputDirectory.mkdirs()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(outputDirectory, "IMG_$timeStamp.jpg")

        // Configure the output options for the image capture
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Check if the camera is bound before capturing the photo
        camera?.let { camera ->
            imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                        val intent = Intent(
                            this@CeibroCameraActivity,
                            CeibroCapturedPreviewActivity::class.java
                        )
                        intent.putExtra("capturedUri", savedUri)
                        startActivity(intent)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            this@CeibroCameraActivity,
                            "Error capturing photo: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
    }
}