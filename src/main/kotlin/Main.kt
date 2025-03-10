import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
@Preview
fun App() {
    var imagePaths by remember { mutableStateOf<List<String>>(emptyList()) }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Button(onClick = {
                val fileChooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
                    isMultiSelectionEnabled = true
                    fileFilter = FileNameExtensionFilter(
                        "Images", *SUPPORTED_IMAGES.toTypedArray()
                    )
                }

                val result = fileChooser.showOpenDialog(ComposeWindow())
                if (result == JFileChooser.APPROVE_OPTION) {
                    imagePaths = fileChooser.selectedFiles.flatMap {
                        collectImages(File(it.absolutePath))
                    }
                }
            }) {
                Text("Select Files")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(imagePaths) { path ->
                    ImageFromDisk(
                        filepath = path,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
        }
    }
}


val SUPPORTED_IMAGES = setOf("jpg", "jpeg", "png", "gif", "bmp") // TODO: it's probably missing some
// also I need some way to support raws

fun collectImages(file: File): List<String> {
    return when {
        file.isFile -> {
            if (file.extension.lowercase() in SUPPORTED_IMAGES) listOf(file.absolutePath)
            else emptyList()
        }

        file.isDirectory -> {
            file.walk()
                .filter { it.isFile && it.extension.lowercase() in SUPPORTED_IMAGES }
                .map { it.absolutePath }
                .toList()
        }

        else -> emptyList()
    }
}

@Composable
fun ImageFromDisk(filepath: String, modifier: Modifier = Modifier) {
//    try {
        val bytes = File(filepath).readBytes()
        val image = org.jetbrains.skia.Image.makeFromEncoded(bytes)
        val imageBitmap = image.toComposeImageBitmap()

        Image(
            bitmap = imageBitmap,
            contentDescription = "Image from: $filepath",
            modifier = modifier
        )
//    TODO: error handling of some kind - cannot try/catch in composable function invocations
//    } catch (e: Exception) {
//        Text("Failed to load image from: $filepath")
//    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
