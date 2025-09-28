import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

class AppState {
    var images by mutableStateOf(listOf<Image>())
    var selectedImages by mutableStateOf(setOf<Image>())
        private set
    private var orderedSelectedForMostRecentIndex by mutableStateOf(linkedSetOf<Image>())
    private var orderedSelectedForRangePivotIndex by mutableStateOf(linkedSetOf<Image>())

    fun mostRecentSelectedIndex(): Int {
        return orderedSelectedForMostRecentIndex.lastOrNull()?.let { images.indexOf(it) } ?: 0
    }

    fun pivotRangeIndex(): Int {
        return orderedSelectedForRangePivotIndex.lastOrNull()?.let { images.indexOf(it) } ?: 0
    }

    fun load() {
        loadImagesFromDisk()?.let { newImages ->
            images = newImages.sortedBy { it.id } // TODO: add other sorting mechanisms
            images.firstOrNull()?.let {
                selectedImages = setOf(it)
                orderedSelectedForMostRecentIndex = linkedSetOf(it)
                orderedSelectedForRangePivotIndex = linkedSetOf(it)
            }
        }
    }

    fun selectImage(newImage: Image) {
        selectedImages = setOf(newImage)
        orderedSelectedForMostRecentIndex = linkedSetOf(newImage)
        orderedSelectedForRangePivotIndex = linkedSetOf(newImage)
    }

    fun selectImage(newImageIndex: Int) {
        selectImage(images[newImageIndex])
    }

    fun selectToggleImage(newImage: Image) {
        if (newImage in selectedImages) {
            selectedImages -= newImage
            orderedSelectedForMostRecentIndex -= newImage
            orderedSelectedForRangePivotIndex -= newImage
        } else {
            selectedImages += newImage
            orderedSelectedForMostRecentIndex += newImage
            orderedSelectedForRangePivotIndex += newImage
        }
    }

    fun selectImageRange(newImage: Image) {
        val updatedSelectedImages = selectedImages.toMutableSet()
        val updatedOrderedSelectedForLastIndex = orderedSelectedForMostRecentIndex.toMutableSet()
        val updatedOrderedSelectedForRangePivotIndex = orderedSelectedForRangePivotIndex.toMutableSet()

        val pivotIdx = pivotRangeIndex()

        var startRangeIdx = pivotIdx - 1
        while (startRangeIdx >= 0 && images[startRangeIdx] in updatedSelectedImages) {
            startRangeIdx--
        }
        var endRangeIdx = pivotIdx + 1
        while (endRangeIdx <= images.lastIndex && images[endRangeIdx] in updatedSelectedImages) {
            endRangeIdx++
        }
        val pivotSelectedBlock = (startRangeIdx + 1..endRangeIdx - 1).map { images[it] }
        updatedSelectedImages -= pivotSelectedBlock
        updatedOrderedSelectedForLastIndex -= pivotSelectedBlock
        updatedOrderedSelectedForRangePivotIndex -= pivotSelectedBlock

        val newIdx = images.indexOf(newImage)

        startRangeIdx = newIdx - 1
        while (startRangeIdx >= 0 && images[startRangeIdx] in updatedSelectedImages) {
            startRangeIdx--
        }
        endRangeIdx = newIdx + 1
        while (endRangeIdx <= images.lastIndex && images[endRangeIdx] in updatedSelectedImages) {
            endRangeIdx++
        }
        val newSelectedBlock = (startRangeIdx + 1..endRangeIdx - 1).map { images[it] }
        updatedSelectedImages -= newSelectedBlock
        updatedOrderedSelectedForLastIndex -= newSelectedBlock
        updatedOrderedSelectedForRangePivotIndex -= newSelectedBlock

        val isSelectingForward = pivotIdx < newIdx
        val startIdx =
            if (isSelectingForward) pivotIdx
            else min(newIdx, startRangeIdx + 1)
        val endIdx =
            if (isSelectingForward) max(newIdx, endRangeIdx - 1)
            else pivotIdx
        val finalSelectedBlock = (startIdx..endIdx).map { images[it] }
        val finalSelectedBlockReversed = finalSelectedBlock.reversed()

        updatedSelectedImages += finalSelectedBlock
        if (isSelectingForward) {
            updatedOrderedSelectedForLastIndex += finalSelectedBlock
            updatedOrderedSelectedForRangePivotIndex += finalSelectedBlockReversed
        } else {
            updatedOrderedSelectedForLastIndex += finalSelectedBlockReversed
            updatedOrderedSelectedForRangePivotIndex += finalSelectedBlock
        }

        selectedImages = updatedSelectedImages
        orderedSelectedForMostRecentIndex = updatedOrderedSelectedForLastIndex.toCollection(LinkedHashSet())
        orderedSelectedForRangePivotIndex = updatedOrderedSelectedForRangePivotIndex.toCollection(LinkedHashSet())
    }

    fun selectImageRange(rangeImageIndex: Int) {
        selectImageRange(images[rangeImageIndex])
    }

    fun clearSelectedImages() {
        selectedImages = setOf()
        orderedSelectedForMostRecentIndex = linkedSetOf()
        orderedSelectedForRangePivotIndex = linkedSetOf()
    }

    fun deleteSelectedImages() {
        selectedImages.forEach { it.trash() }

        val lastSelectedImageIndex = mostRecentSelectedIndex()

        images = images.filter { it !in selectedImages }
        if (images.isEmpty()) {
            clearSelectedImages()
        } else {
            selectImage(lastSelectedImageIndex.coerceAtMost(images.lastIndex))
        }
    }
}
