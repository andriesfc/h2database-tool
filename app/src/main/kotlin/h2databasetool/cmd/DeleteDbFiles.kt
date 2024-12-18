package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import h2databasetool.cmd.option.dataDir
import h2databasetool.cmd.ui.Style.softFocus
import h2databasetool.cmd.ui.Style.softYellowFocus

class DeleteDbFiles : CliktCommand(COMMAND) {

    override fun run() {
        val knownDbNames = dataDir.dbNames()
        databaseNames.forEach { dbName ->
            when {
                dbName in knownDbNames -> {
                    val files = dataDir.dbFiles(dbName).onEach { file -> file.delete() }
                    val (deleted, remaining) = files.fold(intArrayOf(0, 0)) { acc, file ->
                        if (file.exists()) acc[0] += 1 else acc[1] += 1
                        acc
                    }
                    echo("Deleted $deleted files in database $dbName (remaining: $remaining)")
                }

                else -> echo("No database found: $dbName [${softFocus(dataDir.dir().path)}]")
            }
        }
    }

    override fun help(context: Context): String =
        "Deleted database files from the database defined in H2 base data directory (${softYellowFocus("NB")}: Database server must not run)"

    private val dataDir by option().dataDir()

    private val databaseNames by argument(
        "database-name",
        help = "Names of the data bases to delete"
    ).multiple(required = true)


    companion object {
        const val COMMAND = "delete"
    }
}
