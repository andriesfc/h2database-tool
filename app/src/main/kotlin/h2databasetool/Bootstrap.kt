package h2databasetool

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.MordantMarkdownHelpFormatter
import h2databasetool.BuildInfo.APP_DESCRIPTION
import h2databasetool.BuildInfo.APP_EXE
import h2databasetool.cmd.InitializeDatabase
import h2databasetool.cmd.ServeDatabases
import h2databasetool.cmd.ShutdownServer
import h2databasetool.cmd.ui.Style.boldEmphasis

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

    override val printHelpOnEmptyArgs: Boolean = true

    override fun help(context: Context): String = context.helpDoc()
}

private fun Context.helpDoc(): String {
    val exe = boldEmphasis(APP_EXE)
    val adminUser = "sa"
    val adminUserPassword = "s3cr3tadmin"
    val serverPassword = "mysecretadminpassword"
    return """
        $APP_DESCRIPTION
        
        **Usage Examples**
        
        1. Initialize a new database named `customersDb` with an admin login of `$adminUser` and password of `$adminUserPassword`.
           ```shell
           $exe  ${InitializeDatabase.COMMAND} customerDb --user $adminUser --password $adminUserPassword
           ```
        2. Serve up all available databases over TCP/IP on port 2029 with an admin password of `$serverPassword`.
           ```shell
           $exe  ${ServeDatabases.COMMAND} --password $serverPassword
           ```
        3. Shutdown database server running on port 2029 using the supplied server admin password.
           ```shell
           $exe ${ShutdownServer.COMMAND}  --password $serverPassword 
           ```
        """.trimIndent()

}
