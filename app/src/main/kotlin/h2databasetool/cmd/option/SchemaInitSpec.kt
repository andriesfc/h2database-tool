package h2databasetool.cmd.option

import com.github.ajalt.clikt.parameters.options.*
import h2databasetool.commons.file
import h2databasetool.commons.secondOrNull
import java.io.File

data class SchemaInitSpec(
    val schema: String,
    val schemaInitFile: File?,
    val onlyDrop: Boolean = false,
) {

    override fun toString(): String = buildString {
        append(
            "schema:",
            when {
                onlyDrop -> "drop:"
                schemaInitFile == null -> "create:"
                else -> "init:"
            },
            schema
        )
        if (schemaInitFile != null) append(":", schemaInitFile.toURI())
    }

    companion object {
        const val DROP_PREFIX = "drop:"
    }
}

inline fun RawOption.schemaInitSpecs(crossinline quoted: () -> Boolean): OptionWithValues<Set<SchemaInitSpec>, List<String>, String> =
    copy(names = setOf("-s", "--schema"))
        .help("Configure one or more schemas on the target database.")
        .varargValues(min = 1, max = 2)
        .transformAll { values: List<List<String>> ->
            val quoted = quoted()
            values.map { args ->
                val onlyDrop = args[0].startsWith(SchemaInitSpec.DROP_PREFIX)
                val schemaName = args[0].substringAfter(SchemaInitSpec.DROP_PREFIX)
                    .let { schemaName -> if (quoted) "\"$schemaName\"" else schemaName }
                val schemaScriptFile =
                    args.secondOrNull().takeUnless { onlyDrop }?.file(canonical = true, absolute = true)
                SchemaInitSpec(
                    schemaName,
                    schemaScriptFile,
                    onlyDrop
                )
            }
        }.unique()

