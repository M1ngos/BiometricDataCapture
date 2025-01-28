import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class PathPoint(
    val x: Float,
    val y: Float,
    val isStartPoint: Boolean = false
)

@Composable
fun SignatureScreen(
    onSignatureSaved: (File) -> Unit
) {
    val points = remember { mutableStateListOf<PathPoint>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Signature canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SignatureCanvas(
                points = points,
                onPointAdded = { point ->
                    points.add(point)
                }
            )
        }

        // Button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { points.clear() }
            ) {
                Text("Clear")
            }

            Button(
                onClick = {
                    scope.launch {
                        val bitmap = createSignatureBitmap(points)
                        val file = saveSignatureToFile(context, bitmap)

                        onSignatureSaved(file)
                    }
                },
                enabled = points.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun SignatureCanvas(
    points: List<PathPoint>,
    onPointAdded: (PathPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onPointAdded(PathPoint(offset.x, offset.y, true))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onPointAdded(PathPoint(change.position.x, change.position.y))
                    }
                )
            }
    ) {
        // Draw signature path
        val currentPath = Path()

        points.forEach { point ->
            if (point.isStartPoint) {
                currentPath.moveTo(point.x, point.y)
            } else {
                currentPath.lineTo(point.x, point.y)
            }
        }

        drawPath(
            path = currentPath,
            color = Color.Black,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw border
        drawRect(
            color = Color.LightGray,
            style = Stroke(width = 2f)
        )
    }
}

private fun createSignatureBitmap(points: List<PathPoint>): Bitmap {
    if (points.isEmpty()) {
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    }

    // Find the bounds of the signature
    val minX = points.minOf { it.x }
    val minY = points.minOf { it.y }
    val maxX = points.maxOf { it.x }
    val maxY = points.maxOf { it.y }

    // Add padding
    val padding = 20f
    val width = (maxX - minX + 2 * padding).toInt()
    val height = (maxY - minY + 2 * padding).toInt()

    // Create bitmap with actual signature dimensions
    val bitmap = Bitmap.createBitmap(
        width.coerceAtLeast(100),
        height.coerceAtLeast(100),
        Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(bitmap)

    // Fill with white background
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        strokeWidth = 3f
        style = android.graphics.Paint.Style.STROKE
        strokeJoin = android.graphics.Paint.Join.ROUND
        strokeCap = android.graphics.Paint.Cap.ROUND
        isAntiAlias = true
    }

    val path = android.graphics.Path()

    // Translate points to fit within the bitmap with padding
    points.forEach { point ->
        val x = point.x - minX + padding
        val y = point.y - minY + padding

        if (point.isStartPoint) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    canvas.drawPath(path, paint)
    return bitmap
}

private fun saveSignatureToFile(context: Context, bitmap: Bitmap): File {
    val file = File(context.cacheDir, "signature.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
}

// ViewModel for handling signature data
class SignatureViewModel : ViewModel() {
    private val _signatureFile = MutableStateFlow<File?>(null)
    val signatureFile: StateFlow<File?> = _signatureFile.asStateFlow()

    fun onSignatureSaved(file: File) {
        _signatureFile.value = file
    }
}