package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.TextAlign.RIGHT
import com.github.ajalt.mordant.table.grid
import h2databasetool.cmd.ui.Style.boldEmphasis
import h2databasetool.cmd.ui.Style.notice
import h2databasetool.cmd.ui.Style.softFocus
import h2databasetool.commons.add
import h2databasetool.commons.file
import h2databasetool.commons.render
import h2databasetool.commons.terminal.NL
import h2databasetool.env.Env
import org.h2.server.TcpServer
import org.h2.util.MathUtils.secureRandomBytes
import org.h2.util.StringUtils.convertBytesToHex
import java.io.File

class ServeDatabasesCommand : CliktCommand(NAME) {
    
    override fun help(context: Context): String = """
        Serves database from the base directory.
    """.trimIndent()

    private val trace by option("--trace", envvar = Env.H2TOOL_TRACE_CALLS.envVariable)
        .help("Trace client calls and dumps output to a dot-trace file.")
        .flag(default = Env.H2TOOL_TRACE_CALLS.default, defaultForHelp = Env.H2TOOL_TRACE_CALLS.default.toString())

    private val enableVirtualThreads by option(envvar = Env.H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS.envVariable)
        .help("Use virtual threads when client connects.")
        .flag(default = Env.H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS.default)

    private val baseDir by option("--data-dir", metavar = "directory", envvar = Env.H2TOOL_DATA_DIR.envVariable)
        .help("Location of database(s)")
        .default(Env.H2TOOL_DATA_DIR.default)

    private val allowOthers by option("--allow-others", envvar = Env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.envVariable)
        .help("Allow connections from other hosts to databases.")
        .flag(
            default = Env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.default,
            defaultForHelp = Env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.default.toString()
        )

    private val bindAddress by option(
        "--bind-address",
        metavar = "host address",
        envvar = Env.H2TOOL_SERVER_HOST.envVariable
    ).default(Env.H2TOOL_SERVER_HOST.default)
        .help("The network address the server should bind to.")

    private val managementPassword by option(
        "--management-password",
        metavar = "server management password",
        envvar = Env.H2TOOL_SERVER_PASSWORD.envVariable
    ).help("Protect the exposed port on the lan with a password (if not set a random password will be generated).")

    private val port by option(
        "--bind-port",
        metavar = Env.H2TOOL_SERVER_PORT.envVariable,
        envvar = Env.H2TOOL_SERVER_PORT.envVariable
    ).convert { it.toUShort() }
        .default(Env.H2TOOL_SERVER_PORT.default)
        .help("Network port on which to serve the database.")

    private val autoCreateDbIfNotExists by option(
        "--permit-remote-db-creation",
        envvar = Env.H2TOOL_SERVER_PERMIT_CREATE_DB.envVariable
    ).flag(
        default = Env.H2TOOL_SERVER_PERMIT_CREATE_DB.default,
        defaultForHelp = Env.H2TOOL_SERVER_PERMIT_CREATE_DB.default.toString()
    ).help("Allow clients connecting to create their own databases.")

    private lateinit var _serverManagementPassword: String
    private lateinit var _baseDir: File
    private var managementPasswordGenerated = false

    private fun prepareServerArgs() =
        mutableListOf<Any>().apply {
            _serverManagementPassword =
                managementPassword ?: generateManagementPassword().also {
                    managementPasswordGenerated = true
                }
            _baseDir = baseDir.file(canonical = true, absolute = true)
            if (trace) add("-trace")
            if (enableVirtualThreads) add("-tcpVirtualThreads", true)
            if (autoCreateDbIfNotExists) add("-ifNotExists", true) else add("-ifExists", true)
            if (allowOthers) add("-tcpAllowOthers")
            add("-tcpPassword", _serverManagementPassword)
            add("-tcpPort", port)
            add("-baseDir", _baseDir)
        }.map(Any::toString).toTypedArray()

    override fun run() {
        Env.H2TOOL_SERVER_HOST(bindAddress)
        val server = TcpServer()
        server.init(*prepareServerArgs())
        server.start()
        showDetails(server)
        server.listen()
    }

    private fun showDetails(server: TcpServer) = render(terminal) {

        val details = buildList<Pair<String, String>> {
            add(
                "Server url:" to server.url,
                "Server base directory:" to _baseDir.path,
                "Server allowing clients to create database:" to if (autoCreateDbIfNotExists) "yes" else "no",
                "Server bind address:" to bindAddress,
                "Server accepts network requests:" to if (allowOthers) "yes" else "no",
                "Server using virtual threads:" to if (enableVirtualThreads) "yes" else "no",
            )
            if (managementPasswordGenerated) add(
                "Server shutdown password (generated):" to _serverManagementPassword,
            )
            sortBy { (label, _) -> label.lowercase() }
        }

        grid {
            row {
                cell(boldEmphasis("Sever started up.${NL}Please note the following:")) {
                    columnSpan = 2
                    align = TextAlign.CENTER
                }
                padding {
                    bottom = 1
                    top = 1
                }
            }
            details.forEach { (label, value) ->
                row {
                    cell(softFocus(label)) { align = RIGHT }
                    cell(notice(value)) { align = LEFT }
                }
            }
        }
    }

    private fun generateManagementPassword() =
        Env.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE.value()
            .let(::secureRandomBytes)
            .let(::convertBytesToHex)

    companion object {
        const val NAME = "serveDb"
    }
}

