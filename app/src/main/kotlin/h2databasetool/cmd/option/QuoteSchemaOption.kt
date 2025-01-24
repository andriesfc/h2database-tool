package h2databasetool.cmd.option

import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import h2databasetool.env.Env

fun RawOption.quoteSchemaName() =
    Env.H2ToolAlwaysQuoteSchema.run {
        help(this.description).copy(envvar = this.envVariable, names = setOf("--quote-schema-name"))
            .flag(default = this.default, defaultForHelp = "$default")
    }
