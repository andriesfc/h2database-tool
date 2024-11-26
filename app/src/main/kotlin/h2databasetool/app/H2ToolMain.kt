@file:JvmName("H2ToolMain")

package h2databasetool.app

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import h2databasetool.cmd.InitializeDatabase
import h2databasetool.cmd.ServeDatabases
import h2databasetool.cmd.ShutdownTcpServer

fun main(args: Array<String>) =
    bootstrap().subcommands(
        InitializeDatabase(),
        ServeDatabases(),
        ShutdownTcpServer(),
    ).main(args)



