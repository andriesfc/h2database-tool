package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.TextAlign.RIGHT
import com.github.ajalt.mordant.table.grid
import h2databasetool.cmd.ui.Styles.boldEmphasis
import h2databasetool.cmd.ui.Styles.notice
import h2databasetool.cmd.ui.Styles.softFocus
import h2databasetool.cmd.ui.renderOn
import h2databasetool.env.env
import h2databasetool.utils.add
import h2databasetool.utils.file
import h2databasetool.utils.resourceOfClassWithExt
import org.h2.server.TcpServer
import org.h2.util.MathUtils.secureRandomBytes
import org.h2.util.StringUtils.convertBytesToHex
import java.io.File

class ServeDatabasesCommand : CliktCommand("servedb") {

    private val helpDoc = resourceOfClassWithExt<ServeDatabasesCommand>("help.md")

    override fun help(context: Context): String = helpDoc.readText()

    private val trace by option("--trace", envvar = env.H2TOOL_TRACE_CALLS.envvar)
        .help("Trace client calls and dumps output to a dot-trace file.")
        .flag(default = env.H2TOOL_TRACE_CALLS.default, defaultForHelp = env.H2TOOL_TRACE_CALLS.default.toString())

    private val enableVirtualThreads by option(envvar = env.H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS.envvar)
        .help("Use virtual threads when client connects.")
        .flag(default = env.H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS.default)

    private val baseDir by option("--data-dir", metavar = "H2_DATA_DIRECTORY", envvar = env.H2TOOL_DATA_DIR.envvar)
        .help("Location of database(s)")
        .default(env.H2TOOL_DATA_DIR.default)

    private val allowOthers by option("--allow-others", envvar = env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.envvar)
        .help("Allow connections from other hosts to databases.")
        .flag(
            default = env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.default,
            defaultForHelp = env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.default.toString()
        )

    private val managementPassword by option(
        "--management-password",
        metavar = "TCP management password",
        envvar = env.H2TOOL_SERVER_PASSWORD.envvar
    ).help("Protect the exposed port on the lan with a password (if not set a random password will be generated).")

    private val port by option("--port", metavar = "H2_DATABASE_PORT", envvar = env.H2TOOL_SERVER_PORT.envvar)
        .help("Network port on which to serve the database.")
        .convert { it.toUShort() }
        .default(env.H2TOOL_SERVER_PORT.default)

    private val autoCreateDbIfNotExists by option(
        "--permit-remote-db-creation",
        envvar = env.H2TOOL_SERVER_PERMIT_CREATE_DB.envvar
    ).flag(
        default = env.H2TOOL_SERVER_PERMIT_CREATE_DB.default,
        defaultForHelp = env.H2TOOL_SERVER_PERMIT_CREATE_DB.default.toString()
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

    private fun echoStatus(server: TcpServer) = renderOn(terminal) {
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


    private fun generateManagementPassword() = env.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE.run {
        (System.getenv(envvar)?.toUShortOrNull()?.also { fromEnv: UShort ->
            if (fromEnv !in permittedSizes) throw CliktError(
                "Invalid value for $envvar environment " +
                        "variable: $fromEnv. Only the following is permitted: ${permittedSizes.joinToString()}"
            )
        } ?: default).toInt()
            .let(::secureRandomBytes)
            .let(::convertBytesToHex)
    }

}
