import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppStateTest {
    private fun createMockImage() = mockk<Image> {
        every { this@mockk.trash() } returns Unit
    }

    @Test
    fun testInitialState() {
        val appState = AppState()

        assertFalse { appState.isImageGridPoppedOut }
        assertTrue(appState.images.isEmpty())
        assertTrue(appState.selectedImages.isEmpty())
    }

    @Test
    fun testToggleIsImageGridPoppedOut() {
        val appState = AppState()

        assertFalse { appState.isImageGridPoppedOut }
        appState.toggleIsImageGridPoppedOut()
        assertTrue { appState.isImageGridPoppedOut }
        appState.toggleIsImageGridPoppedOut()
        assertFalse { appState.isImageGridPoppedOut }
    }

    @Test
    fun testSelectImageByImage() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[0])
        assertEquals(setOf(images[0]), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex())
        assertEquals(0, appState.pivotRangeIndex())

        appState.selectImage(images[4])
        assertEquals(setOf(images[4]), appState.selectedImages)
        assertEquals(4, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImage(images[8])
        assertEquals(setOf(images[8]), appState.selectedImages)
        assertEquals(8, appState.mostRecentSelectedIndex())
        assertEquals(8, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageByIndex() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(0)
        assertEquals(setOf(images[0]), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex())
        assertEquals(0, appState.pivotRangeIndex())

        appState.selectImage(4)
        assertEquals(setOf(images[4]), appState.selectedImages)
        assertEquals(4, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImage(8)
        assertEquals(setOf(images[8]), appState.selectedImages)
        assertEquals(8, appState.mostRecentSelectedIndex())
        assertEquals(8, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectToggleImage() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectToggleImage(images[0])
        assertEquals(setOf(images[0]), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex())
        assertEquals(0, appState.pivotRangeIndex())

        appState.selectToggleImage(images[4])
        assertEquals(setOf(images[0], images[4]), appState.selectedImages)
        assertEquals(4, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectToggleImage(images[8])
        assertEquals(setOf(images[0], images[4], images[8]), appState.selectedImages)
        assertEquals(8, appState.mostRecentSelectedIndex())
        assertEquals(8, appState.pivotRangeIndex())

        appState.selectToggleImage(images[4])
        assertEquals(setOf(images[0], images[8]), appState.selectedImages)
        assertEquals(8, appState.mostRecentSelectedIndex())
        assertEquals(8, appState.pivotRangeIndex())

        appState.selectToggleImage(images[8])
        assertEquals(setOf(images[0]), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex())
        assertEquals(0, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRange() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[6])
        assertEquals(images.slice(4..6).toSet(), appState.selectedImages)
        assertEquals(6, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeDefaultsToStartingAt0() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImageRange(images[2])

        assertEquals(images.slice(0..2).toSet(), appState.selectedImages)
        assertEquals(2, appState.mostRecentSelectedIndex())
        assertEquals(0, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeSelectsAndExtendsForward() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), appState.selectedImages)
        assertEquals(6, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeSelectsAndExtendsBackward() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), appState.selectedImages)
        assertEquals(2, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeSelectsAndShrinksForward() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), appState.selectedImages)
        assertEquals(6, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeSelectsAndShrinksBackward() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), appState.selectedImages)
        assertEquals(2, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeFlipsForwardAroundPivotIndex() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), appState.selectedImages)
        assertEquals(2, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeFlipsBackwardAroundPivotIndex() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[4])

        appState.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), appState.selectedImages)
        assertEquals(6, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeAbsorbsForwardAdjacentRanges() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[7])
        appState.selectToggleImage(images[4])

        appState.selectImageRange(images[6])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())

        appState.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeAbsorbsBackwardAdjacentRanges() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImage(images[1])
        appState.selectToggleImage(images[4])

        appState.selectImageRange(images[2])

        assertEquals(images.slice(1..4).toSet(), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
        appState.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), appState.selectedImages)
        assertEquals(7, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun clearSelectedImages() {
        val appState = AppState()
        val images = List(3) { createMockImage() }
        appState.images = images

        appState.selectImage(1)

        appState.clearSelectedImages()

        assertEquals(setOf(), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex())
        assertEquals(0, appState.pivotRangeIndex())
    }

    @Test
    fun testDeleteSelectedImages() {
        val appState = AppState()
        val images = List(3) { createMockImage() }
        appState.images = images

        appState.selectImage(1)

        appState.deleteSelectedImages()

        verify { images[1].trash() }
        assertEquals(listOf(images[0], images[2]), appState.images)
        assertEquals(setOf(images[2]), appState.selectedImages)
        assertEquals(1, appState.mostRecentSelectedIndex())
        assertEquals(1, appState.pivotRangeIndex())
    }

    @Test
    fun testDeleteSelectedImagesSelectsSameIndex() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        val imagesToSelect = listOf(0, 8, 4)

        imagesToSelect.forEach { appState.selectToggleImage(images[it]) }

        appState.deleteSelectedImages()

        assertEquals(images.filterIndexed { idx, _ -> !imagesToSelect.contains(idx) }, appState.images)
        assertEquals(setOf(images[6]), appState.selectedImages)
        assertEquals(4, appState.mostRecentSelectedIndex())
        assertEquals(4, appState.pivotRangeIndex())
    }

    @Test
    fun testDeleteSelectedImagesSelectsSameIndexCapped() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        val imagesToSelect = listOf(0, 4, 8)

        imagesToSelect.forEach { appState.selectToggleImage(images[it]) }

        appState.deleteSelectedImages()

        assertEquals(images.filterIndexed { idx, _ -> !imagesToSelect.contains(idx) }, appState.images)
        assertEquals(setOf(images[7]), appState.selectedImages)
        assertEquals(5, appState.mostRecentSelectedIndex())
        assertEquals(5, appState.pivotRangeIndex())
    }

    @Test
    fun testDeleteSelectedImagesAll() {
        val appState = AppState()
        val images = List(9) { createMockImage() }
        appState.images = images

        appState.selectImageRange(images[8])

        appState.deleteSelectedImages()

        images.forEach { verify { it.trash() } }
        assertEquals(listOf(), appState.images)
        assertEquals(setOf(), appState.selectedImages)
        assertEquals(0, appState.mostRecentSelectedIndex())
        assertEquals(0, appState.pivotRangeIndex())
    }
}