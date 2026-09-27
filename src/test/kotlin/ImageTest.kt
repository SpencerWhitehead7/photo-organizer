import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails


class ImageFormatTest() {
    @Test
    fun supported() {
        assertEquals(ImageFormat.entries.size, ImageFormat.SUPPORTED.size)
    }

    @Test
    fun format_to_use_order() {
        assertEquals(ImageFormat.entries.size, ImageFormat.FORMAT_TO_USE_ORDER.size)
    }

    @Test
    fun fromExtension() {
        assertEquals(ImageFormat.DNG, ImageFormat.fromExtension("dng"))
        assertEquals(ImageFormat.JPEG, ImageFormat.fromExtension("jpeg"))
        assertEquals(ImageFormat.JPG, ImageFormat.fromExtension("jpg"))
        assertEquals(ImageFormat.PNG, ImageFormat.fromExtension("png"))
        assertEquals(ImageFormat.WEBP, ImageFormat.fromExtension("webp"))

        assertFails { ImageFormat.fromExtension("test-ext") }
    }
}

class ImageTest {
    @Test
    fun oneFile() {
        val image = Image.create(
            "filename",
            listOf(
                File("filename.jpg"),
            ),
        )

        assertEquals(File("filename.jpg"), image.asFile())
        assertEquals(listOf(ImageFormat.JPG), image.formats())
    }

    @Test
    fun multipleFilesSameFormat() {
        val image = Image.create(
            "filename",
            listOf(
                File("filename.jpg"),
                File("1/filename.jpg"),
                File("1/2/filename.jpg"),
            ),
        )

        assertEquals(File("filename.jpg"), image.asFile())
        assertEquals(listOf(ImageFormat.JPG, ImageFormat.JPG, ImageFormat.JPG), image.formats())
    }

    @Test
    fun multipleFilesDifferentFormats() {
        val image = Image.create(
            "filename",
            listOf(
                File("filename.dng"),
                File("filename.jpg"),
                File("filename.png"),
                File("filename.webp"),
                File("filename.jpeg"),
            ),
        )

        assertEquals(File("filename.webp"), image.asFile())
        assertEquals(
            listOf(ImageFormat.WEBP, ImageFormat.PNG, ImageFormat.JPEG, ImageFormat.JPG, ImageFormat.DNG),
            image.formats()
        )
    }

    @Test
    fun multipleFilesSameAndDifferentFormats() {
        val image = Image.create(
            "filename",
            listOf(
                File("filename.dng"),
                File("filename.jpg"),
                File("filename.png"),
                File("1/filename.jpg"),
                File("filename.webp"),
                File("filename.jpeg"),
                File("1/2/filename.jpg"),

                ),
        )

        assertEquals(File("filename.webp"), image.asFile())
        assertEquals(
            listOf(
                ImageFormat.WEBP,
                ImageFormat.PNG,
                ImageFormat.JPEG,
                ImageFormat.JPG,
                ImageFormat.JPG,
                ImageFormat.JPG,
                ImageFormat.DNG
            ), image.formats()
        )
    }
}