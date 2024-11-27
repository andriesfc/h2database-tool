package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.TextAlign.RIGHT
import com.github.ajalt.mordant.table.grid
import h2databasetool.cmd.ui.Styles.boldEmphasis
import h2databasetool.cmd.ui.Styles.notice
import h2databasetool.cmd.ui.Styles.softFocus
import h2databasetool.cmd.ui.render
import h2databasetool.env.EnvDefault
import h2databasetool.env.EnvVar
import h2databasetool.env.EnvVar.H2TOOL_BASE_DIR
import h2databasetool.env.EnvVar.H2TOOL_SERVER_PORT
import h2databasetool.env.EnvVar.H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS
import h2databasetool.utils.add
import h2databasetool.utils.resourceOfClassWithExt
import h2databasetool.utils.file
import org.h2.server.TcpServer
import org.h2.util.MathUtils.secureRandomBytes
import org.h2.util.StringUtils.convertBytesToHex
import java.io.File

class ServeDatabasesCommand : CliktCommand("servedb") {

    private val helpDoc = resourceOfClassWithExt<ServeDatabasesCommand>("help.md")

    override fun help(context: Context): String = helpDoc.readText()

    private val trace by option("--trace", envvar = EnvVar.H2TOOL_TRACE_CALLS)
        .help("Trace client calls and dumps output to a dot-trace file.")
        .flag(default = EnvDefault.H2TOOL_TRACE_CALLS, defaultForHelp = EnvDefault.H2TOOL_TRACE_CALLS.toString())

    private val enableVirtualThreads by option(envvar = H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS)
        .help("Use virtual threads when client connects.")
        .flag(default = EnvDefault.H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS)

    private val baseDir by option("--data-dir", metavar = "H2_DATA_DIRECTORY", envvar = H2TOOL_BASE_DIR)
        .help("Location of database(s)")
        .default(EnvDefault.H2TOOL_BASE_DIR)

    private val allowOthers by option("--allow-others", envvar = EnvVar.H2TOOL_TCP_ALLOW_REMOTE_CONNECTIONS)
        .help("Allow connections from other hosts to databases.")
        .flag(
            default = EnvDefault.H2TOOL_TCP_ALLOW_REMOTE_CONNECTIONS,
            defaultForHelp = EnvDefault.H2TOOL_TCP_ALLOW_REMOTE_CONNECTIONS.toString()
        )

    private val managementPassword by option(
        "--management-password",
        metavar = "TCP management password",
        envvar = EnvVar.H2TOOL_SERVER_PASSWORD
    ).help("Protect the exposed port on the lan with a password (if not set a random password will be generated).")

    private val port by option("--port", metavar = "H2_DATABASE_PORT", envvar = H2TOOL_SERVER_PORT)
        .help("Network port on which to serve the database.")
        .convert { it.toUShort() }
        .default(EnvDefault.H2TOOL_SERVER_PORT)

    private val autoCreateDbIfNotExists by option(
        "--auto-create-database",
        envvar = EnvVar.H2TOOL_PERMIT_DB_CREATION
    ).flag(
        default = EnvDefault.H2TOOL_PERMIT_DB_CREATION,
        defaultForHelp = EnvVar.H2TOOL_SERVER_PASSWORD
    ).help("Allow clients connecting to create their own databases.")

    private lateinit var _serverManagementPassword: String
    private lateinit var _baseDir: File
    private var managementPasswordGenerated = false

    private fun serverArgs() =
        mutableListOf<Any>().apply {
            _serverManagementPassword = managementPassword ?: generateManagementPassword()
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
        server.init(*serverArgs())
        server.start()
        echoStatus(server)
        server.listen()
    }

    private fun echoStatus(server: TcpServer) = terminal.render {
        grid {
            fun detailRow(name: String, value: String, valid: Boolean = true) {
                if (valid) row {
                    cell(softFocus(name)) { align = RIGHT }
                    cell(notice(value)) { align = LEFT }
                }
            }
            row { cell(boldEmphasis("Sever started up. Please note the following")) { columnSpan = 2 } }
            detailRow("TCP Server: ", server.url)
            detailRow("Base Directory: ", _baseDir.path)
            detailRow("TCP Server Password: ", _serverManagementPassword, managementPasswordGenerated)
        }
    }


    private fun generateManagementPassword(): String {
        return EnvVar.get(EnvVar.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE, EnvDefault.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE)
            .let(::secureRandomBytes)
            .let(::convertBytesToHex)
            .also { managementPasswordGenerated = true }
    }

}
