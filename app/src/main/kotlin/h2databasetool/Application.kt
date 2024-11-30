package h2databasetool

import com.github.ajalt.clikt.core.main
import h2databasetool.cmd.AboutToolCommand
import h2databasetool.cmd.GenerateAdminPasswordCommand
import h2databasetool.cmd.InitializeDatabaseCommand
import h2databasetool.cmd.ServeDatabasesCommand

fun main(args: Array<String>) =
    Bootstrap(
        listOf(
            AboutToolCommand(),
            InitializeDatabaseCommand(),
            ServeDatabasesCommand(),
            GenerateAdminPasswordCommand()
        )
    ).main(args)

