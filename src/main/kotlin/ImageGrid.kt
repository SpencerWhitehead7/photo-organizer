import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ImageGrid(appState: AppState) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    fun onTabNavigate(image: Image, offset: Int) {
        val newSelectedIndex = (appState.images.indexOf(image) + offset).coerceIn(0, appState.images.lastIndex)

        appState.selectImage(newSelectedIndex)
    }

    fun onArrowNavigate(key: Key, isRange: Boolean) {
        // TODO: scrolling bug
        // This all seems to work, except that when the function runs during an ongoing
        // user scroll, it just refuses to scroll to the arrow-selected item.
        // No attempt to fix this has worked at all; not cancelling scrolls, not disabling user scrolling,
        // not dispatching a scroll to snap to current position and then scrolling, nothing.
        // This seems like an incredibly common use case, so I'm baffled nothing works, but
        // maybe on mobile it can't really happen so they haven't bothered to deal with it?

        val lastSelectedIndex = appState.mostRecentSelectedIndex
        val columnCount = gridState.layoutInfo.visibleItemsInfo.firstOrNull()?.let { info ->
            gridState.layoutInfo.visibleItemsInfo.count { it.offset.y == info.offset.y }
        } ?: 1
        val newIndex = when (key) {
            Key.DirectionRight -> (lastSelectedIndex + 1).coerceAtMost(appState.images.lastIndex)
            Key.DirectionLeft -> (lastSelectedIndex - 1).coerceAtLeast(0)
            Key.DirectionDown -> (lastSelectedIndex + columnCount).coerceAtMost(appState.images.lastIndex)
            Key.DirectionUp -> (lastSelectedIndex - columnCount).coerceAtLeast(0)
            else -> return
        }
        if (isRange) appState.selectImageRange(newIndex)
        else appState.selectImage(newIndex)

        val gridItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == newIndex }

        val firstVisibleItem = gridState.layoutInfo.visibleItemsInfo.first()
        val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.last()

        val isFullyAboveViewport = newIndex < firstVisibleItem.index
        val isFullyBelowViewport = newIndex > lastVisibleItem.index
        if (gridItem == null || isFullyAboveViewport || isFullyBelowViewport) {
            coroutineScope.launch {
                gridState.animateScrollToItem(
                    newIndex,
                    if (isFullyAboveViewport) 0
                    else -(gridState.layoutInfo.viewportSize.height - 200 - 8) // height of an image + padding
                )
            }
            return
        }

        val viewportStart = gridState.layoutInfo.viewportStartOffset
        val viewportEnd = gridState.layoutInfo.viewportEndOffset

        val itemStart = gridItem.offset.y
        val itemEnd = gridItem.offset.y + gridItem.size.height

        val isPartiallyAboveViewport = viewportStart in (itemStart + 1)..<itemEnd
        val isPartiallyBelowViewport = viewportEnd in (itemStart + 1)..<itemEnd
        if (isPartiallyAboveViewport || isPartiallyBelowViewport) {
            coroutineScope.launch {
                gridState.animateScrollToItem(
                    newIndex,
                    if (isPartiallyAboveViewport) 0
                    else -(gridState.layoutInfo.viewportSize.height - gridItem.size.height)
                )
            }
            return
        }
    }

    Box {
        ContextMenuArea(items = {
            if (appState.selectedImages.isNotEmpty()) {
                listOf(
                    ContextMenuItem("Delete Selected Images") {
                        appState.deleteSelectedImages()
                    },
                ) + appState.selectedImagesFormats.map {
                    ContextMenuItem("Deleted Selected ${it}s") {
                        appState.deleteSelectedImages(it)
                    }
                }
            } else emptyList()
        }) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(appState.images, key = { it }) {
                    GridImage(
                        image = it,
                        isSelected = it in appState.selectedImages,
                        onClick = { appState.selectImage(it) },
                        onCtrlClick = { appState.selectToggleImage(it) },
                        onShiftClick = { appState.selectImageRange(it) },
                        onTab = { onTabNavigate(it, 1) },
                        onShiftTab = { onTabNavigate(it, -1) },
                        onArrowKey = { key, isRange -> onArrowNavigate(key, isRange) },
                    )
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = gridState)
        )
    }
}
