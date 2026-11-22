import io.mockk.every
import io.mockk.mockk
import java.io.File

fun createImage(
    suffix: Int = 0,
    formats: List<ImageFormat> = listOf(ImageFormat.JPG),
    mockedDiskOperations: DiskOperations = mockk<DiskOperations>()
): Image {
    every { mockedDiskOperations.trash(any<File>()) } returns true

    return Image.create(
        id = "filename$suffix",
        files = formats.map { File("filename$suffix.${it.extension}") },
        diskOperations = mockedDiskOperations
    )
}