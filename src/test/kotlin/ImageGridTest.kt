import androidx.compose.material.MaterialTheme
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageGridTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setupComponent(appState: AppState) {
        rule.setContent {
            MaterialTheme {
                ImageGrid(appState)
            }
        }
    }

    @Test
    fun imagesDoNotTakeNativeFocus() {
        val images = List(4) { createImage(it) }
        val appState = AppState().apply { this.images = images }

        setupComponent(appState)

        rule.onNodeWithText("filename0").performClick()

        assertEquals(setOf(images[0]), appState.selectedImages)
        images.forEach { image ->
            rule.onNodeWithText(image.id).assertIsNotFocused()
        }
    }

    @Test
    fun arrowNavigationContinuesAfterDeletingFocusedImage() {
        val images = List(4) { createImage(it) }
        val appState = AppState().apply { this.images = images }

        setupComponent(appState)

        rule.onNodeWithText("filename1").performClick()
        appState.deleteSelectedImages()
        rule.waitForIdle()

        rule.onRoot().performKeyInput {
            pressKey(Key.DirectionRight)
        }

        assertEquals(setOf(images[3]), appState.selectedImages)
    }
}
