package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import h2databasetool.cmd.ui.Style.notice
import h2databasetool.cmd.ui.Style.softFocus
import h2databasetool.commons.*
import h2databasetool.commons.terminal.fail
import h2databasetool.env.Env
import org.h2.jdbcx.JdbcDataSource
import java.io.File
import java.sql.SQLException
import javax.sql.DataSource

class InitDb : CliktCommand(COMMAND) {

    companion object {
        const val COMMAND = "initDb"
    }

    private data class SchemaInitializer(
        val schema: String,
        val schemaInitScript: File?,
    )

    override fun help(context: Context): String = "Create a new database in the specified base directory."

    private val dataDir by option(metavar = "H2 data directory", envvar = Env.H2TOOL_DATA_DIR.envVariable)
        .help("Location of database")
        .default(Env.H2TOOL_DATA_DIR.default)

    private val quoted by option(
        "--quote-schema-name",
        envvar = Env.H2TOOL_ALWAYS_QUOTE_SCHEMA.envVariable,
    ).help("Always quote schema names").flag(
        default = Env.H2TOOL_ALWAYS_QUOTE_SCHEMA.default,
        defaultForHelp = "${Env.H2TOOL_ALWAYS_QUOTE_SCHEMA.default}"
    )

    private val user by option(
        "--user",
        help = "JDBC user name",
        metavar = "name",
        envvar = Env.H2TOOL_DATABASE_USER.envVariable,
    ).default(Env.H2TOOL_DATABASE_USER.default)

    private val password by option(
        "--password",
        metavar = "secret",
        help = "JDBC password (please change this if need be).",
        envvar = Env.H2TOOL_DATABASE_PASSWORD.envVariable,
    ).default(Env.H2TOOL_DATABASE_PASSWORD.default)

    private val initScript by option("--init", "-i", metavar = "script-file")
        .help(
            """
            Scrip file to execute once the database has been successfully created.
            """.trimIndent()
        ).convert { it.file(canonical = true, absolute = true) }


    private val initSchemas by option("--init-schema", "-s", metavar = "<schema [file]>")
        .help(
            """
            Create one or more schemas, and optionally execute a supplied sql script file against the schema.
            """.trimIndent()
        ).varargValues(1, 2).transformAll() { optionArgs: List<List<String>> ->
            optionArgs.map { a ->
                SchemaInitializer(
                    schema = a.first().let { name -> if (quoted) "\"$name\"" else name },
                    schemaInitScript = a.secondOrNull()?.let(::File)
                )
            }
        }.unique()


    private val skipConnectionCheck by option(
        envvar = Env.H2TOOL_SKIP_CONNECTION_CHECK.envVariable,
    ).flag(default = true).help(Env.H2TOOL_SKIP_CONNECTION_CHECK.description)

    private val database by argument(
        name = "database-name",
        help = "Name of the new database to initialize."
    )
    private val ifExists by option("--if-exists")
        .choice(Env.H2TOOL_DB_FORCE_INIT.IfExistsChoice.choices())
        .help(Env.H2TOOL_DB_FORCE_INIT.description)


    private fun ifExistsChoice() = ifExists ?: Env.H2TOOL_DB_FORCE_INIT.default

    override fun run() {

        val baseDir = dataDir.file().also { baseDir ->
            if (!baseDir.exists() && !baseDir.mkdirs())
                fail("Unable to create base directory: $baseDir")
        }

        val jdbcUrl = "jdbc:h2:$baseDir/$database"
        var performInitDb = true
        var performInitDbSchemas = true

        if (baseDir.containsDb(database)) {
            when (ifExistsChoice()) {

                Env.H2TOOL_DB_FORCE_INIT.IfExistsChoice.FORCED -> {
                    echo("Forcing re-initializing the database $jdbcUrl")
                    baseDir.files { (_, name, _) -> (name == database) }.forEach { file ->
                        if (!file.delete())
                            fail("Unable to delete data base file: $file")
                    }
                }

                Env.H2TOOL_DB_FORCE_INIT.IfExistsChoice.FAIL -> {
                    fail("Not allowed to force initializing database $jdbcUrl")
                }

                Env.H2TOOL_DB_FORCE_INIT.IfExistsChoice.IGNORE -> {
                    echo("Skip initializing database $jdbcUrl")
                    performInitDb = false
                    performInitDbSchemas = false
                }

                Env.H2TOOL_DB_FORCE_INIT.IfExistsChoice.INIT_ONLY_SCHEMAS -> {
                    echo("Only initializing schema(s) in database $jdbcUrl")
                    performInitDbSchemas = true
                    performInitDb = false
                }
            }
        }


        val performInit = performInitDb || performInitDbSchemas
        val runTestBeforeInit = !skipConnectionCheck || !performInit
        val ds = datasourceOf(jdbcUrl, runTestBeforeInit)

        if (performInitDb) {
            echo("Performing database initialization...")
            ds.takeIf({ performInitDbSchemas })?.processSchemaInitializers()
            ds.takeIf({ performInitDb })?.processInitializer()
        }
    }


    private fun reportScriptExecutionError(e: ScriptExecutionError) {
        echo(e.message, err = true)
    }

    private fun DataSource.processInitializer() {
        val script = initScript ?: return
        val e = using(connection) { runCatching { executeScript(script) }.exceptionOrNull() }
        when (e) {
            is ScriptExecutionError -> reportScriptExecutionError(e)
            null -> return
            else -> throw e
        }
    }

    private fun DataSource.processSchemaInitializers() {

        if (initSchemas.isEmpty())
            return

        for ((schema, schemaScriptFile) in initSchemas) {
            echo(buildString {
                append("Initializing ${notice(schema)} schema")
                schemaScriptFile?.also { append(" ", softFocus("(using script $schemaScriptFile)")) }
                append('.')
            })
            using(connection) { executeScript("drop schema $schema if exists cascade") }
            using(connection) { executeScript("create schema $schema") }
            schemaScriptFile ?: continue
            val e = using(connection) {
                executeScript("set schema $schema")
                runCatching { executeScript(schemaScriptFile) }.exceptionOrNull()
            }
            when (e) {
                is ScriptExecutionError -> reportScriptExecutionError(e)
                null -> continue
                else -> throw e
            }
        }
    }


    private fun datasourceOf(jdbcUrl: String, testConnection: Boolean = false) = JdbcDataSource().also { ds ->
        ds.setURL(jdbcUrl)
        ds.setUser(user)
        ds.setPassword(password)
        if (testConnection) ds.runCatching { connection.close() }.onFailure { e ->
            when (e) {
                is SQLException -> fail("Unable to connect to $jdbcUrl: ${e.message} [errorCode=${e.errorCode}]")
                else -> throw e
            }
        }
    }

}

