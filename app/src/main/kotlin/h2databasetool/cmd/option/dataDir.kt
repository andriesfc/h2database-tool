package h2databasetool.cmd.option

import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.types.file
import h2databasetool.commons.h2.toH2BaseDir
import h2databasetool.env.Env

fun RawOption.dataDir() =
    Env.H2ToolDataDir.run {
        help("Directory of H2 database files")
            .copy(envvar = envVariable, names = setOf("--data"))
            .file().convert { file -> file.toH2BaseDir() }
            .default(default().toH2BaseDir(), default().toH2BaseDir().dir().path)
    }
