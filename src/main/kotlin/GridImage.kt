import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import org.apache.commons.lang3.SystemUtils

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun GridImage(
    image: Image,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCtrlClick: () -> Unit,
    onShiftClick: () -> Unit,
) {
    val keyboardModifiers = LocalWindowInfo.current.keyboardModifiers
    val isCtrlPressed =
        if (SystemUtils.IS_OS_UNIX) keyboardModifiers.isMetaPressed
        else keyboardModifiers.isCtrlPressed
    val isShiftPressed = keyboardModifiers.isShiftPressed

    TooltipArea(
        tooltip = {
            Surface(elevation = 4.dp) {
                Column(modifier = Modifier.padding(8.dp)) {
                    image.filePaths().forEach { (format, path) ->
                        Text(
                            text = "${format.extension.uppercase()}: $path",
                            style = MaterialTheme.typography.overline,
                            color = format.color(MaterialTheme.colors.isLight),
                        )
                    }
                }
            }
        },
        delayMillis = 2000,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    if (isSelected) MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                    else MaterialTheme.colors.background
                )
                .clickable {
                    when {
                        isCtrlPressed && !isShiftPressed -> onCtrlClick()
                        isShiftPressed && !isCtrlPressed -> onShiftClick()
                        else -> onClick()
                    }
                }
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(image.asFile())
                        .build(),
                    // placeholder = placeholder, // TODO: add
                    // fallback = , // TODO: add
                    // error = , // TODO: add
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
                        Text(
                            text = it.extension,
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onPrimary,
                            modifier = Modifier
                                .background(
                                    color = it.color(MaterialTheme.colors.isLight),
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
                color = if (isSelected) MaterialTheme.colors.background
                else MaterialTheme.colors.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

