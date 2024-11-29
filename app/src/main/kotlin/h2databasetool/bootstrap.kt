package h2databasetool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.installMordant
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.MordantMarkdownHelpFormatter
import h2databasetool.commons.resourceOf

internal fun bootstrap(vararg commands: CliktCommand) = object : NoOpCliktCommand(BuildInfo.APP_NAME) {

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
        subcommands(*commands)
    }

    private val toolDoc = resourceOf("/h2databasetool/app.help.md")

    override fun help(context: Context): String = toolDoc.readText()

    override val printHelpOnEmptyArgs: Boolean = true

}
