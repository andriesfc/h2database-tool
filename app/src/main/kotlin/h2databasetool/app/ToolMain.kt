@file:JvmName("ToolMain")

package h2databasetool.app

import com.github.ajalt.clikt.core.*
import h2databasetool.cmd.InitializeNewDatabase
import h2databasetool.cmd.ServeDatabase

fun main(args: Array<String>) = bootstrap()
    .subcommands(InitializeNewDatabase(), ServeDatabase())
    .main(args)



