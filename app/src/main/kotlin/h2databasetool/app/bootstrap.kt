package h2databasetool.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.output.MordantHelpFormatter

internal fun bootstrap() = object : CliktCommand("h2") {

    init {
        configureContext {
            helpFormatter = { context ->
                MordantHelpFormatter(
                    context,
                    showDefaultValues = true,
                    showRequiredTag = true,
                    requiredOptionMarker = "*"
                )
            }
            readEnvvarBeforeValueSource = true
        }
    }

    override fun help(context: Context): String = """
        H2 database tool
        
        A collection of useful scripts to operate H2 databases. 
        """.trimIndent()

    override val printHelpOnEmptyArgs: Boolean = true

    override fun run() = Unit
}