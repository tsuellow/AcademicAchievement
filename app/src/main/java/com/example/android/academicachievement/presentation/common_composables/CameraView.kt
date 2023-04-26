package com.example.android.academicachievement.presentation.common_composables

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun RoundedCameraView(
    filename: String,
    outputDirectory: File,
    //executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // 1
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()
    val executor = Executors.newSingleThreadExecutor()
    val imageAnalysis = ImageAnalysis.Builder().apply {
        setTargetResolution(Size(previewView.width, previewView.height))
        setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    }.build()

    var frozenBitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

    val analyzer = FreezeAnalyzer(object : FreezeCallback {
        override fun onLastFrameCaptured(bitmap: Bitmap) {
            frozenBitmap = bitmap
        }
    })
    imageAnalysis.setAnalyzer(executor, analyzer)


    var processing by remember {
        mutableStateOf(false)
    }
    //lateinit var cameraProvider:ProcessCameraProvider
    // 2
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    // 3
    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxWidth()) {

        AndroidView(
            { previewView },
            modifier = Modifier
                .aspectRatio(1f)
                .clip(CircleShape)
        )

        frozenBitmap?.let {
            Image(
                modifier = Modifier
                    .blur(radius=16.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape),
                bitmap = it.asImageBitmap(),
                contentDescription = "frozen blur"
            )
        }

        if (processing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        FloatingActionButton(
            modifier = Modifier.padding(20.dp),
            onClick = {
                Log.i("kilo", "ON CLICK")
                processing = true
                takePhoto(
                    filename = filename,
                    imageCapture = imageCapture,
                    analyzer = analyzer,
                    outputDirectory = outputDirectory,
                    context = context,
                    executor = executor,
                    onImageCaptured = {
                        processing = false
                        onImageCaptured(it)
                    },
                    onError = onError
                )
            },
            content = {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Take picture",
                    tint = Color.White,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(1.dp)
                )
            }
        )
    }
}

private fun takePhoto(
    filename: String,
    imageCapture: ImageCapture,
    analyzer: FreezeAnalyzer,
    outputDirectory: File,
    context: Context,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {

    val photoFile = File(
        outputDirectory,
        "vessel.jpg"
    )

    if (photoFile.exists()) {
        photoFile.delete()
    }

    val compPhotoFile = File(
        outputDirectory,
        "$filename.jpg"
    )

    if (compPhotoFile.exists()) {
        compPhotoFile.delete()
    }


    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    analyzer.freeze()
    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
            onError(exception)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val savedUri = cropAndCompress(photoFile, compPhotoFile, context)
            onImageCaptured(savedUri)
        }
    })
}

fun cropAndCompress(file: File, compressedFile: File, context: Context): Uri {
    val bigBitmap = rectifyImage(context = context, file)!!
    val width = bigBitmap.width
    val height = bigBitmap.height
    val newWidth = if (height > width) width else height
    val newHeight = if (height > width) height - (height - width) else height
    var cropW = (width - height) / 2
    cropW = if (cropW < 0) 0 else cropW
    var cropH = (height - width) / 2
    cropH = if (cropH < 0) 0 else cropH
    val bitmap = Bitmap.createBitmap(bigBitmap, cropW, cropH, newWidth, newHeight)
    try {
        if (compressedFile.exists()) {
            compressedFile.delete()
        }
        val mediumOut = FileOutputStream(compressedFile)
        val medium = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
        medium.compress(Bitmap.CompressFormat.JPEG, 100, mediumOut)
        mediumOut.flush()
        mediumOut.close()
        file.delete()
        return Uri.fromFile(compressedFile)
    } catch (ex: IOException) {
        ex.printStackTrace()
        return Uri.fromFile(file)
    }
}


fun rectifyImage(context: Context, imageFile: File): Bitmap? {
    val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
    return try {
        val uri = Uri.fromFile(imageFile)
        val input: InputStream? = context.contentResolver.openInputStream(uri)
        val ei: ExifInterface
        if (Build.VERSION.SDK_INT > 23) ei = input?.let { ExifInterface(it) }!! else ei =
            ExifInterface(uri.path!!)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(originalBitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(originalBitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(originalBitmap, 270f)
            else -> originalBitmap
        }
    } catch (e: Exception) {
        originalBitmap
    }
}

fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height,
        matrix, true
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }


class FreezeAnalyzer(private val callback: FreezeCallback) : ImageAnalysis.Analyzer {
    private var flag = false

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        if (flag) {
            flag = false
            val bitmap = toBitmap(image)
            if (bitmap != null) {
                callback.onLastFrameCaptured(bitmap)
            }
        }
        image.close()
    }

    fun freeze() {
        flag = true
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun toBitmap(image: ImageProxy): Bitmap? {
        // Convert the imageProxy to Bitmap
        // ref https://stackoverflow.com/questions/56772967/converting-imageproxy-to-bitmap
        // ISSUE, on my android 7 when converting the imageProxy to Bitmap I have a problem with the colors...
        var bitmap: Bitmap? = image.image?.toBitmap()

        // Rotate the bitmap
        val rotationDegrees = image.imageInfo.rotationDegrees.toFloat()
        if (rotationDegrees != 0f) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees)
            bitmap = bitmap?.let { Bitmap.createBitmap(it, 0, 0, bitmap!!.width, bitmap!!.height, matrix, true) }
        }
        return bitmap
    }
}

interface FreezeCallback {
    fun onLastFrameCaptured(bitmap: Bitmap)
}

fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}