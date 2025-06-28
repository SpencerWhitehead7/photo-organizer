import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImagesStateTest {
    private fun createMockImage() = mockk<Image> {
        every { this@mockk.trash() } returns Unit
    }

    @Test
    fun testInitialState() {
        val state = ImagesState()

        assertTrue(state.images.isEmpty())
        assertTrue(state.selectedImages.isEmpty())
    }

    @Test
    fun testSelectImageByImage() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[0])
        assertEquals(setOf(images[0]), state.selectedImages)
        assertEquals(0, state.mostRecentSelectedIndex())
        assertEquals(0, state.pivotRangeIndex())

        state.selectImage(images[4])
        assertEquals(setOf(images[4]), state.selectedImages)
        assertEquals(4, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImage(images[8])
        assertEquals(setOf(images[8]), state.selectedImages)
        assertEquals(8, state.mostRecentSelectedIndex())
        assertEquals(8, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageByIndex() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(0)
        assertEquals(setOf(images[0]), state.selectedImages)
        assertEquals(0, state.mostRecentSelectedIndex())
        assertEquals(0, state.pivotRangeIndex())

        state.selectImage(4)
        assertEquals(setOf(images[4]), state.selectedImages)
        assertEquals(4, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImage(8)
        assertEquals(setOf(images[8]), state.selectedImages)
        assertEquals(8, state.mostRecentSelectedIndex())
        assertEquals(8, state.pivotRangeIndex())
    }

    @Test
    fun testSelectToggleImage() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectToggleImage(images[0])
        assertEquals(setOf(images[0]), state.selectedImages)
        assertEquals(0, state.mostRecentSelectedIndex())
        assertEquals(0, state.pivotRangeIndex())

        state.selectToggleImage(images[4])
        assertEquals(setOf(images[0], images[4]), state.selectedImages)
        assertEquals(4, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectToggleImage(images[8])
        assertEquals(setOf(images[0], images[4], images[8]), state.selectedImages)
        assertEquals(8, state.mostRecentSelectedIndex())
        assertEquals(8, state.pivotRangeIndex())

        state.selectToggleImage(images[4])
        assertEquals(setOf(images[0], images[8]), state.selectedImages)
        assertEquals(8, state.mostRecentSelectedIndex())
        assertEquals(8, state.pivotRangeIndex())

        state.selectToggleImage(images[8])
        assertEquals(setOf(images[0]), state.selectedImages)
        assertEquals(0, state.mostRecentSelectedIndex())
        assertEquals(0, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRange() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[4])

        state.selectImageRange(images[6])
        assertEquals(images.slice(4..6).toSet(), state.selectedImages)
        assertEquals(6, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeDefaultsToStartingAt0() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImageRange(images[2])

        assertEquals(images.slice(0..2).toSet(), state.selectedImages)
        assertEquals(2, state.mostRecentSelectedIndex())
        assertEquals(0, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeSelectsAndExtendsForward() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[4])

        state.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), state.selectedImages)
        assertEquals(6, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), state.selectedImages)
        assertEquals(7, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeSelectsAndExtendsBackward() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[4])

        state.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), state.selectedImages)
        assertEquals(2, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), state.selectedImages)
        assertEquals(1, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeSelectsAndShrinksForward() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[4])

        state.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), state.selectedImages)
        assertEquals(7, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), state.selectedImages)
        assertEquals(6, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeSelectsAndShrinksBackward() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[4])

        state.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), state.selectedImages)
        assertEquals(1, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), state.selectedImages)
        assertEquals(2, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeFlipsForwardAroundPivotIndex() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[4])

        state.selectImageRange(images[2])

        assertEquals(images.slice(2..4).toSet(), state.selectedImages)
        assertEquals(2, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), state.selectedImages)
        assertEquals(7, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeFlipsBackwardAroundPivotIndex() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[4])

        state.selectImageRange(images[6])

        assertEquals(images.slice(4..6).toSet(), state.selectedImages)
        assertEquals(6, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), state.selectedImages)
        assertEquals(1, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeAbsorbsForwardAdjacentRanges() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[7])
        state.selectToggleImage(images[4])

        state.selectImageRange(images[6])

        assertEquals(images.slice(4..7).toSet(), state.selectedImages)
        assertEquals(7, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())

        state.selectImageRange(images[1])

        assertEquals(images.slice(1..4).toSet(), state.selectedImages)
        assertEquals(1, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testSelectImageRangeAbsorbsBackwardAdjacentRanges() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImage(images[1])
        state.selectToggleImage(images[4])

        state.selectImageRange(images[2])

        assertEquals(images.slice(1..4).toSet(), state.selectedImages)
        assertEquals(1, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
        state.selectImageRange(images[7])

        assertEquals(images.slice(4..7).toSet(), state.selectedImages)
        assertEquals(7, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun clearSelectedImages() {
        val state = ImagesState()
        val images = List(3) { createMockImage() }
        state.images = images

        state.selectImage(1)

        state.clearSelectedImages()

        assertEquals(setOf(), state.selectedImages)
        assertEquals(0, state.mostRecentSelectedIndex())
        assertEquals(0, state.pivotRangeIndex())
    }

    @Test
    fun testDeleteSelectedImages() {
        val state = ImagesState()
        val images = List(3) { createMockImage() }
        state.images = images

        state.selectImage(1)

        state.deleteSelectedImages()

        verify { images[1].trash() }
        assertEquals(listOf(images[0], images[2]), state.images)
        assertEquals(setOf(images[2]), state.selectedImages)
        assertEquals(1, state.mostRecentSelectedIndex())
        assertEquals(1, state.pivotRangeIndex())
    }

    @Test
    fun testDeleteSelectedImagesSelectsSameIndex() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        val imagesToSelect = listOf(0, 8, 4)

        imagesToSelect.forEach { state.selectToggleImage(images[it]) }

        state.deleteSelectedImages()

        assertEquals(images.filterIndexed { idx, _ -> !imagesToSelect.contains(idx) }, state.images)
        assertEquals(setOf(images[6]), state.selectedImages)
        assertEquals(4, state.mostRecentSelectedIndex())
        assertEquals(4, state.pivotRangeIndex())
    }

    @Test
    fun testDeleteSelectedImagesSelectsSameIndexCapped() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        val imagesToSelect = listOf(0, 4, 8)

        imagesToSelect.forEach { state.selectToggleImage(images[it]) }

        state.deleteSelectedImages()

        assertEquals(images.filterIndexed { idx, _ -> !imagesToSelect.contains(idx) }, state.images)
        assertEquals(setOf(images[7]), state.selectedImages)
        assertEquals(5, state.mostRecentSelectedIndex())
        assertEquals(5, state.pivotRangeIndex())
    }

    @Test
    fun testDeleteSelectedImagesAll() {
        val state = ImagesState()
        val images = List(9) { createMockImage() }
        state.images = images

        state.selectImageRange(images[8])

        state.deleteSelectedImages()

        images.forEach { verify { it.trash() } }
        assertEquals(listOf(), state.images)
        assertEquals(setOf(), state.selectedImages)
        assertEquals(0, state.mostRecentSelectedIndex())
        assertEquals(0, state.pivotRangeIndex())
    }
}