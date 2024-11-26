package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*
import h2databasetool.env.EnvDefault
import h2databasetool.env.EnvVar
import org.h2.server.TcpServer.shutdown

class ShutdownTcpServer : CliktCommand("shutdownDb") {

    override fun help(context: Context): String = """
            |Shuts down a H2 database server running on TCP port and/or host.
            """.trimMargin()

    private val port by option("--port", envvar = EnvVar.H2TOOL_SERVER_PORT, metavar = "PORT-NUMBER")
        .help("The TCP port the server runs on.")
        .convert { it.toUShort() }.default(EnvDefault.H2TOOL_SERVER_PORT)

    private val host by option("--host", envvar = EnvVar.H2TOOL_DATABASE_HOST, metavar = "HOST-NAME")
        .help("Host on which the database run.")
        .default(EnvDefault.H2TOOL_DATABASE_HOST)

    private val forced by option("--force", envvar = EnvVar.H2TOOL_FORCE_SHUTDOWN)
        .help("Forces the shutdown.")
        .flag(default = false, defaultForHelp = "${EnvDefault.H2TOOL_FORCE_SHUTDOWN}")

    private val managementDbPassword by option("--password", "-p", envvar = EnvVar.H2TOOL_SERVER_PASSWORD)
        .help("Management password supplied (or generated) at server startup.")

    override fun run() {
        val serverUrl = "tcp://$host:$port"
        val serverManagementPassword = managementDbPassword ?: ""
        shutdown(serverUrl, serverManagementPassword, forced, false)
    }
}