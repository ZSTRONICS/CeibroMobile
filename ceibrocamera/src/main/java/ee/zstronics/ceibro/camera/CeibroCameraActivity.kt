package ee.zstronics.ceibro.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.util.Size
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.activity_ceibro_camera, null, false)
        setContentView(binding.root)
        sourceName = intent.getStringExtra("source_name") ?: ""

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
            binding.captureButton.isEnabled = false
            binding.flipCameraButton.isEnabled = false
            binding.flashButton.isEnabled = false
            binding.toggleTorchButton.isEnabled = false
            val tint = ContextCompat.getColor(this, R.color.appGrey3)
            binding.captureButton.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
            capturePhoto()
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
                pickFiles { listOfPickedImages ->
                    if (sourceName == CeibroImageViewerActivity::class.java.name) {
                        val ceibroImagesIntent =
                            Intent()
                        val newBundle = Bundle()
                        newBundle.putParcelableArrayList("images", listOfPickedImages)
                        ceibroImagesIntent.putExtras(newBundle)
                        setResult(RESULT_OK, ceibroImagesIntent)
                        finish()
                    } else {
                        val ceibroCamera =
                            Intent(applicationContext, CeibroImageViewerActivity::class.java)
                        val bundle = Bundle()
                        bundle.putParcelableArrayList("images", listOfPickedImages)
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
        binding.captureButton.isEnabled = true
        binding.captureButton.isEnabled = true
        binding.flipCameraButton.isEnabled = true
        binding.flashButton.isEnabled = true
        binding.toggleTorchButton.isEnabled = true
        
        val tint = ContextCompat.getColor(this, R.color.white)
        binding.captureButton.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
//        if (sourceName == CeibroImageViewerActivity::class.java.name) {
//            binding.imagesPicker.visibility = View.GONE
//        } else {
//            binding.imagesPicker.visibility = View.VISIBLE
//        }
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
                val listOfPickedImages =
                    result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                val ceibroCamera =
                    Intent(applicationContext, CeibroImageViewerActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("images", listOfPickedImages)
                ceibroCamera.putExtras(bundle)
                ceibroImageViewerLauncher.launch(ceibroCamera)
            }
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
            val sensorSize =
                characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
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
                        if (sourceName == CeibroImageViewerActivity::class.java.name) {
                            val intent = Intent(
                                this@CeibroCameraActivity,
                                CapturedPreviewActivity::class.java
                            )
                            intent.putExtra("capturedUri", savedUri)
                            cameraPreviewLauncher.launch(intent)
                        } else {
                            val intent = Intent(
                                this@CeibroCameraActivity,
                                CeibroCapturedPreviewActivity::class.java
                            )
                            intent.putExtra("capturedUri", savedUri)
                            cameraPreviewLauncherNoFinish.launch(intent)
                        }
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