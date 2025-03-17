import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
@Preview
fun SelectImages(onSelectImages: (List<Image>) -> Unit) {
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

            val images = imagePaths
                .groupBy { File(it).nameWithoutExtension }
                .map { (filename, paths) ->
                    Image.create(
                        id = filename,
                        dng = paths.find { it.endsWith(".dng", ignoreCase = true) },
                        jpeg = paths.find { it.endsWith(".jpeg", ignoreCase = true) },
                        jpg = paths.find { it.endsWith(".jpg", ignoreCase = true) },
                        png = paths.find { it.endsWith(".png", ignoreCase = true) },
                        webp = paths.find { it.endsWith(".webp", ignoreCase = true) }
                    )
                }

            onSelectImages(images)
        }
    }) {
        Text("Select Photos")
    }
}
