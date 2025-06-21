import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

@Composable
@Preview
fun GridImage(
    image: Image,
    isSelected: Boolean,
    onClick: () -> Unit,
    onTab: () -> Unit,
    onShiftTab: () -> Unit,
    onArrowKey: (Key) -> Unit,
) {
    val arrowKeys = setOf(Key.DirectionLeft, Key.DirectionRight, Key.DirectionUp, Key.DirectionDown)

    Column(
        modifier = Modifier
            .size(200.dp)
            .background(
                if (isSelected) MaterialTheme.colors.secondary.copy(alpha = 0.2f)
                else MaterialTheme.colors.surface.copy(alpha = 0f)
            )
            .clickable { onClick() }
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown) {
                    when (it.key) {
                        in arrowKeys -> {
                            onArrowKey(it.key)
                            true
                        }

                        Key.Tab -> {
                            (if (it.isShiftPressed) onShiftTab else onTab)()
                            true
                        }

                        else -> false
                    }
                }
                false
            }
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(image.asFile())
                    .build(),
//                placeholder = placeholder, // TODO: add
//                fallback = , // TODO: add
//                error = , // TODO: add
                contentDescription = "Image from: ${image.id}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )

            Row( // TODO: replace with FlowRow once that API is finalized (overflowing
                // labels isn't an urgent problem but it'd be nice know it'd work)
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                image.formats().forEach {
                    Text( // TODO: comp with different color per image format
                        text = it.extension,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colors.primary,
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(4.dp)
                    )
                }
            }
        }

        Text(
            text = image.id,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
