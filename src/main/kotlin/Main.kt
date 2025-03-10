import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
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
import org.jetbrains.skia.Image
import java.io.File

@Composable
@Preview
fun App() {
    var selectedFilePath by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Button(onClick = {
            val fd = java.awt.FileDialog(ComposeWindow())
            fd.isVisible = true
            selectedFilePath = fd.directory + fd.file
        }) {
            Text("Select File")
        }
        selectedFilePath?.let {
            ImageFromDisk(it)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Composable
fun ImageFromDisk(filepath: String) {
    val bytes = File(filepath).readBytes()
    val image = Image.makeFromEncoded(bytes)
    val imageBitmap = image.toComposeImageBitmap()

    Image(
        bitmap = imageBitmap,
        contentDescription = "Loaded image",
        modifier = Modifier.size(400.dp)
    )
}