package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import h2databasetool.BuildInfo
import h2databasetool.cmd.ui.Style
import h2databasetool.cmd.ui.Style.h1
import h2databasetool.env.Env
import h2databasetool.commons.*
import h2databasetool.commons.terminal.echoMarkdown
import h2databasetool.commons.terminal.fail
import org.h2.jdbcx.JdbcDataSource
import java.io.File
import javax.sql.DataSource

class InitializeDatabaseCommand : CliktCommand(NAME) {

    companion object {
        const val NAME = "initDb"
    }

    private data class SchemaInitializer(
        val schema: String,
        val schemaInitScript: File?,
    )

    override fun help(context: Context): String {
        val cmd = Style.boldEmphasis(BuildInfo.APP_EXE)
        return """
             Create a new database in the specified base directory.
             
             ${h1("EXAMPLES")}
             
            1. Create a coin collection
               ```shell
               $cmd initDb myCoinDb
               ```
            2. Create my business database with a customer and stock schemas.
               ```shell
               $cmd initDb myBizDb --init-schema customer --init-schema stock 
               ```
            3. Force the re-creation of schemas the business database
               ```shell
               $cmd initDb myBizDb --init-schema customer --init-schema stock --force
               ```
            4. Initialize a database named __finTackDb__ with a schema called expenses which needs to be set up
               with a script `../templates/expenses-template.sql` 
               ```shell
               $cmd initDb finTrackDb --init-schema expenses ../template/expenses-template.sql
               ```
            """.trimIndent()
    }

    private val dataDir by option(metavar = "H2 data directory", envvar = Env.H2TOOL_DATA_DIR.variable)
        .help("Location of database")
        .default(Env.H2TOOL_DATA_DIR.default)

    private val dryRun: Boolean by option("--dry-run")
        .flag(default = false)
        .help("Do a dry run by printing command output to STDOUT.")

    private val forceInitIfExists by option("--force")
        .help("Overwrite existing database if exists")
        .flag()

    private val quoted by option(
        "--quote-schema-name",
        envvar = Env.H2TOOL_ALWAYS_QUOTE_SCHEMA.variable,
    ).help("Always quote schema names").flag(
        default = Env.H2TOOL_ALWAYS_QUOTE_SCHEMA.default,
        defaultForHelp = "${Env.H2TOOL_ALWAYS_QUOTE_SCHEMA.default}"
    )

    private val user by option(
        "--user",
        help = "JDBC user name",
        metavar = "name",
        envvar = Env.H2TOOL_DATABASE_USER.variable,
    ).default(Env.H2TOOL_DATABASE_USER.default)

    private val password by option(
        "--password",
        metavar = "secret",
        help = "JDBC password (please change this if need be).",
        envvar = Env.H2TOOL_DATABASE_PASSWORD.variable,
    ).default(Env.H2TOOL_DATABASE_PASSWORD.default)

    private val initScript by option("--init", "-i", metavar = "script-file")
        .help(
            """
            Scrip file to execute once the database has been successfully created.
            """.trimIndent()
        ).convert { it.file(canonical = true, absolute = true) }


    private val schemaInitializers by option("--init-schema", "-s", metavar = "<schema [file]>")
        .help(
            """
            Create one or more schemas, and optionally execute a supplied sql script file against the schema.
            """.trimIndent()
        ).varargValues(1, 2).transformAll() { optionArgs: List<List<String>> ->
            optionArgs.map { a ->
                SchemaInitializer(
                    schema = a.first(),
                    schemaInitScript = a.secondOrNull()?.let(::File)
                )
            }
        }.unique()

    private val database by argument(
        name = "database-name", help = "Name of the new database to initialize."
    )

    override fun run() {

        if (dryRun) {
            echoMarkdown(
                """
                👉 **NOTE**: Only performing a dry run
                """.trimIndent()
            )
        }

        val baseDir = dataDir
            .file().apply {
                if (!exists() && !mkdirs())
                    fail("Unable to create base directory: $this")
            }

        val jdbcUrl = "jdbc:h2:$baseDir/$database"

        if (baseDir.containsDb(database)) {
            if (!forceInitIfExists)
                fail("Database $database already exists at $baseDir")
            else doAction("Removing database $database from data directory: $baseDir") {
                baseDir.files { (_, name, _) -> (name == database) }.forEach { file ->
                    if (!file.delete())
                        fail("Unable to delete data base file: $file")
                }
            }
        }

        doAction("Initialize H2 database: __${jdbcUrl}__ (user=$user, password=$password)") {
            datasource(
                jdbcUrl,
                testOnce = true
            )
        }

        doAction("Initialize data source:$jdbcUrl") {
            val ds = datasource(jdbcUrl)
            ds.processSchemaInitializers()
            ds.processInitializer()
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

        if (schemaInitializers.isEmpty())
            return

        for ((schema, schemaScriptFile) in schemaInitializers) {
            echo(buildString {
                append("Initializing ${Style.notice(schema)} schema")
                schemaScriptFile?.also { append(" ", Style.softFocus("(using script $schemaScriptFile)")) }
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

    private inline fun doAction(announcement: String, runAction: () -> Unit) {
        echoMarkdown(announcement)
        if (!dryRun) runAction()
    }

    private fun datasource(jdbcUrl: String, testOnce: Boolean = false) = JdbcDataSource().also { ds ->
        ds.setURL(jdbcUrl)
        ds.setUser(user)
        ds.setPassword(password)
        ds.takeIf { testOnce }?.connection?.close()
    }

}

