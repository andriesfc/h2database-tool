package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*
import h2databasetool.cmd.ui.Style.softYellowFocus
import h2databasetool.commons.terminal.FORCE_LINE_BREAK
import h2databasetool.env.Env
import org.h2.tools.Server.shutdownTcpServer

/**
 * Shutdown an H2 server running on a TCP/IP port for a given server.
 * > **NOTE**: The application must have network access to host running the
 * > server, as well as the password for that server.
 */
class ShutdownServer : CliktCommand(COMMAND) {

    override fun help(context: Context): String {
        return """
            Shutdowns a server running on specific sever host and port.
            $FORCE_LINE_BREAK
            > ${softYellowFocus("NOTE:")} The client must have network access to the host,
            > (or run on the same host), as well
            > as the server password used/generated at startup.
            """.trimIndent()
    }

    private val forceShutdown by option("--force")
        .help("Forcefully shutdown server.")
        .flag(
            default = Env.H2ToolServerForceShutdown.default,
            defaultForHelp = Env.H2ToolServerForceShutdown.default.toString()
        )

    private val adminPassword by option(
        "--password",
        metavar = "password",
        envvar = Env.H2ToolServerPassword.envVariable
    ).help("Shuts down a running server on a host with a given port.")
        .default(Env.H2ToolServerPassword.default)

    private val host by option(
        "--host",
        metavar = "host",
        envvar = Env.H2ToolServerHost.envVariable
    ).default(Env.H2ToolServerHost.default)
        .help("Host server address to bind to.")

    private val port by option(
        "--port", "-p", metavar = "port number",
        envvar = Env.H2ToolServerPort.envVariable
    ).convert { it.toUShort() }
        .default(Env.H2ToolServerPort.default)
        .help("The port the server is running on.")

    private fun uri() = "tcp://$host:$port"

    override fun run() =
        shutdownTcpServer(
            /* url = */ uri(),
            /* password = */ adminPassword,
            /* force = */ forceShutdown,
            /* all = */ false
        )

    companion object {
        const val COMMAND = "shutdown"
    }
}
