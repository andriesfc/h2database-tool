@file:JvmName("H2ToolMain")

package h2databasetool.app

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import h2databasetool.cmd.InitializeDatabaseCommand
import h2databasetool.cmd.ServeDatabasesCommand
import h2databasetool.cmd.ShutdownTcpServer
import h2databasetool.cmd.AboutToolCommand

fun main(args: Array<String>) =
    bootstrap().subcommands(
        AboutToolCommand(),
        InitializeDatabaseCommand(),
        ServeDatabasesCommand(),
        ShutdownTcpServer(),
    ).main(args)




