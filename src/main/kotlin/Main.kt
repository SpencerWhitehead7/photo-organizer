import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import org.apache.commons.lang3.SystemUtils

@Composable
fun App(imagesState: ImagesState) {
    // 2 grid images + 3 gutters
    var explorerWidth by remember { mutableStateOf((200 * 2 + 8 * 3).dp) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                imagesState.clearSelectedImages()
            },
        horizontalAlignment = if (imagesState.images.isEmpty()) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = if (imagesState.images.isEmpty()) Arrangement.Center else Arrangement.Top,
    ) {
        if (imagesState.images.isEmpty()) {
            Button(
                onClick = { imagesState.load() }
            ) {
                Text("Load Images")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(explorerWidth),
                ) {
                    ImageGrid(imagesState)
                }

                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colors.onBackground)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                explorerWidth = explorerWidth + (dragAmount.x * 0.5).dp
                            }
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    PreviewImage(imagesState.selectedImages.toList())
                }
            }
        }
    }
}


fun main() = application {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
//            .allowConversionToBitmap(true) // TODO: necessary?
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache(
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.80)
                    .build()
            )
            .crossfade(128)
            .build()
    }

    val imagesState = remember { ImagesState() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Photo Organizer",
        state = WindowState(size = DpSize(1920.dp, 1080.dp)),
    ) {
        MenuBar {
            Menu("File") {
                Item(
                    text = "Load Images",
                    shortcut = KeyShortcut(key = Key.O, meta = true),
                    onClick = imagesState::load,
                )
                Item(
                    text = "Deleted Selected Images",
                    shortcut =
                        if (SystemUtils.IS_OS_UNIX) KeyShortcut(key = Key.Backspace, meta = true)
                        else KeyShortcut(key = Key.Delete),
                    onClick = imagesState::deleteSelectedImages,
                )
            }
        }

        MaterialTheme(
            colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
        ) {
            App(imagesState)
        }
    }
}
