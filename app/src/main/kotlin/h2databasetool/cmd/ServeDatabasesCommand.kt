package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.TextAlign.RIGHT
import com.github.ajalt.mordant.table.grid
import h2databasetool.cmd.ui.Styles.boldEmphasis
import h2databasetool.cmd.ui.Styles.notice
import h2databasetool.cmd.ui.Styles.softFocus
import h2databasetool.cmd.ui.renderOn
import h2databasetool.env.env
import h2databasetool.commons.NL
import h2databasetool.commons.add
import h2databasetool.commons.file
import h2databasetool.commons.resourceOfClassWithExt
import org.h2.server.TcpServer
import org.h2.util.MathUtils.secureRandomBytes
import org.h2.util.StringUtils.convertBytesToHex
import java.io.File

class ServeDatabasesCommand : CliktCommand("servedb") {

    private val helpDoc = resourceOfClassWithExt<ServeDatabasesCommand>("help.md")

    override fun help(context: Context): String = helpDoc.readText()

    private val trace by option("--trace", envvar = env.H2TOOL_TRACE_CALLS.variable)
        .help("Trace client calls and dumps output to a dot-trace file.")
        .flag(default = env.H2TOOL_TRACE_CALLS.default, defaultForHelp = env.H2TOOL_TRACE_CALLS.default.toString())

    private val enableVirtualThreads by option(envvar = env.H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS.variable)
        .help("Use virtual threads when client connects.")
        .flag(default = env.H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS.default)

    private val baseDir by option("--data-dir", metavar = "H2_DATA_DIRECTORY", envvar = env.H2TOOL_DATA_DIR.variable)
        .help("Location of database(s)")
        .default(env.H2TOOL_DATA_DIR.default)

    private val allowOthers by option("--allow-others", envvar = env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.variable)
        .help("Allow connections from other hosts to databases.")
        .flag(
            default = env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.default,
            defaultForHelp = env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.default.toString()
        )

    private val managementPassword by option(
        "--management-password",
        metavar = "TCP management password",
        envvar = env.H2TOOL_SERVER_PASSWORD.variable
    ).help("Protect the exposed port on the lan with a password (if not set a random password will be generated).")

    private val port by option(
        "--port",
        metavar = env.H2TOOL_SERVER_PORT.variable,
        envvar = env.H2TOOL_SERVER_PORT.variable
    ).convert { it.toUShort() }
        .default(env.H2TOOL_SERVER_PORT.default)
        .help("Network port on which to serve the database.")

    private val autoCreateDbIfNotExists by option(
        "--permit-remote-db-creation",
        envvar = env.H2TOOL_SERVER_PERMIT_CREATE_DB.variable
    ).flag(
        default = env.H2TOOL_SERVER_PERMIT_CREATE_DB.default,
        defaultForHelp = env.H2TOOL_SERVER_PERMIT_CREATE_DB.default.toString()
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
        val server = TcpServer()
        server.init(*prepareServerArgs())
        server.start()
        showDetails(server)
        server.listen()
    }

    private fun showDetails(server: TcpServer) = renderOn(terminal) {
        grid {
            fun detail(name: String, value: String, visible: Boolean = true) {
                if (visible) row {
                    cell(softFocus(name)) { align = RIGHT }
                    cell(notice(value)) { align = LEFT }
                }
            }
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
            detail("Server url: ", server.url)
            detail("Server base directory: ", _baseDir.path)
            detail("Server allowing clients to create databases: ", if (autoCreateDbIfNotExists) "yes" else "no")
            detail("Server accepts network request: ", if (allowOthers) "yes" else "no")
            detail("Server using virtual threads: ", if (enableVirtualThreads) "yes" else "no")
            detail("Server shutdown password (generated): ", _serverManagementPassword, managementPasswordGenerated)
        }
    }

    private fun generateManagementPassword() =
        env.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE()
            .let(::secureRandomBytes)
            .let(::convertBytesToHex)
}

