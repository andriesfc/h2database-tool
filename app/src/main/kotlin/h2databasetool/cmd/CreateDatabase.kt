package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import h2databasetool.cmd.option.*
import h2databasetool.commons.ensureParents
import h2databasetool.commons.executeScript
import h2databasetool.commons.file
import h2databasetool.commons.using
import org.h2.jdbcx.JdbcDataSource
import java.sql.Connection

class CreateDatabase : CliktCommand(COMMAND) {

    override fun help(context: Context): String =
        "Creates a new database at a specific file location."

    private val fullDatabaseWithPath by argument(
        help = "Fully qualified file path (including) database name",
        name = "full-database-path"
    ).convert { path -> path.file(absolute = true, canonical = true) }

    private val user by option().jdbcUser()
    private val password by option().jdbcPassword()
    private val quoteSchemaName by option().quoteSchemaName()
    private val schemeInitializers by option(metavar = "schema-init-spec").schemaInitSpecs { quoteSchemaName }
    private val script by option().convert { script -> script.file(canonical = true, absolute = true) }
    private val dryRun by option(help = "Only perform a dry run (just output).").flag()

    override fun run() {

        val jdbcUri =
            "jdbc:h2:${fullDatabaseWithPath.toURI()}"

        echo("Create database: $jdbcUri")
        echo("        Schemas: $schemeInitializers")
        echo("         script: [${script ?: ""}]")
        echo("        Dry run: ${if (dryRun) "YES" else "NO"}")

        takeUnless { dryRun }?.initializeDb(jdbcUri)
    }

    private fun datasource() =
        JdbcDataSource().apply {
            user = this@CreateDatabase.user
            password = this@CreateDatabase.password
            setUrl("jdbc:h2:${this@CreateDatabase.fullDatabaseWithPath.toURI()}")
            connection.close()
        }

    private fun initializeDb(jdbcUri: String) {
        echo("Initializing database: $jdbcUri")
        fullDatabaseWithPath.ensureParents { "Unable to realize parents for path: $path" }
        val ds = datasource()
        if (script == null && schemeInitializers.isEmpty()) return
        script?.also { file ->
            echo("Executing script: $file")
            using(ds.connection) { executeScript(file) }
        }
        schemeInitializers.forEachIndexed() { index, spec ->
            ds.connection.use { connection ->
                connection.initSchema(
                    id = "${index + 1}: $spec",
                    spec
                )
            }
        }
    }

    private fun Connection.initSchema(id: String, spec: SchemaInitSpec) {
        echo("Initializing schema [$id]: $spec")
        val source = spec.toString()
        val (schema, script, dropOnly) = spec
        if (schemaExists(schema))
            executeScript("drop schema $schema if exists cascade", source)
        if (dropOnly)
            return
        executeScript("create schema $schema", source)
        executeScript("set schema $schema")
        script?.also { executeScript(script) }
    }

    private fun Connection.schemaExists(schema: String) =
        with(metaData.schemas) {
            var found = false
            while (next() && !found)
                found = getString(1).equals(schema, ignoreCase = true)
            found
        }

    companion object {
        const val COMMAND = "create"
    }
}
