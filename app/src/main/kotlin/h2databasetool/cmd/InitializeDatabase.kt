package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import h2databasetool.utils.*
import java.sql.DriverManager
import org.h2.jdbcx.JdbcDataSource
import java.io.File

class InitializeDatabase : CliktCommand("initDb") {

    override fun help(context: Context): String = """
        Creates a local H2 database.
        """.trimIndent()

    private val dataDir by option(metavar = "H2_DATA_DIRECTORY", envvar = H2_DATA_DIR)
        .help("Location of database")
        .default("~/.h2/data")

    private val dryRun: Boolean by option("--dry-run")
        .flag(default = false)
        .help("Only do a dry run by printing command output to STDOUT.")

    private val overwriteIfExists by option("--force")
        .help("Overwrite existing database if exists")
        .flag()

    private val user by option(
        "--user",
        help = "JDBC user name",
        metavar = "name",
        envvar = H2_DATABASE_USER,
    ).default("sa")

    private val password by option(
        "--password",
        metavar = "secret",
        help = "JDBC password (please change this if need be).",
        envvar = H2_DATABASE_PASSWORD,
    ).default("secret")

    private val schemas by option("--schema", metavar = "SCHEMA_NAME[${SCHEMA_SCRIPT_DELIMITER}INIT_SCRIPT]")
        .help("Optionally create one or more schemas.")
        .convert { spec -> spec.splitAround(SCHEMA_SCRIPT_DELIMITER, { null }).mapSecond { it?.file() } }
        .multiple()

    private val database by argument(
        name = "database-name", help = "Name of the database to create."
    )

    override fun run() {

        if (dryRun) {
            echoMarkdown(
                """
                👉 **NOTE**: Only performing a dry run
                """.trimIndent()
            )
        }

        val dir = dataDir.file()
        val jdbcUrl = "jdbc:h2:$dir/$database"

        val existsAlready = dir.resolve("$database.mv.db").exists()
        if (existsAlready) {
            if (!overwriteIfExists)
                throw PrintMessage("Database $database already exists at $dir", statusCode = 1)
            else doAction("Removing database $database from data directory: $dir") {
                dir.files { (_, name, _) -> (name == database) }.forEach { file ->
                    if (!file.delete())
                        throw PrintMessage("Unable to delete data base file: $file", 1)
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

        initSchemas(jdbcUrl)

    }

    private inline fun doAction(announcement: String, runAction: () -> Unit) {
        echoMarkdown(announcement)
        if (!dryRun) runAction()
    }

    private fun initSchemas(jdbcUrl: String) {

        val datasource by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            val ds = JdbcDataSource()
            ds.setURL(jdbcUrl)
            ds.setUser(user)
            ds.setPassword(password)
            ds.connection.close()
            ds
        }

        fun initSchema(schema: String, script: File?) = doAction("") {
            using(datasource.connection) {
                executeScript("create schema \"$schema\"")
                if (script != null) executeScript(script)
            }
        }

        schemas.forEach { (schema, schemaInitScript) ->
            initSchema(schema, schemaInitScript)
        }
    }

    companion object {
        private const val SCHEMA_SCRIPT_DELIMITER = ":"
    }
}