package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.mordant.rendering.TextStyles.bold
import h2databasetool.BuildInfo
import h2databasetool.cmd.ui.Style.boldEmphasis
import h2databasetool.cmd.ui.Style.h1
import h2databasetool.cmd.ui.Style.notice
import h2databasetool.cmd.ui.Style.softFocus
import h2databasetool.commons.*
import h2databasetool.commons.terminal.NL
import h2databasetool.commons.terminal.echoMarkdown
import h2databasetool.commons.terminal.fail
import h2databasetool.env.Env
import org.h2.jdbcx.JdbcDataSource
import java.io.File
import java.sql.SQLException
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
        val cmd = boldEmphasis(BuildInfo.APP_EXE)

        val forceInitChoices = {
            buildString() {
                Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.choices().onEachIndexed { i, (choice, mode) ->
                    appendLine(
                        "| ", (i + 1), ". ", bold(choice), bold(": "),
                        when (mode) {
                            Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.ALWAYS -> "Always attempt to delete the whole database."
                            Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.FAIL -> "Fails with an error"
                            Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.SKIP -> "Do not attempt to re-initialize the database"
                            Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.INIT_ONLY_SCHEMAS -> """
                                    |Only attempt to initialize schemas (if present),
                                    | by first deleting the schema and then apply the
                                    | supplied script.""".stripMultiLineToMargin()
                        }
                    )
                }
            }
        }
        return """
            |Create a new database in the specified base directory.
            |$NL
            |Note that the forcing the re initializing of an existing database may result
            |in a failure. This behaviour can be controlled via the `--force-init` option which provides 
            |the following choices:
            | 
             ${forceInitChoices()}
            | 
            |${h1("EXAMPLES")}
            | 
            |1. Create a coin collection
            |   ```shell
            |   $cmd initDb myCoinDb
            |   ```
            |2. Create my business database with a customer and stock schemas.
            |   ```shell
            |   $cmd initDb myBizDb --init-schema customer --init-schema stock 
            |   ```
            |3. Force the re-creation of schemas the business database
            |   ```shell
            |   $cmd initDb myBizDb --init-schema customer --init-schema stock --force-init always
            |   ```
            |4. Initialize a database named __finTackDb__ with a schema called expenses which needs to be set up
            |   with a script `../templates/expenses-template.sql` 
            |   ```shell
            |   $cmd initDb finTrackDb --init-schema expenses ../template/expenses-template.sql
            |   ```
            |""".trimMargin()

    }

    private val dataDir by option(metavar = "H2 data directory", envvar = Env.H2TOOL_DATA_DIR.envVariable)
        .help("Location of database")
        .default(Env.H2TOOL_DATA_DIR.default)

    private val dryRun: Boolean by option("--dry-run")
        .flag(default = false)
        .help("Do a dry run by printing command output to STDOUT.")


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

    private val forceInit by option("--force-init")
        .choice(Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.choices())
        .default(Env.H2TOOL_DB_FORCE_INIT_MODE.default)
        .help(Env.H2TOOL_DB_FORCE_INIT_MODE.description)

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

        val baseDir = dataDir.file().also { baseDir ->
            if (!baseDir.exists()) doAction("Base data dir not found. Attempting to create : $baseDir") {
                if (!baseDir.mkdirs())
                    fail("Unable to create base directory: $baseDir")
            }
        }

        val jdbcUrl = "jdbc:h2:$baseDir/$database"
        var performInitDb = true
        var performInitDbSchemas = true

        if (baseDir.containsDb(database)) {
            when (forceInit) {

                Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.ALWAYS -> doAction("Forcing re-initializing the database $jdbcUrl.") {
                    baseDir.files { (_, name, _) -> (name == database) }.forEach { file ->
                        if (!file.delete())
                            fail("Unable to delete data base file: $file")
                    }
                }

                Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.FAIL -> doAction("Not allowed to force initialize database $jdbcUrl.") {
                    fail("Not allowed to force initializing database $jdbcUrl")
                }

                Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.SKIP -> doAction("Skip initializing database $jdbcUrl") {
                    performInitDb = false
                    performInitDbSchemas = false
                }

                Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.INIT_ONLY_SCHEMAS -> doAction("Only init schemas in database $jdbcUrl") {
                    performInitDbSchemas = true
                    performInitDb = false
                }
            }
        }

        doAction("Initialize H2 database: __${jdbcUrl}__ (user=$user, password=$password)") {
            datasource(
                jdbcUrl,
                testOnce = true
            )
        }

        if (performInitDb || performInitDbSchemas) doAction("Initialize data source:$jdbcUrl") {
            val ds = datasource(jdbcUrl)
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

    private inline fun doAction(announcement: String, runAction: () -> Unit) {
        echoMarkdown(announcement)
        if (!dryRun) runAction()
    }

    private fun datasource(jdbcUrl: String, testOnce: Boolean = false) = JdbcDataSource().also { ds ->
        ds.setURL(jdbcUrl)
        ds.setUser(user)
        ds.setPassword(password)
        if (testOnce) ds.runCatching { connection.close() }.onFailure { e ->
            when (e) {
                is SQLException -> fail("Unable to connect to $jdbcUrl: ${e.message} [errorCode=${e.errorCode}]")
                else -> throw e
            }
        }
    }

}

