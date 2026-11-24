import ImageFile.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.toLowerCase
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.executeBlocking
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Size
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2

enum class ImageFormat(val extension: String) {
    DNG("dng"),
    JPEG("jpeg"),
    JPG("jpg"),
    PNG("png"),
    WEBP("webp");

    companion object {
        val SUPPORTED = entries.map { it.extension }.toSet()
        fun isSupported(f: File) = f.extension.lowercase() in SUPPORTED

        val FORMAT_TO_USE_ORDER = listOf(
            WEBP,
            PNG,
            JPEG,
            JPG,
            DNG,
        )
            .withIndex()
            .associate { (idx, fmt) -> fmt to idx }

        private val byExtension = entries.associateBy { it.extension }
        fun fromExtension(extension: String): ImageFormat = byExtension[extension]!!
    }

    fun color(isLight: Boolean): Color = when (this) {
        DNG -> if (isLight) Color(0xFFBA8F3A)
        else Color(0xFFFFD54F)

        JPEG -> if (isLight) Color(0xFF0D47A1)
        else Color(0xFF90CAF9)


        JPG -> if (isLight) Color(0xFF00695C)
        else Color(0xFF80CBC4)

        PNG -> if (isLight) Color(0xFF2E7D32)
        else Color(0xFFA5D6A7)

        WEBP -> if (isLight) Color(0xFF6A1B9A)
        else Color(0xFFCE93D8)
    }
}

sealed class ImageFile {
    data class Dng(override val file: File) : ImageFile()
    data class Jpeg(override val file: File) : ImageFile()
    data class Jpg(override val file: File) : ImageFile()
    data class Png(override val file: File) : ImageFile()
    data class Webp(override val file: File) : ImageFile()

    abstract val file: File

    val format: ImageFormat
        get() = when (this) {
            is Dng -> ImageFormat.DNG
            is Jpeg -> ImageFormat.JPEG
            is Jpg -> ImageFormat.JPG
            is Png -> ImageFormat.PNG
            is Webp -> ImageFormat.WEBP
        }
}

class Image private constructor(
    val id: String,
    imageFiles: List<ImageFile>,
    private val diskOperations: DiskOperations,
) {
    private var imageFiles by mutableStateOf(imageFiles)

    companion object {
        fun create(
            id: String,
            files: List<File>,
            diskOperations: DiskOperations = DiskOperations,
        ): Image {
            val imageFiles: List<ImageFile> = files
                .groupBy { ImageFormat.fromExtension(it.extension.lowercase()) }
                .toSortedMap(compareBy { ImageFormat.FORMAT_TO_USE_ORDER[it] })
                .flatMap { (ext, files) ->
                    when (ext) {
                        ImageFormat.DNG -> files.map { Dng(it) }
                        ImageFormat.JPG -> files.map { Jpg(it) }
                        ImageFormat.JPEG -> files.map { Jpeg(it) }
                        ImageFormat.PNG -> files.map { Png(it) }
                        ImageFormat.WEBP -> files.map { Webp(it) }
                    }
                }
            require(imageFiles.isNotEmpty()) { "At least one supported format image must be provided" }

            return Image(id, imageFiles, diskOperations)
        }
    }

    fun asFile(): File {
        return this.imageFiles.first().file
    }

    fun formats(): List<ImageFormat> {
        return this.imageFiles.map { it.format }
    }

    fun filePaths(): List<Pair<ImageFormat, String>> {
        return this.imageFiles.map { it.format to it.file.path }
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
        val (filesToTrash, filesToRetain) = when (format) {
            null -> this.imageFiles to emptyList()
            else -> this.imageFiles.partition { it.format == format }
        }

        filesToTrash.forEach { this.diskOperations.trash(it.file) }
        this.imageFiles = filesToRetain
    }
}

fun loadImages(sources: List<File>): List<Image> {
    return sources.flatMap { source ->
        when {
            source.isFile ->
                if (ImageFormat.isSupported(source)) listOf(source)
                else emptyList()

            source.isDirectory ->
                source
                    .walk()
                    .filter { it.isFile && ImageFormat.isSupported(it) }
                    .toList()

            else -> emptyList()
        }
    }
        .groupBy { it.nameWithoutExtension }
        .map { (filename, imageFiles) -> Image.create(filename, imageFiles) }
}
