package com.test.recofaciale

import android.os.Bundle
import android.util.Size
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.face.Face
import com.test.recofaciale.ui.theme.RecoFacialeTheme
import kotlinx.coroutines.CoroutineScope

class MainActivity : ComponentActivity() {
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var viewModelFace = ViewModelProvider(this)[ViewModelFace::class.java]

        setContent {
            RecoFacialeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraPreview(viewModelFace)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreview(viewModel : ViewModelFace) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val permissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    LaunchedEffect(permissionState) {
        if (permissionState.hasPermission) {
            permissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (permissionState.hasPermission) {
            CameraPreviewContent(context = context, coroutineScope = coroutineScope, viewModel = viewModel)
        } else {
            Text(text = "Camera permission is required to use this app.")
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewContent(context: android.content.Context, coroutineScope: CoroutineScope,viewModel : ViewModelFace) {
    val ctx = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var cameraSelector = remember { CameraSelector.DEFAULT_FRONT_CAMERA }

    val previewView = remember {
        PreviewView(context).apply {
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) { view ->
        val cameraProvider = cameraProviderFuture.get()
        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(view.surfaceProvider)
        }
        val executor = ContextCompat.getMainExecutor(ctx)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(previewView.width, previewView.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setImageQueueDepth(10)
            .build()
            .apply {
                setAnalyzer(executor, FaceAnalyzer(
                    object : FaceAnalyzerCallback {
                        override fun processFace(faces: List<Face>) {
                            viewModel.processFaces(faces)
                            if (viewModel.state.value.isSmiling)
                                Toast.makeText(ctx,"SMile",LENGTH_SHORT).show()
                            Toast.makeText(ctx,"REUSSI",LENGTH_SHORT).show()
                        }

                        override fun errorFace(error: String) {
                            Toast.makeText(ctx,error,LENGTH_SHORT).show()
                        }

                    }
                ))
            }

        cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        CameraPreview(ViewModelFace())
    }
}



