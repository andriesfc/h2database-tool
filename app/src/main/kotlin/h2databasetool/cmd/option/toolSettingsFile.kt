package h2databasetool.cmd.option

import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.types.file

fun RawOption.toolSettingsFile() =
    help("Settings file used override all environment files and command line optiopns.")
        .copy(names = setOf("--settings-file", "-S"))
        .file(mustBeReadable = true, mustExist = true, canBeDir = false)
