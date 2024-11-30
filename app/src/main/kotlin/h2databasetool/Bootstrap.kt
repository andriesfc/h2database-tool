package h2databasetool

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.MordantMarkdownHelpFormatter

class Bootstrap(
    commands: List<CliktCommand>,
) : NoOpCliktCommand(BuildInfo.APP_EXE) {

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
            readEnvvarBeforeValueSource = true
        }
        subcommands(commands)
    }

    override val printHelpOnEmptyArgs: Boolean = true


    override fun help(context: Context): String = BuildInfo.APP_DESCRIPTION

}