package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.table.grid
import com.github.ajalt.mordant.widgets.Text
import h2databasetool.BuildInfo
import h2databasetool.cmd.option.dataDir
import h2databasetool.cmd.ui.Style.h1
import h2databasetool.env.Env

class AboutTool : CliktCommand(COMMAND) {

    companion object {
        const val COMMAND = "env"
    }

    private val dataDir by option().dataDir()

    override fun help(context: Context): String {
        return "Displays interesting configurations."
    }

    override fun run() {
        echo(grid {
            row { cell(h1("tool environment")) { columnSpan = 2; align = TextAlign.CENTER } }
            Env.entries().filter { env -> env.isSet }.forEach { env ->
                row {
                    cell("${env.envVariable}:") { align = TextAlign.RIGHT }
                    cell(env.get())
                }
            }
            row { }
            row { cell(h1("databases")) { columnSpan = 2; align = TextAlign.CENTER } }
            dataDir.dbNames().forEach { database ->
                row {
                    cell(Text(text = "- $database")) { align = TextAlign.LEFT; columnSpan = 2 }
                }
            }
            row { }
            row { cell(h1("build information")) { columnSpan = 2; align = TextAlign.CENTER } }
            listOf(
                BuildInfo.BUILD_OS to "Build Info:",
                BuildInfo.BUILD_DATE to "Build Date:",
                BuildInfo.VERSION_NAME to "Version Name:",
                BuildInfo.H2_LIB_VERSION to "H2 Library Version:",
                BuildInfo.VERSION to "Build Version:"
            ).sortedBy { (_, value) -> value.lowercase() }.forEach { (value, label) ->
                row {
                    cell(label) { align = TextAlign.RIGHT }
                    cell(value)
                }
            }
        })
    }

}

