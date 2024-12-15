@file:Suppress("FunctionName")

package h2databasetool.cmd

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import h2databasetool.cmd.ui.Style
import h2databasetool.cmd.ui.Style.boldEmphasis
import h2databasetool.cmd.ui.Style.notice
import h2databasetool.cmd.ui.Style.softFocus
import h2databasetool.cmd.ui.Style.warn
import h2databasetool.commons.*
import h2databasetool.commons.h2.H2FileList
import h2databasetool.commons.h2.H2FileType
import h2databasetool.commons.h2.listDatabaseFiles
import h2databasetool.commons.terminal.fail
import h2databasetool.env.Env
import h2databasetool.env.Env.H2ToolDbForceInit.IfExistsChoice
import org.h2.jdbcx.JdbcDataSource
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

private typealias ScriptLet = () -> Unit

class InitializeDatabase : CliktCommand(COMMAND) {

    override fun help(context: Context): String = "Create a new database in the specified base directory."

    private val dataDir by option(metavar = "H2 data directory", envvar = Env.H2ToolDataDir.envVariable)
        .help("Location of database")
        .default(Env.H2ToolDataDir.default)

    private val quoted by option(
        "--quote-schema-name",
        envvar = Env.H2ToolAlwaysQuoteSchema.envVariable,
    ).help("Always quote schema names").flag(
        default = Env.H2ToolAlwaysQuoteSchema.default,
        defaultForHelp = "${Env.H2ToolAlwaysQuoteSchema.default}"
    )

    private val user by option(
        "--user",
        help = "JDBC user name",
        metavar = "name",
        envvar = Env.H2ToolDatabaseUser.envVariable,
    ).default(Env.H2ToolDatabaseUser.default)

    private val password by option(
        "--password",
        metavar = "secret",
        help = "JDBC password (please change this if need be).",
        envvar = Env.H2ToolDatabasePassword.envVariable,
    ).default(Env.H2ToolDatabasePassword.default)

    private val initScript by option("--init", "-i", metavar = "script-file")
        .help(
            """
            Scrip file to execute once the database has been successfully created.
            """.trimIndent()
        ).convert { it.file(canonical = true, absolute = true) }


    private val schemaInitializers: Set<SchemaInitSpec> by option(
        "-s",
        metavar = "schema-spec"
    ).schemaInitSpecs({ quoted }).help { "Configure 1 or more schemas on the target database." }

    private val skipConnectionCheck by option(
        envvar = Env.H2ToolSkipConnectionCheck.envVariable,
    ).flag().help(Env.H2ToolSkipConnectionCheck.description)

    private val database by argument(
        name = "database-name",
        help = "Name of the new database to initialize."
    )
    private val ifDataFileExists by option("--if-exists")
        .help("What to do when attempting to run ${notice(COMMAND)} command against an existing database.")
        .choice(IfExistsChoice.choices())

    private val initializer = object : Runnable {

        private lateinit var _baseDir: File
        private lateinit var _jdbcUrl: String
        private lateinit var _databaseName: String
        private var _performInitDb = true
        private var _performInitDbSchemas = true
        private var _forceDestroyLocalDb = false
        private lateinit var _handlingOfExistingDataFiles: IfExistsChoice
        private var _databaseExists: Boolean = false
        private var _databaseFiles: H2FileList = emptyList()
        private var _testConnection = false
        private var _connectionOnly = false

        fun configure() {
            _handlingOfExistingDataFiles = ifExistsChoice()
            _databaseName = database
            _baseDir = dataDir.file().ensureDir()
            _jdbcUrl = "jdbc:h2:$_baseDir/$_databaseName"
            _databaseFiles = _baseDir.listDatabaseFiles(_databaseName)
            _databaseExists = _databaseFiles.isNotEmpty()
            _forceDestroyLocalDb = _handlingOfExistingDataFiles == IfExistsChoice.FORCE && _databaseExists
            _performInitDb = this@InitializeDatabase.initScript != null
            _performInitDbSchemas = this@InitializeDatabase.schemaInitializers.isNotEmpty()
            _testConnection = !skipConnectionCheck
            _connectionOnly = true
        }

        override fun run() {
            if (_databaseExists) when (_handlingOfExistingDataFiles) {
                IfExistsChoice.FORCE -> _forceDestroyLocalDb = true
                IfExistsChoice.FAIL -> fail("Unable to initialize database \"$_databaseName\" which exist already.")
                IfExistsChoice.INIT_ONLY_SCHEMAS -> {
                    _forceDestroyLocalDb = false
                    _connectionOnly = true
                }

                IfExistsChoice.SKIP_II -> {
                    _connectionOnly = false
                    _forceDestroyLocalDb = false
                    _performInitDbSchemas = false
                    _performInitDb = false
                }
            }
            if (_forceDestroyLocalDb) destroyLocalDatabase(_databaseName, _baseDir)

            if (_performInitDb || _performInitDbSchemas || _connectionOnly)
                datasourceOf(_jdbcUrl, _testConnection).also { ds ->
                    if (_performInitDbSchemas) ds.processSchemaInitializers(_databaseName)
                    if (_performInitDb) ds.processInitializer(_databaseName)
                }
        }
    }

    private fun ifExistsChoice() = ifDataFileExists ?: Env.H2ToolDbForceInit.default

    override fun run() {
        initializer.configure()
        initializer.run()
    }


