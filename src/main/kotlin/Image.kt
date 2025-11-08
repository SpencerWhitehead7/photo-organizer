import ImagePath.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.executeBlocking
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Size
import java.awt.Desktop
import java.awt.Desktop.getDesktop
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

val DESKTOP: Desktop = getDesktop()

enum class ImageFormat(val extension: String) {
    DNG("dng"),
    JPEG("jpeg"),
    JPG("jpg"),
    PNG("png"),
    WEBP("webp");
}

val SUPPORTED_IMAGES_FORMATS = ImageFormat.entries.map { it.extension }.toSet()

sealed class ImagePath {
    data class Dng(override val path: String, override val desktop: Desktop) : ImagePath()
    data class Jpeg(override val path: String, override val desktop: Desktop) : ImagePath()
    data class Jpg(override val path: String, override val desktop: Desktop) : ImagePath()
    data class Png(override val path: String, override val desktop: Desktop) : ImagePath()
    data class Webp(override val path: String, override val desktop: Desktop) : ImagePath()

    abstract val path: String
    abstract val desktop: Desktop

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
        return this.desktop.moveToTrash(this.asFile())
    }
}

class Image private constructor(
    val id: String,
    paths: List<ImagePath>
) {
    private var paths by mutableStateOf(paths)

    companion object {
        fun create(
            id: String,
            dng: String? = null,
            jpeg: String? = null,
            jpg: String? = null,
            png: String? = null,
            webp: String? = null,
            desktop: Desktop = DESKTOP
        ): Image {
            val paths = buildList {
                webp?.let { add(Webp(it, desktop)) }
                png?.let { add(Png(it, desktop)) }
                jpeg?.let { add(Jpeg(it, desktop)) }
                jpg?.let { add(Jpg(it, desktop)) }
                dng?.let { add(Dng(it, desktop)) }
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

    fun isVertical(ctx: PlatformContext): Boolean {
        val request = ImageRequest.Builder(ctx)
            .data(this.asFile())
            .size(Size.ORIGINAL)
            .precision(Precision.INEXACT)
            .build()

        return SingletonImageLoader
            .get(ctx)
            .executeBlocking(request)
            .image
            ?.let { it.width < it.height }
            ?: false
    }

    fun trash(format: ImageFormat? = null) {
        when (format) {
            null -> {
                paths.forEach { it.trash() }
                paths = emptyList()

            }

            else -> {
                val path = paths.find { it.format == format }
                path?.trash()
                paths = paths.filter { it != path }

            }
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
