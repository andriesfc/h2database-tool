package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.grid
import h2databasetool.BuildInfo
import h2databasetool.cmd.ui.Styles
import h2databasetool.cmd.ui.renderOn
import h2databasetool.cmd.ui.Styles.boldEmphasis
import h2databasetool.cmd.ui.Styles.notice
import h2databasetool.cmd.ui.Styles.softFocus
import java.time.LocalDate

class AboutToolCommand : CliktCommand("about") {

    override fun help(context: Context): String {
        return "Displays application version information"
    }

    override fun run() {
        val heading = Styles.boldEmphasis(BuildInfo.APP_DESCRIPTION.uppercase())
        val detail = listOf(
            BuildInfo.VERSION to "Build Version:",
            BuildInfo.VERSION_NAME to "Build Name:",
            BuildInfo.BUILD_DATE to "Build Date:",
            BuildInfo.BUILD_OS to "Build OS:",
            LocalDate.now().toString() to "Current Date:",
        ).sortedBy { (_, label) -> label.lowercase() }
        renderOn(terminal) {
            grid {
                row {
                    cell(boldEmphasis(heading)) {
                        columnSpan = 2
                        align = TextAlign.CENTER

                    }
                    padding { top = 1 }
                }
                row {
                    cell((softFocus + TextColors.brightYellow)("Some interesting details")) {
                        columnSpan = 2
                        align = TextAlign.CENTER
                        padding { bottom = 1 }
                    }
                }
                detail.forEach { (value, label) ->
                    row {
                        cell(softFocus(label)) { align = TextAlign.RIGHT }
                        cell(notice(value)) { align = TextAlign.LEFT }
                    }
                }
            }
        }
    }
}