    private fun reportScriptExecutionError(e: ScriptExecutionError) {
        echo(e.message, err = true)
    }

    private fun DataSource.processInitializer(database: String) {

        val script = initScript ?: return

        echo("Running global/public script ${script.path} targeting database $database")

        val e = using(connection) { runCatching { executeScript(script) }.exceptionOrNull() }
        when (e) {
            is ScriptExecutionError -> reportScriptExecutionError(e)
            null -> return
            else -> throw e
        }
    }

    private fun destroyLocalDatabase(databaseName: String, baseDir: File) {

        val dbFiles = baseDir
            .listDatabaseFiles(databaseName)
            .onEach { (file) -> file.delete() }
            .sortedBy { (_, type) -> type.nameSuffix }
            .takeUnless { listing -> listing.isEmpty() } ?: return

        terminal.println(DataDeletionView(databaseName, baseDir, dbFiles))
    }

    private fun DataDeletionView(
        databaseName: String,
        baseDir: File,
        dbFiles: List<Pair<File, H2FileType>>,
    ): Any {
        val remaining = dbFiles.count { (file) -> file.exists() }
        return when (remaining) {
            0 -> "Deleted ${dbFiles.size} database files from $databaseName"
            else -> "Some Files was not deleted: $remaining"
        }
    }

    private fun DataSource.processSchemaInitializers(database: String) {

        if (schemaInitializers.isEmpty())
            return

        val target =
            if (schemaInitializers.size == 1) "one initializer" else "${schemaInitializers.size} initializers"

        val message = "Processing $target targeting database $database"
        echo(message)

        val dropSchema =
            { schemaName: String ->
                runScripLet {
                    echo("${warn("Dropping")} schema ${notice(schemaName)}")
                    using(connection) {
                        if (schemaExists(schemaName))
                            executeScript("drop schema if exists $schemaName cascade")
                    }
                }
            }

        val createSchema =
            { schemaName: String ->
                runScripLet {
                    echo("${boldEmphasis("Creating")} schema ${notice(schemaName)}")
                    using(connection) { executeScript("create schema $schemaName") }
                }
            }

        val runSchemaScript =
            { schemaName: String, script: File ->
                runScripLet {
                    echo("${boldEmphasis("Executing")} script ${notice(script.name)} (dir:${softFocus("${script.parent}")})")
                    using(connection) {
                        executeScript("set schema $schemaName")
                        executeScript(script)
                    }
                }
            }

        for ((schema, schemaScriptFile, onlyDrop) in schemaInitializers) {
            dropSchema(schema)
            if (onlyDrop || schemaScriptFile == null)
                continue
            createSchema(schema)
            runSchemaScript(schema, schemaScriptFile)
        }
    }

    private fun datasourceOf(jdbcUrl: String, testConnection: Boolean = false) =
        JdbcDataSource()
            .also { ds ->
                ds.setURL(jdbcUrl)
                ds.setUser(user)
                ds.setPassword(password)
                echo("Datasource configured as $jdbcUrl")
            }.also { ds ->
                if (testConnection) ds.runCatching {
                    var connectionClosed: Boolean = false
                    try {
                        connection.close()
                        connectionClosed = true
                    } finally {
                        if (!connectionClosed) connection.runCatching(Connection::close)
                    }
                }.onSuccess {
                    echo("Datasource available.")
                }.onFailure {
                    abort(it)
                }
            }

    private data class SchemaInitSpec(
        val schema: String,
        val schemaInitScript: File?,
        val onlyDrop: Boolean = false,
    )

    companion object {

        const val COMMAND = "initDb"
        const val DROP_SCRIPT_PREFIX = "drop:"

        private fun CoreCliktCommand.abort(theCause: Any): Nothing {
            echo(Style.Colors.dazzlingRed("Error:"), trailingNewline = false)
            echo(theCause)
            throw CliktError()
        }

        private fun CoreCliktCommand.runScripLet(action: ScriptLet) {
            runCatching(action).onFailure { e ->
                when (e) {
                    is SQLException, is ScriptExecutionError -> echo(e.message, err = true)
                    is IOException -> echo(e.message, err = true)
                    else -> throw e
                }
            }
        }

        private fun OptionWithValues<String?, String, String>.schemaInitSpecs(
            quoted: () -> Boolean,
            dropScriptPrefix: () -> String = { InitializeDatabase.DROP_SCRIPT_PREFIX },
        ): OptionWithValues<Set<SchemaInitSpec>, List<String>, String> =
            varargValues(1, 2).transformAll() { values: List<List<String>> ->
                val dropScriptPrefix = dropScriptPrefix()
                val quoted = quoted()
                values.map { args ->

                    val onlyDrop = args
                        .first()
                        .startsWith(dropScriptPrefix)

                    val schemaName = args
                        .first()
                        .substringAfter(dropScriptPrefix)
                        .let { if (quoted) "\"$it\"" else it }

                    val schemaScriptFile =
                        args.secondOrNull().takeUnless { onlyDrop }?.file(canonical = true, absolute = true)

                    SchemaInitSpec(
                        schema = schemaName,
                        onlyDrop = onlyDrop,
                        schemaInitScript = schemaScriptFile
                    )
                }
            }.unique()

        private fun Connection.schemaExists(schema: String) =
            with(metaData.schemas) {
                var found = false
                while (next() && !found)
                    found = getString(1).equals(schema, ignoreCase = true)
                found
            }

    }
}



