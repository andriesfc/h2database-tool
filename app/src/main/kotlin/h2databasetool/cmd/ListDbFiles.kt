package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.option
import h2databasetool.cmd.option.dataDir

class ListDbFiles() : CliktCommand(COMMAND) {

    private val baseDir by option().dataDir()

    override fun help(context: Context): String {
        return "List known database based on the H2 base directory."
    }

    override fun run() {
        baseDir.dbNames().forEach { dbName ->
            echo(dbName)
        }
    }

    companion object {
        const val COMMAND = "list"
    }
}
