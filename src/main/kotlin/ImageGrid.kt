import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.apache.commons.lang3.SystemUtils

@Composable
fun ImageGrid(appState: AppState) {
    val gridState = rememberLazyGridState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val keyboardModifiers = LocalWindowInfo.current.keyboardModifiers
    val isCtrlPressed =
        if (SystemUtils.IS_OS_UNIX) keyboardModifiers.isMetaPressed
        else keyboardModifiers.isCtrlPressed

    fun scrollToItem(itemIndex: Int) {
        // TODO: scrolling bug
        // This all seems fine, except that when the function runs during an ongoing
        // user scroll, it just refuses to scroll to the selected item.
        // No attempt to fix this has worked at all; not cancelling scrolls, not disabling user scrolling,
        // not dispatching a scroll to snap to current position and then scrolling, nothing.
        // This seems like an incredibly common issue, so I'm baffled nothing works, but maybe
        // on mobile it can't really happen so they haven't bothered to deal with it?

        val gridItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == itemIndex }

        val itemHeight = 200 // it'll be fun to figure out the
        val gutterHeight = 8 // bug if I ever change these
        val offsetDown = gridState.layoutInfo.viewportSize.height - itemHeight - gutterHeight

        val (isAboveViewport, isBelowViewport) = if (gridItem != null) {
            val itemRange = (gridItem.offset.y - gutterHeight)..(gridItem.offset.y + itemHeight + gutterHeight)

            val isPartlyAboveViewport = gridState.layoutInfo.viewportStartOffset in itemRange
            val isPartlyBelowViewport = gridState.layoutInfo.viewportEndOffset in itemRange

            Pair(isPartlyAboveViewport, isPartlyBelowViewport)
        } else {
            val isFullyAboveViewport = itemIndex < gridState.layoutInfo.visibleItemsInfo.first().index
            val isFullyBelowViewport = itemIndex > gridState.layoutInfo.visibleItemsInfo.last().index

            Pair(isFullyAboveViewport, isFullyBelowViewport)
        }

        val offset = if (isAboveViewport) 0
        else if (isBelowViewport) -offsetDown
        else return

        coroutineScope.launch {
            gridState.animateScrollToItem(itemIndex, offset)
        }
    }

    fun onTabNavigate(offset: Int) {
        val newSelectedIndex = (appState.mostRecentSelectedIndex + offset).coerceIn(0, appState.images.lastIndex)

        appState.selectImage(newSelectedIndex)

        scrollToItem(newSelectedIndex)
    }

    fun onArrowNavigate(key: Key, isRange: Boolean) {
        val lastSelectedIndex = appState.mostRecentSelectedIndex
        val columnCount = gridState.layoutInfo.visibleItemsInfo.firstOrNull()?.let { info ->
            gridState.layoutInfo.visibleItemsInfo.count { it.offset.y == info.offset.y }
        } ?: 1
        val newSelectedIndex = when (key) {
            Key.DirectionRight -> (lastSelectedIndex + 1).coerceAtMost(appState.images.lastIndex)
            Key.DirectionLeft -> (lastSelectedIndex - 1).coerceAtLeast(0)
            Key.DirectionDown -> (lastSelectedIndex + columnCount).coerceAtMost(appState.images.lastIndex)
            Key.DirectionUp -> (lastSelectedIndex - columnCount).coerceAtLeast(0)
            else -> return
        }

        if (isRange) appState.selectImageRange(newSelectedIndex)
        else appState.selectImage(newSelectedIndex)

        scrollToItem(newSelectedIndex)
    }

    LaunchedEffect(appState.images, appState.selectedImages) {
        if (appState.selectedImages.isNotEmpty()) {
            focusRequester.requestFocus()
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
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.Tab -> {
                                    val offset = if (event.isShiftPressed) -1 else 1
                                    onTabNavigate(offset)
                                    true
                                }

                                Key.DirectionLeft,
                                Key.DirectionRight,
                                Key.DirectionUp,
                                Key.DirectionDown -> {
                                    if (!isCtrlPressed) onArrowNavigate(event.key, event.isShiftPressed)
                                    true
                                }

                                else -> false
                            }
                        } else {
                            false
                        }
                    }
            ) {
                items(appState.images, key = { it }) {
                    GridImage(
                        image = it,
                        isSelected = it in appState.selectedImages,
                        onClick = { appState.selectImage(it) },
                        onCtrlClick = { appState.selectToggleImage(it) },
                        onShiftClick = { appState.selectImageRange(it) },
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
