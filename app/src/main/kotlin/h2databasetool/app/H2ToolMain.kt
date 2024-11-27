@file:JvmName("H2ToolMain")

package h2databasetool.app

import com.github.ajalt.clikt.core.main
import h2databasetool.cmd.*

fun main(args: Array<String>) =
    bootstrap(
        AboutToolCommand(),
        InitializeDatabaseCommand(),
        ServeDatabasesCommand(),
        GenerateAdminPasswordCommand()
    ).main(args)
