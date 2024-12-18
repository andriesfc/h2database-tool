package h2databasetool.cmd.option

import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import h2databasetool.env.Env

fun RawOption.jdbcPassword() = help("JDBC password")
    .copy(envvar = Env.H2ToolDatabasePassword.envVariable, names = setOf("--password"))
    .default(Env.H2ToolDatabasePassword.default())
