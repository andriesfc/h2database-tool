package h2databasetool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.installMordant
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.MordantMarkdownHelpFormatter

internal fun bootstrap(vararg commands: CliktCommand, buildHelpDoc: ((Context) -> String)? = null) =
    object : NoOpCliktCommand(BuildInfo.APP_NAME) {

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

        override val printHelpOnEmptyArgs: Boolean = true

        override fun help(context: Context): String =
            buildHelpDoc?.invoke(context) ?: super.help(context)
    }
