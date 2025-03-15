import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

val SUPPORTED_IMAGES_FORMATS = setOf("jpg", "jpeg", "png", "dng") // TODO: it's probably missing some

@Composable
@Preview
fun SelectImages(onSelectImages: (List<String>) -> Unit) {
    return Button(onClick = {
        val fileChooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
            isMultiSelectionEnabled = true
            fileFilter = FileNameExtensionFilter(
                "Images", *SUPPORTED_IMAGES_FORMATS.toTypedArray()
            )
        }

        val result = fileChooser.showOpenDialog(ComposeWindow())

        if (result == JFileChooser.APPROVE_OPTION) {
            val imagePaths = fileChooser.selectedFiles.flatMap { filePath ->
                val file = File(filePath.absolutePath)

                when {
                    file.isFile -> {
                        if (file.extension.lowercase() in SUPPORTED_IMAGES_FORMATS) listOf<String>(file.absolutePath)
                        else emptyList()
                    }

                    file.isDirectory -> {
                        file.walk()
                            .filter { it.isFile && it.extension.lowercase() in SUPPORTED_IMAGES_FORMATS }
                            .map<File, String> { it.absolutePath }
                            .toList()
                    }

                    else -> emptyList()
                }
            }
            onSelectImages(imagePaths)
        }
    }) {
        Text("Select Photos")
    }
}
