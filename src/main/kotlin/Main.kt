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
import androidx.compose.ui.window.*
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import org.apache.commons.lang3.SystemUtils

@Composable
fun App(appState: AppState) {
    // 2 grid images + 3 gutters
    var explorerWidth by remember { mutableStateOf((200 * 2 + 8 * 3).dp) }

    if (appState.isImageGridPoppedOut) {
        Window(
            onCloseRequest = appState::toggleIsImageGridPoppedOut,
            title = "Photo Organizer Image Grid",
            state = WindowState(size = DpSize(explorerWidth, 1080.dp))
        ) {
            AppMenuBar(appState)
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                ) {
                    ImageGrid(appState)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                appState.clearSelectedImages()
            },
        horizontalAlignment = if (appState.images.isEmpty()) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = if (appState.images.isEmpty()) Arrangement.Center else Arrangement.Top,
    ) {
        if (appState.images.isEmpty()) {
            Button(
                onClick = { appState.load() }
            ) {
                Text("Load Images")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                if (!appState.isImageGridPoppedOut) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(explorerWidth),
                    ) {
                        ImageGrid(appState)
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
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    PreviewImage(appState.selectedImages.toList())
                }
            }
        }
    }
}

@Composable
fun FrameWindowScope.AppMenuBar(appState: AppState) {
    MenuBar {
        Menu("File") {
            Item(
                text = "Load Images",
                shortcut = KeyShortcut(key = Key.O, meta = true),
                onClick = appState::load,
            )
            Item(
                text = "Deleted Selected Images",
                shortcut =
                    if (SystemUtils.IS_OS_UNIX) KeyShortcut(key = Key.Backspace, meta = true)
                    else KeyShortcut(key = Key.Delete),
                onClick = appState::deleteSelectedImages,
            )
        }
        Menu("View") {
            Item(
                text = if (appState.isImageGridPoppedOut) "Dock Grid" else "Undock Grid",
                shortcut = KeyShortcut(key = Key.P, alt = true),
                onClick = appState::toggleIsImageGridPoppedOut
            )
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

    val appState = remember { AppState() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Photo Organizer",
        state = WindowState(size = DpSize(1920.dp, 1080.dp)),
    ) {
        AppMenuBar(appState)

        MaterialTheme(
            colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
        ) {
            App(appState)
        }
    }
}
