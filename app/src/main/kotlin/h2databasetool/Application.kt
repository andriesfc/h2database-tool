package h2databasetool

import com.github.ajalt.clikt.core.main
import h2databasetool.cmd.*

fun main(args: Array<String>) =
    Bootstrap(
        listOf(
            AboutToolCommand(),
            InitializeDatabaseCommand(),
            ServeDatabasesCommand(),
            GenerateAdminPasswordCommand(),
            ShutdownServerCommand()
        )
    ).main(args)

