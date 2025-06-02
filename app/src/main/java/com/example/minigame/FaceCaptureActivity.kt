class FaceCaptureActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewView = PreviewView(this)
        setContentView(previewView)

        EmotionAnalyzer.init(this)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                    processImage(imageProxy)
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        FaceDetection.getClient().process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0].boundingBox
                    val bitmap = previewView.bitmap ?: return@addOnSuccessListener
                    val faceBitmap = Bitmap.createBitmap(bitmap, face.left.coerceAtLeast(0), face.top.coerceAtLeast(0), face.width(), face.height())
                    val emotion = EmotionAnalyzer.predictEmotion(faceBitmap)

                    val intent = Intent().putExtra("emotion", emotion)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }.addOnCompleteListener { imageProxy.close() }
    }
}
