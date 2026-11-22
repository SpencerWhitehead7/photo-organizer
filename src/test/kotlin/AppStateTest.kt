import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class AppStateTest {
    @Test
    fun initialState() {
        val appState = AppState()

        assertFalse { appState.isImageGridPoppedOut }
        assertTrue(appState.images.isEmpty())
        assertTrue(appState.selectedImages.isEmpty())
    }

    @Test
    fun toggleIsImageGridPoppedOut() {
        val appState = AppState()

        assertFalse { appState.isImageGridPoppedOut }
        appState.toggleIsImageGridPoppedOut()
        assertTrue { appState.isImageGridPoppedOut }
        appState.toggleIsImageGridPoppedOut()
        assertFalse { appState.isImageGridPoppedOut }
    }

    @Test
    fun selectedImageFormats() {
        val allFormat = ImageFormat.entries
        val dngFormat = listOf(ImageFormat.DNG)
        val jpegFormat = listOf(ImageFormat.JPEG)
        val jpgFormat = listOf(ImageFormat.JPG)
        val pngFormat = listOf(ImageFormat.PNG)
        val webpFormat = listOf(ImageFormat.WEBP)

        val all = createImage(0, allFormat)
        val dng = createImage(0, dngFormat)
        val jpeg = createImage(0, jpegFormat)
        val jpg = createImage(0, jpgFormat)
        val png = createImage(0, pngFormat)
        val webp = createImage(0, webpFormat)

        val appState = AppState()

        appState.images = listOf(all, dng, jpeg, jpg, png, webp)

        assertEquals(emptySet(), appState.selectedImagesFormats)

        appState.selectToggleImage(all)
        assertEquals(allFormat.toSet(), appState.selectedImagesFormats)

        appState.selectToggleImage(dng)
        appState.selectToggleImage(jpeg)
        appState.selectToggleImage(jpg)
        appState.selectToggleImage(png)
        appState.selectToggleImage(webp)
        assertEquals(allFormat.toSet(), appState.selectedImagesFormats)

        appState.selectToggleImage(all)
        assertEquals(allFormat.toSet(), appState.selectedImagesFormats)

        appState.selectToggleImage(dng)
        assertEquals(
            setOf(ImageFormat.JPEG, ImageFormat.JPG, ImageFormat.PNG, ImageFormat.WEBP),
            appState.selectedImagesFormats
        )

        appState.selectToggleImage(jpeg)
        assertEquals(
            setOf(ImageFormat.JPG, ImageFormat.PNG, ImageFormat.WEBP),
            appState.selectedImagesFormats
        )

        appState.selectToggleImage(jpg)
        assertEquals(
            setOf(ImageFormat.PNG, ImageFormat.WEBP),
            appState.selectedImagesFormats
        )

        appState.selectToggleImage(png)
        assertEquals(
            setOf(ImageFormat.WEBP),
            appState.selectedImagesFormats
        )

        appState.selectToggleImage(webp)
        assertEquals(emptySet(), appState.selectedImagesFormats)

    }

    @Test
    fun selectImageByImage() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[0])
        assertEquals(setOf(images[0]), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex)
        assertEquals(0, appState.pivotRangeIndex)

        appState.selectImage(images[4])
        assertEquals(setOf(images[4]), appState.selectedImages)
        assertEquals(4, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImage(images[8])
        assertEquals(setOf(images[8]), appState.selectedImages)
        assertEquals(8, appState.mostRecentSelectedIndex)
        assertEquals(8, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageByIndex() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(0)
        assertEquals(setOf(images[0]), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex)
        assertEquals(0, appState.pivotRangeIndex)

        appState.selectImage(4)
        assertEquals(setOf(images[4]), appState.selectedImages)
        assertEquals(4, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImage(8)
        assertEquals(setOf(images[8]), appState.selectedImages)
        assertEquals(8, appState.mostRecentSelectedIndex)
        assertEquals(8, appState.pivotRangeIndex)
    }

    @Test
    fun selectToggleImage() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectToggleImage(images[0])
        assertEquals(setOf(images[0]), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex)
        assertEquals(0, appState.pivotRangeIndex)

        appState.selectToggleImage(images[4])
        assertEquals(setOf(images[0], images[4]), appState.selectedImages)
        assertEquals(4, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectToggleImage(images[8])
        assertEquals(setOf(images[0], images[4], images[8]), appState.selectedImages)
        assertEquals(8, appState.mostRecentSelectedIndex)
        assertEquals(8, appState.pivotRangeIndex)

        appState.selectToggleImage(images[4])
        assertEquals(setOf(images[0], images[8]), appState.selectedImages)
        assertEquals(8, appState.mostRecentSelectedIndex)
        assertEquals(8, appState.pivotRangeIndex)

        appState.selectToggleImage(images[8])
        assertEquals(setOf(images[0]), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex)
        assertEquals(0, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRange() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[6])
        assertEquals(images.slice(4..6).toSet(), appState.selectedImages)
        assertEquals(6, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeDefaultsToStartingAt0() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImageRange(images[2])

        assertEquals(images.slice(0..2).toSet(), appState.selectedImages)
        assertEquals(2, appState.mostRecentSelectedIndex)
        assertEquals(0, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeSelectsAndExtendsForward() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), appState.selectedImages)
        assertEquals(6, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeSelectsAndExtendsBackward() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), appState.selectedImages)
        assertEquals(2, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeSelectsAndShrinksForward() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), appState.selectedImages)
        assertEquals(6, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeSelectsAndShrinksBackward() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), appState.selectedImages)
        assertEquals(2, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeFlipsForwardAroundPivotIndex() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), appState.selectedImages)
        assertEquals(2, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeFlipsBackwardAroundPivotIndex() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), appState.selectedImages)
        assertEquals(6, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeAbsorbsForwardAdjacentRanges() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[7])
        appState.selectToggleImage(images[4])

        appState.selectImageRange(images[6])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)

        appState.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun selectImageRangeAbsorbsBackwardAdjacentRanges() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImage(images[1])
        appState.selectToggleImage(images[4])

        appState.selectImageRange(images[2])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
        appState.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun clearSelectedImages() {
        val appState = AppState()
        val images = List(3) { createImage(it) }
        appState.images = images

        appState.selectImage(1)

        appState.clearSelectedImages()

        assertEquals(setOf(), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex)
        assertEquals(0, appState.pivotRangeIndex)
    }

    @Test
    fun deleteSelectedImages() {
        val mockedDiskOperations = mockk<DiskOperations>()

        val appState = AppState()
        val images = List(3) { createImage(it, listOf(ImageFormat.DNG, ImageFormat.JPG), mockedDiskOperations) }
        appState.images = images

        appState.selectImage(1)

        appState.deleteSelectedImages()

        verify { mockedDiskOperations.trash(File("filename1.dng")) }
        verify { mockedDiskOperations.trash(File("filename1.jpg")) }
        assertEquals(listOf(images[0], images[2]), appState.images)
        assertEquals(setOf(images[2]), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(1, appState.pivotRangeIndex)
    }

    @Test
    fun deleteSelectedImagesOfFormat() {
        val mockedDiskOperations = mockk<DiskOperations>()

        val appState = AppState()
        val images = List(3) { createImage(it, listOf(ImageFormat.DNG, ImageFormat.JPG), mockedDiskOperations) }
        appState.images = images

        appState.selectImage(1)

        appState.deleteSelectedImages(ImageFormat.DNG)

        verify { mockedDiskOperations.trash(File("filename1.dng")) }
        assertEquals(images, appState.images)
        assertEquals(setOf(images[1]), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(1, appState.pivotRangeIndex)
    }

    @Test
    fun deleteSelectedImagesOfFormatHandlesPartiallyAndFullyDeletedImages() {
        val appState = AppState()
        val images = List(3) { createImage(it, listOf(ImageFormat.DNG, ImageFormat.JPG)) }
        appState.images = images

        appState.selectImage(1)

        appState.deleteSelectedImages(ImageFormat.DNG)

        assertEquals(images, appState.images)
        assertEquals(setOf(images[1]), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(1, appState.pivotRangeIndex)

        appState.deleteSelectedImages(ImageFormat.JPG)

        assertEquals(listOf(images[0], images[2]), appState.images)
        assertEquals(setOf(images[2]), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex)
        assertEquals(1, appState.pivotRangeIndex)
    }

    @Test
    fun deleteSelectedImagesOfFormatHandlesMixOfPartiallyAndFullyDeletedImages() {
        val appState = AppState()
        val images = List(9) { createImage(it, listOf(ImageFormat.DNG, ImageFormat.JPG)) }
        appState.images = images

        appState.selectToggleImage(images[4])
        appState.deleteSelectedImages(ImageFormat.JPG)
        appState.selectToggleImage(images[4])

        val imagesToSelect = listOf(0, 8, 4)
        imagesToSelect.forEach { appState.selectToggleImage(images[it]) }

        appState.deleteSelectedImages(ImageFormat.DNG)

        assertEquals(images.filterIndexed { idx, _ -> idx != 4 }, appState.images)
        assertEquals(setOf(images[0], images[8]), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex)
        assertEquals(7, appState.pivotRangeIndex)
    }

    @Test
    fun deleteSelectedImagesSelectsSameIndex() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        val imagesToSelect = listOf(0, 8, 4)
        imagesToSelect.forEach { appState.selectToggleImage(images[it]) }

        appState.deleteSelectedImages()

        assertEquals(images.filterIndexed { idx, _ -> !imagesToSelect.contains(idx) }, appState.images)
        assertEquals(setOf(images[6]), appState.selectedImages)
        assertEquals(4, appState.mostRecentSelectedIndex)
        assertEquals(4, appState.pivotRangeIndex)
    }

    @Test
    fun deleteSelectedImagesSelectsSameIndexCapped() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        val imagesToSelect = listOf(0, 4, 8)
        imagesToSelect.forEach { appState.selectToggleImage(images[it]) }

        appState.deleteSelectedImages()

        assertEquals(images.filterIndexed { idx, _ -> !imagesToSelect.contains(idx) }, appState.images)
        assertEquals(setOf(images[7]), appState.selectedImages)
        assertEquals(5, appState.mostRecentSelectedIndex)
        assertEquals(5, appState.pivotRangeIndex)
    }

    @Test
    fun deleteSelectedImagesAll() {
        val appState = AppState()
        val images = List(9) { createImage(it) }
        appState.images = images

        appState.selectImageRange(images[8])

        appState.deleteSelectedImages()

        assertEquals(listOf(), appState.images)
        assertEquals(setOf(), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex)
        assertEquals(0, appState.pivotRangeIndex)
    }
}