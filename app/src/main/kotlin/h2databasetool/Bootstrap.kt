package h2databasetool

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.MordantMarkdownHelpFormatter
import com.github.ajalt.clikt.parameters.options.option
import h2databasetool.BuildInfo.APP_EXE
import h2databasetool.cmd.option.quiet

class Bootstrap(
    commands: List<CliktCommand>,
) : NoOpCliktCommand(APP_EXE) {

    init {
        installMordant(force = true)
        configureContext {
            helpFormatter = { context ->
                MordantMarkdownHelpFormatter(
                    context,
                    showDefaultValues = true,
                    showRequiredTag = true,
                    requiredOptionMarker = "*"
                )
            }
        }
        subcommands(commands)
    }

    val quiet by option().quiet()

    override val printHelpOnEmptyArgs: Boolean = true

    override fun help(context: Context): String = BuildInfo.APP_DESCRIPTION
}
