package h2databasetool.cmd.option

import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import h2databasetool.env.Env

fun RawOption.jdbcUser() =
    help("JDBC user name")
        .copy(envvar = Env.H2ToolDatabaseUser.envVariable, names = setOf("--user"))
        .default(Env.H2ToolDatabaseUser.default)
