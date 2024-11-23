@file:JvmName("ToolMain")

package h2databasetool.app

import com.github.ajalt.clikt.core.*
import h2databasetool.cmd.InitializeDatabase
import h2databasetool.cmd.ServeDatabase

fun main(args: Array<String>) = bootstrap()
    .subcommands(InitializeDatabase(), ServeDatabase())
    .main(args)



