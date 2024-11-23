package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*
import h2databasetool.utils.add
import h2databasetool.utils.echoMarkdown
import h2databasetool.utils.file
import org.h2.server.TcpServer

class ServeDatabase : CliktCommand("serveDb") {

    override fun help(context: Context): String {
        return "Serves databases via a TCP connection."
    }

    private val trace by option("--trace")
        .help("Trace calls and dumps output to dot-trace file.")
        .flag()

    private val enableVirtualThreads by option()
        .help("Use virtual threads when client connects.")
        .flag()

    private val dryRun by option()
        .help("Only prints out messages, but does not expose amy database on a port.")
        .flag()

    private val baseDir by option("--data-dir", metavar = "H2_DATA_DIRECTORY", envvar = H2_DATA_DIR)
        .help("Location of database(s)")
        .default("~/.h2/data")

    private val allowOthers by option("--allow-others")
        .help("Allow connections from other hosts to databases.")
        .flag()

    private val managementPassword by option("--management-password", metavar = "TCP management password")
        .help("Protect the exposed port on the lan with a password.")

    private val port by option("--port", metavar = "H2_DATABASE_PORT", envvar = H2_DATABSE_NETWORK_PORT)
        .help("Network port on which to serve the database.")
        .convert { it.toUShort() }
        .default(3569u)

    private val autoCreateDbIfNotExists by option("--auto-create-database").flag(
        default = false,
        defaultForHelp = "false"
    )
        .help("Allow clients connecting to create their own databases.")

    private inline fun doOperation(announcement: String, operation: () -> Unit) {
        echoMarkdown(announcement)
        if (!dryRun) operation()
    }

    private fun serverArgs() =
        mutableListOf<Any>().apply {
            if (trace) add("-trace")
            if (enableVirtualThreads) add("-tcpVirtualThreads", true)
            if (autoCreateDbIfNotExists) add("-ifNotExists", true) else add("-ifExists", true)
            if (allowOthers) add("-tcpAllowOthers")
            managementPassword?.also { add("-tcpPassword", it) }
            add("-tcpPort", port)
            add("-baseDir", baseDir.file(canonical = true, absolute = true))
        }.map(Any::toString).toTypedArray()

    override fun run() {
        val server = TcpServer().apply {
            init(*serverArgs())
            setShutdownHandler { start() }
            start()
        }
        echo("TCP server started: ${server.url}")
        echo("baseDir: ${baseDir.file(canonical = true, absolute = true)}")
        server.listen()
    }

}
