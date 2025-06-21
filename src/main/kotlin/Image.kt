import androidx.compose.ui.awt.ComposeWindow
import java.awt.Desktop.getDesktop
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

enum class ImageFormat(val extension: String) {
    DNG("dng"),
    JPEG("jpeg"),
    JPG("jpg"),
    PNG("png"),
    WEBP("webp");
}

val SUPPORTED_IMAGES_FORMATS = ImageFormat.entries.map { it.extension }.toSet()

sealed class ImagePath {
    data class Dng(override val path: String) : ImagePath()
    data class Jpeg(override val path: String) : ImagePath()
    data class Jpg(override val path: String) : ImagePath()
    data class Png(override val path: String) : ImagePath()
    data class Webp(override val path: String) : ImagePath()

    abstract val path: String

    val format: ImageFormat
        get() = when (this) {
            is Dng -> ImageFormat.DNG
            is Jpeg -> ImageFormat.JPEG
            is Jpg -> ImageFormat.JPG
            is Png -> ImageFormat.PNG
            is Webp -> ImageFormat.WEBP
        }

    fun asFile(): File {
        return File(path)
    }

    fun trash(): Boolean {
        // TODO: does it work on windows? linux? fallbacks? error handling?
        return getDesktop().moveToTrash(this.asFile())
    }
}

class Image private constructor(
    val id: String,
    private val paths: List<ImagePath>
) {
    companion object {
        fun create(
            id: String,
            dng: String? = null,
            jpeg: String? = null,
            jpg: String? = null,
            png: String? = null,
            webp: String? = null
        ): Image {
            val paths = buildList {
                webp?.let { add(Webp(it)) }
                png?.let { add(Png(it)) }
                jpeg?.let { add(Jpeg(it)) }
                jpg?.let { add(Jpg(it)) }
                dng?.let { add(Dng(it)) }
            }

            require(paths.isNotEmpty()) { "At least one image format must be provided" }
            return Image(id, paths)
        }
    }

    fun asFile(): File {
        return paths.first().asFile()
    }

    fun formats(): List<ImageFormat> {
        return paths.map { it.format }
    }

    fun trash(format: ImageFormat? = null) {
        when (format) {
            null -> paths.forEach { it.trash() }
            else -> paths.find { it.format == format }?.trash()
        }
    }
}

fun loadImagesFromDisk(): List<Image>? {
    val fileChooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        isMultiSelectionEnabled = true
        fileFilter = FileNameExtensionFilter(
            "Images", *SUPPORTED_IMAGES_FORMATS.toTypedArray()
        )
    }

    val result = fileChooser.showOpenDialog(ComposeWindow())
    if (result != JFileChooser.APPROVE_OPTION) return null

    val imagePaths = fileChooser.selectedFiles.flatMap { filePath ->
        val file = File(filePath.absolutePath)

        when {
            file.isFile -> {
                if (file.extension.lowercase() in SUPPORTED_IMAGES_FORMATS) listOf(file.absolutePath)
                else emptyList()
            }

            file.isDirectory -> {
                file.walk()
                    .filter { it.isFile && it.extension.lowercase() in SUPPORTED_IMAGES_FORMATS }
                    .map { it.absolutePath }
                    .toList()
            }

            else -> emptyList()
        }
    }

    return imagePaths
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
}
