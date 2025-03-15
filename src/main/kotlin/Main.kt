import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
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
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

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

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Button(onClick = {
                val fileChooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
                    isMultiSelectionEnabled = true
                    fileFilter = FileNameExtensionFilter(
                        "Images", *SUPPORTED_IMAGES.toTypedArray()
                    )
                }

                val result = fileChooser.showOpenDialog(ComposeWindow())
                if (result == JFileChooser.APPROVE_OPTION) {
                    imagePaths = fileChooser.selectedFiles.flatMap {
                        collectImages(File(it.absolutePath))
                    }
                }
            }) {
                Text("Select Files")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = imagePaths,
                    key = { it }
                ) { path ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(File(path))
                            .build(),
//                        placeholder = placeholder, // TODO: add
//                        fallback = , // TODO: add
//                        error = , // TODO: add
                        contentDescription = "Image from: $path",
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                }
            }
        }
    }
}

val SUPPORTED_IMAGES = setOf("jpg", "jpeg", "png", "dng") // TODO: it's probably missing some

fun collectImages(file: File): List<String> {
    return when {
        file.isFile -> {
            if (file.extension.lowercase() in SUPPORTED_IMAGES) listOf(file.absolutePath)
            else emptyList()
        }

        file.isDirectory -> {
            file.walk()
                .filter { it.isFile && it.extension.lowercase() in SUPPORTED_IMAGES }
                .map { it.absolutePath }
                .toList()
        }

        else -> emptyList()
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
