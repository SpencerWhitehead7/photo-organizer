import androidx.compose.ui.awt.ComposeWindow
import java.awt.Desktop.getDesktop
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

object DiskOperations {
    private val desktop = getDesktop()

    fun trash(f: File): Boolean {
        return when (getOS()) {
            OS.MAC -> {
                this.desktop.moveToTrash(f)
            }
        }
    }
}

fun selectFilesViaUserPicker(filterDescription: String, filterExtensions: Set<String>): List<File>? {
    val fileChooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        isMultiSelectionEnabled = true
        fileFilter = FileNameExtensionFilter(filterDescription, *filterExtensions.toTypedArray())
    }

    val result = fileChooser.showOpenDialog(ComposeWindow())
    if (result != JFileChooser.APPROVE_OPTION) return null

    return fileChooser.selectedFiles.flatMap { file ->
        when {
            file.isFile -> {
                if (file.extension.lowercase() in filterExtensions) listOf(file)
                else emptyList()
            }

            file.isDirectory -> {
                file.walk()
                    .filter { it.isFile && it.extension.lowercase() in filterExtensions }
                    .toList()
            }

            else -> emptyList()
        }
    }
}