@file:Suppress("ConstPropertyName")

package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import h2databasetool.env.EnvVar.H2TOOL_ALWAYS_QUOTE_SCHEMA
import h2databasetool.env.EnvVar.H2TOOL_BASE_DIR
import h2databasetool.env.EnvVar.H2TOOL_DATABASE_PASSWORD
import h2databasetool.env.EnvVar.H2TOOL_DATABASE_USER
import h2databasetool.env.EnvDefault
import h2databasetool.utils.*
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import org.h2.jdbcx.JdbcDataSource
import java.io.File
import java.sql.DriverManager

class InitializeDatabase : CliktCommand("initDb") {

    override fun help(context: Context): String =
        """
        Create a local H2 database in the specified base data directory.
        
        ## Use of defaults
        
        The tool uses various defaults when initializing a new database. These defaults may be overridden using
        either options on the command line, or by using specific environment variables:
        
        | Environment Variable            | What it overrides                                                  | Default      |
        |---------------------------------|--------------------------------------------------------------------| -------------|
        | `${H2TOOL_BASE_DIR}`            | The base directory for all databases.                              | `${EnvDefault.BASE_DIR}` |
        | `${H2TOOL_DATABASE_USER}`       | The user name (owner) of the database.                             | `${EnvDefault.DATABASE_USER}`         |
        | `${H2TOOL_DATABASE_PASSWORD}`   | The password for the new database.`                                | `${EnvDefault.H2TOOL_USER_PASSWORD}`  |
        | `${H2TOOL_ALWAYS_QUOTE_SCHEMA}` | Preserve the case for schema names when creating them before hand. | `${EnvDefault.H2TOOL_ALWAYS_QUOTE_SCHEMA}` |
        
        ## Initializing scripts
        
        Initializing scripts are handled on a per schema basis using the `--init-script` option. To specify a 
        a specific script use the following syntax: `--init-script <schema>:<schema-init-file>`.  
        
        """.trimIndent()

    private val dataDir by option(metavar = "H2_DATA_DIRECTORY", envvar = H2TOOL_BASE_DIR)
        .help("Location of database")
        .default(EnvDefault.BASE_DIR)

    private val dryRun: Boolean by option("--dry-run")
        .flag(default = false)
        .help("Do a dry run by printing command output to STDOUT.")

    private val forceInitIfExists by option("--force")
        .help("Overwrite existing database if exists")
        .flag()

    private val quoted by option(
        "--quote-schema-name",
        envvar = H2TOOL_ALWAYS_QUOTE_SCHEMA
    ).help("Always quote schema names").flag(
        default = EnvDefault.H2TOOL_ALWAYS_QUOTE_SCHEMA,
        defaultForHelp = "${EnvDefault.H2TOOL_ALWAYS_QUOTE_SCHEMA}"
    )

    private val user by option(
        "--user",
        help = "JDBC user name",
        metavar = "name",
        envvar = H2TOOL_DATABASE_USER,
    ).default(EnvDefault.DATABASE_USER)

    private val password by option(
        "--password",
        metavar = "secret",
        help = "JDBC password (please change this if need be).",
        envvar = H2TOOL_DATABASE_PASSWORD,
    ).default(EnvDefault.H2TOOL_USER_PASSWORD)

    private val schemas by option(
        "--init-schema","-s",
        metavar = "schema-name${SCHEMA_DELIMITER}optional-script-file"
    ).help(
        """
        Optionally create one or more schemas.
        Note that any script passed to the schema will be executed with in the context
        of the schema.
        """.trimIndent()
    ).convert { spec ->
        spec.withBeforeAndAfter(SCHEMA_DELIMITER) { before, after, _ ->
            before to after?.file()
        }
    }.multiple()

    private val initScript by option("--init", metavar = "script-file")
        .help(
            """
            Scrip file to execute once the database has been successfully created.
            """.trimIndent()
        ).convert { it.file(canonical = true, absolute = true) }

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
            DriverManager.getConnection(
                jdbcUrl,
                user,
                password
            ).close()
        }

        processInitializer(jdbcUrl)
        processSchemaInitializers(jdbcUrl)

    }

    private fun processInitializer(jdbcUrl: String) {
        val script = initScript ?: return
        using(datasource(jdbcUrl).connection) { executeScript(script) }
    }

    private inline fun doAction(announcement: String, runAction: () -> Unit) {
        echoMarkdown(announcement)
        if (!dryRun) runAction()
    }

    private fun datasource(jdbcUrl: String) = JdbcDataSource().also { ds ->
        ds.setURL(jdbcUrl)
        ds.setUser(user)
        ds.setPassword(password)
        ds.connection.close()
    }

    private fun processSchemaInitializers(jdbcUrl: String) {
        val datasource = datasource(jdbcUrl)
        fun initSchema(schema: String, schemaInitScript: File?) = doAction("Initializing schema $schema") {
            using(datasource.connection) {
                val schemaIsPublic = schema.equals("public", ignoreCase = true)
                val schemaName = if (quoted && !schemaIsPublic) "\"$schema\"" else schema
                if (!schemaIsPublic) try {
                    executeScript("create schema $schemaName")
                } catch (e: JdbcSQLSyntaxErrorException) {
                    if (!forceInitIfExists || !e.failedOnExistingSchema()) throw e
                    executeScript("drop schema $schemaName cascade")
                }
                if (schemaInitScript != null) {
                    executeScript("set schema $schemaName")
                    executeScript(schemaInitScript)
                }
            }
        }
        schemas.forEach { (schema, schemaInitScript) -> initSchema(schema, schemaInitScript) }
    }

    companion object {
        private const val SCHEMA_DELIMITER = ":"
        private fun JdbcSQLSyntaxErrorException.failedOnExistingSchema(): Boolean {
            val e = message?.lowercase() ?: return false
            val a = e.indexOf("schema").takeUnless { it == -1 } ?: return false
            val b = e.indexOf("already exists").takeUnless { it == -1 || it < a } ?: return false
            return true
        }
    }
}