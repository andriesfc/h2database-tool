@file:JvmName("H2DbToolApp")

package h2databasetool

import com.github.ajalt.clikt.core.main
import h2databasetool.cmd.*

fun main(args: Array<String>) =
    Bootstrap(
        listOf(
            AboutTool(),
            InitializeDatabase(),
            ServeDatabases(),
            NewAdminPassword(),
            ShutdownServer(),
            DeleteDbFiles(),
            ListDbFiles()
        ).sortedBy { command -> command.commandName.lowercase() }
    ).main(args)

