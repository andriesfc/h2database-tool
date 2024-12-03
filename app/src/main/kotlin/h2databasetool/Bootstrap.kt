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

    override fun help(context: Context): String = """
        ${BuildInfo.APP_DESCRIPTION}
        
        **Usage Examples**
        
        1. Initialize a new database named `customersDb` with an admin login of `ca` and password of `s3cr3t`.
           ```shell
           ./${BuildInfo.APP_EXE} initDb customerDb --user sa --password s3cr3t
           ```
        2. Serve up all available databases over TCP/IP on port 2029 with an admin password of `mysecretadminpassword`.
           ```shell
           ./${BuildInfo.APP_EXE} serveDb --management-password mysecretadminpassword
           ```
        3. Shutdown database server running on port 2029 using the supplied server admin password.
           ```shell
           ./${BuildInfo.APP_EXE} shutdown --pass mysecretadminpassword 
           ```
        """.trimIndent()

}
