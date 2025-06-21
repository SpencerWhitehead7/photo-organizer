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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.apache.commons.lang3.SystemUtils

@Composable
fun App(
    images: List<Image>,
    loadImages: () -> Unit,
    selectedImage: Image?,
    setSelectedImage: (Image?) -> Unit,
    deleteSelectedImage: () -> Unit,
) {
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
                setSelectedImage(null)
            },
        horizontalAlignment = if (images.isEmpty()) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = if (images.isEmpty()) Arrangement.Center else Arrangement.Top,
    ) {
        if (images.isEmpty()) {
            Button(
                onClick = { loadImages() }
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
                    ImageGrid(
                        images = images,
                        selectedImage = selectedImage,
                        setSelectedImage = { setSelectedImage(it) },
                        deleteSelectedImage = { deleteSelectedImage() }
                    )
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
                    selectedImage?.let {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(it.asFile())
                                .build(),
                            contentDescription = "Selected image: ${it.id}",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Inside,
                            alignment = Alignment.Center
                        )
                    }
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

    var images by remember { mutableStateOf<List<Image>>(emptyList()) }
    var selectedImage by remember { mutableStateOf<Image?>(null) }

    fun loadImages() {
        loadImagesFromDisk()?.let { newImages ->
            images = newImages.sortedBy { it.id } // TODO: add other sorting mechanisms
            images.firstOrNull()?.let {
                selectedImage = it
            }
        }
    }

    fun deleteSelectedImage() {
        selectedImage?.let {
            it.trash()
            val imgIdx = images.indexOf(it)
            images = images.filter { img -> img != it }
            selectedImage = if (images.isEmpty()) null
            else images[imgIdx.coerceAtMost(images.lastIndex)]
        }
    }

    fun setSelectedImage(image: Image?) {
        selectedImage = image
    }


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
                    onClick = ::loadImages,
                )
                Item(
                    text = "Deleted Selected Images",
                    shortcut =
                        if (SystemUtils.IS_OS_UNIX) KeyShortcut(key = Key.Backspace, meta = true)
                        else KeyShortcut(key = Key.Delete),
                    onClick = ::deleteSelectedImage,
                )
            }
        }

        MaterialTheme(
            colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
        ) {
            App(
                images = images,
                loadImages = ::loadImages,
                selectedImage = selectedImage,
                setSelectedImage = ::setSelectedImage,
                deleteSelectedImage = ::deleteSelectedImage,
            )
        }
    }
}
