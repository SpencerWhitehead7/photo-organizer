import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

@Composable
fun PreviewImage(images: List<Image>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        when (images.size) {
            1 -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(images[0].asFile())
                        .build(),
                    contentDescription = "Selected image: ${images[0].id}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Inside,
                    alignment = Alignment.Center
                )
            }

            2 -> {
                if (images.any { it.isVertical(LocalPlatformContext.current) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        images.forEach { image ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(image.asFile())
                                    .build(),
                                contentDescription = "Selected image: ${image.id}",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f),
                                contentScale = ContentScale.Inside,
                                alignment = Alignment.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        images.forEach { image ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(image.asFile())
                                    .build(),
                                contentDescription = "Selected image: ${image.id}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentScale = ContentScale.Inside,
                                alignment = Alignment.Center
                            )
                        }
                    }
                }
            }

            else -> Text(
                text = "${images.size} Images Selected",
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}
