package h2databasetool

import com.github.ajalt.clikt.core.main
import h2databasetool.cmd.AboutToolCommand
import h2databasetool.cmd.GenerateAdminPasswordCommand
import h2databasetool.cmd.InitializeDatabaseCommand
import h2databasetool.cmd.ServeDatabasesCommand

fun main(args: Array<String>) =
    bootstrap(
        AboutToolCommand(),
        InitializeDatabaseCommand(),
        ServeDatabasesCommand(),
        GenerateAdminPasswordCommand(),
    ) { appHelpDoc(it, BuildInfo.APP_NAME) }.main(args)

