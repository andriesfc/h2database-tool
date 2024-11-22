@file:JvmName("ToolMain")

package h2databasetool.app

import com.github.ajalt.clikt.core.*
import h2databasetool.cmd.InitializeNewDatabase

fun main(args: Array<String>) = bootstrap()
    .subcommands(InitializeNewDatabase())
    .main(args)



