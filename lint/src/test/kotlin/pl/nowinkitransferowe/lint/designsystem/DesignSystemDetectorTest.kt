package pl.nowinkitransferowe.lint.designsystem

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import pl.nowinkitransferowe.lint.designsystem.DesignSystemDetector.Companion.ISSUE
import pl.nowinkitransferowe.lint.designsystem.DesignSystemDetector.Companion.METHOD_NAMES
import pl.nowinkitransferowe.lint.designsystem.DesignSystemDetector.Companion.RECEIVER_NAMES
import org.junit.Test

class DesignSystemDetectorTest {

    @Test
    fun `detect replacements of Composable`() {
        lint()
            .issues(ISSUE)
            .allowMissingSdk()
            .files(
                COMPOSABLE_STUB,
                STUBS,
                @Suppress("LintImplTrimIndent")
                kotlin(
                    """
                    |import androidx.compose.runtime.Composable
                    |
                    |@Composable
                    |fun App() {
                    ${METHOD_NAMES.keys.joinToString("\n") { "|    $it()" }}
                    |}
                    """.trimMargin(),
                ).indented(),
            )
            .run()
            .expect(
                """
src/test.kt:5: Error: Using MaterialTheme instead of NtTheme [DesignSystem]
    MaterialTheme()
    ~~~~~~~~~~~~~~~
src/test.kt:6: Error: Using Button instead of NtButton [DesignSystem]
    Button()
    ~~~~~~~~
src/test.kt:7: Error: Using OutlinedButton instead of NtOutlinedButton [DesignSystem]
    OutlinedButton()
    ~~~~~~~~~~~~~~~~
src/test.kt:8: Error: Using TextButton instead of NtTextButton [DesignSystem]
    TextButton()
    ~~~~~~~~~~~~
src/test.kt:9: Error: Using NavigationBar instead of NtNavigationBar [DesignSystem]
    NavigationBar()
    ~~~~~~~~~~~~~~~
src/test.kt:10: Error: Using NavigationBarItem instead of NtNavigationBarItem [DesignSystem]
    NavigationBarItem()
    ~~~~~~~~~~~~~~~~~~~
src/test.kt:11: Error: Using NavigationRail instead of NtNavigationRail [DesignSystem]
    NavigationRail()
    ~~~~~~~~~~~~~~~~
src/test.kt:12: Error: Using NavigationRailItem instead of NtNavigationRailItem [DesignSystem]
    NavigationRailItem()
    ~~~~~~~~~~~~~~~~~~~~
src/test.kt:13: Error: Using IconToggleButton instead of NtIconToggleButton [DesignSystem]
    IconToggleButton()
    ~~~~~~~~~~~~~~~~~~
src/test.kt:14: Error: Using FilledIconToggleButton instead of NtIconToggleButton [DesignSystem]
    FilledIconToggleButton()
    ~~~~~~~~~~~~~~~~~~~~~~~~
src/test.kt:15: Error: Using FilledTonalIconToggleButton instead of NtIconToggleButton [DesignSystem]
    FilledTonalIconToggleButton()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
src/test.kt:16: Error: Using OutlinedIconToggleButton instead of NtIconToggleButton [DesignSystem]
    OutlinedIconToggleButton()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~
src/test.kt:17: Error: Using CenterAlignedTopAppBar instead of NtTopAppBar [DesignSystem]
    CenterAlignedTopAppBar()
    ~~~~~~~~~~~~~~~~~~~~~~~~
src/test.kt:18: Error: Using SmallTopAppBar instead of NtTopAppBar [DesignSystem]
    SmallTopAppBar()
    ~~~~~~~~~~~~~~~~
src/test.kt:19: Error: Using MediumTopAppBar instead of NtTopAppBar [DesignSystem]
    MediumTopAppBar()
    ~~~~~~~~~~~~~~~~~
src/test.kt:20: Error: Using LargeTopAppBar instead of NtTopAppBar [DesignSystem]
    LargeTopAppBar()
    ~~~~~~~~~~~~~~~~
16 errors, 0 warnings
                """.trimIndent(),
            )
    }

    @Test
    fun `detect replacements of Receiver`() {
        lint()
            .issues(ISSUE)
            .allowMissingSdk()
            .files(
                COMPOSABLE_STUB,
                STUBS,
                @Suppress("LintImplTrimIndent")
                kotlin(
                    """
                    |fun main() {
                    ${RECEIVER_NAMES.keys.joinToString("\n") { "|    $it.toString()" }}
                    |}
                    """.trimMargin(),
                ).indented(),
            )
            .run()
            .expect(
                """
                src/test.kt:2: Error: Using Icons instead of NtIcons [DesignSystem]
                    Icons.toString()
                    ~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent(),
            )
    }

    private companion object {

        private val COMPOSABLE_STUB: TestFile = kotlin(
            """
            package androidx.compose.runtime
            annotation class Composable
            """.trimIndent(),
        ).indented()

        private val STUBS: TestFile = kotlin(
            """
            |import androidx.compose.runtime.Composable
            |
            ${METHOD_NAMES.keys.joinToString("\n") { "|@Composable fun $it() = {}" }}
            ${RECEIVER_NAMES.keys.joinToString("\n") { "|object $it" }}
            |
            """.trimMargin(),
        ).indented()
    }
}
