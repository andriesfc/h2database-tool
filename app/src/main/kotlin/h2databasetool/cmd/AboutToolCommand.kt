package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.table.grid
import com.github.ajalt.mordant.table.verticalLayout
import h2databasetool.BuildInfo
import h2databasetool.cmd.ui.Style.boldEmphasis
import h2databasetool.cmd.ui.Style.notice
import h2databasetool.cmd.ui.Style.softFocus
import h2databasetool.cmd.ui.Style.softYellowFocus
import h2databasetool.cmd.ui.render
import h2databasetool.env.Env
import java.time.LocalDate

class AboutToolCommand : CliktCommand(NAME) {

    companion object {
        const val NAME = "about"
    }

    override fun help(context: Context): String {
        return "Displays interesting information about this H2 tool"
    }

    override fun run() {
        val heading = boldEmphasis(BuildInfo.APP_DESCRIPTION.uppercase())
        val detail = listOf(
            BuildInfo.VERSION to "Build Version:",
            BuildInfo.VERSION_NAME to "Build Name:",
            BuildInfo.BUILD_DATE to "Build Date:",
            BuildInfo.BUILD_OS to "Build OS:",
            LocalDate.now().toString() to "Current Date:",
        ).sortedBy { (_, label) -> label.lowercase() }
        render(terminal) {
            verticalLayout {
                cell(grid {
                    row {
                        cell(boldEmphasis(heading)) {
                            columnSpan = 2
                            align = TextAlign.CENTER

                        }
                        padding { top = 1 }
                    }
                    row {
                        cell(softYellowFocus("Some interesting details")) {
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
                    row {
                        cell(softYellowFocus("System Environment Settings")) {
                            columnSpan = 2
                            align = TextAlign.CENTER
                        }
                        padding {
                            top = 1
                            bottom = 1
                        }
                    }
                    Env.entries().filter { env -> env.isActive }.forEach { env ->
                        row {
                            cell(softFocus(env.variable)) { align = TextAlign.RIGHT }
                            cell(notice(env.get())) { align = TextAlign.LEFT }
                        }
                    }
                })

            }


        }
    }
}