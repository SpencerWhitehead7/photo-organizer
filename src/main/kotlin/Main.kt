import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.io.File

@Composable
@Preview
fun App() {
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

    var imagePaths by remember { mutableStateOf<List<String>>(emptyList()) }

    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    fun setImagePaths(newImagePaths: List<String>) {
        imagePaths = newImagePaths
        selectedImagePath = newImagePaths.firstOrNull()
    }

    var previewHeight by remember { mutableStateOf(400f) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = null,
                    indication = null
                ) {
                    selectedImagePath = null
                }

        ) {
            SelectImages(onSelectImages = ::setImagePaths)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight.dp),
                contentAlignment = Alignment.Center
            ) {
                selectedImagePath?.let { path ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(File(path))
                            .build(),
                        contentDescription = "Selected image: $path",
                        modifier = Modifier.fillMaxHeight(),
                        contentScale = ContentScale.Inside,
                        alignment = Alignment.Center
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            previewHeight = (previewHeight + dragAmount.y * 0.5f).coerceIn(100f, 800f)
                        }
                    }
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val gridState = rememberLazyGridState()

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(
                        items = imagePaths,
                        key = { it }
                    ) { path ->
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(
                                    if (path == selectedImagePath)
                                        MaterialTheme.colors.secondary.copy(alpha = 0.2f)
                                    else
                                        MaterialTheme.colors.surface.copy(alpha = 0f)
                                )
                                .clickable { selectedImagePath = path }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(File(path))
                                    .build(),
//                              placeholder = placeholder, // TODO: add
//                              fallback = , // TODO: add
//                              error = , // TODO: add
                                contentDescription = "Image from: $path",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.Center
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
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
