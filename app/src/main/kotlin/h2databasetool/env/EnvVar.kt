package h2databasetool.env

/** Various environment variables used to control tool commands. */
object EnvVar {

    internal fun get(variable: String, defaultValue: Int): Int {
        return System.getenv(variable)?.trim()?.toIntOrNull() ?: defaultValue
    }

    /**
     * Produce dot-trace files when client connects to a database.
     */
    const val H2TOOL_TRACE_CALLS = "H2TOOL_TRACE_CALLS"

    /** Allow connections from other hosts to databases. */
    const val H2TOOL_TCP_ALLOW_REMOTE_CONNECTIONS = "H2TOOL_TCP_ALLOW_REMOTE_CONNECTIONS"

    /**
     * Whether, or not, allow clients to create a database automatically if not
     * exists.
     */
    const val H2TOOL_PERMIT_DB_CREATION = "H2TOOL_PERMIT_DB_CREATION"

    /** The base directory used to locate existing databases. */
    const val H2TOOL_BASE_DIR = "H2TOOL_BASE_DIR"

    /** Username when creating initializing a new database. */
    const val H2TOOL_DATABASE_USER = "H2TOOL_DATABASE_USER"

    /** User password used when initializing a new database. */
    const val H2TOOL_DATABASE_PASSWORD = "H2TOOL_DATABASE_PASSWORD"

    /** Port to use when serving databases via a TCP connection. */
    const val H2TOOL_SERVER_PORT = "H2TOOL_SERVER_PORT"

    /** Enable virtual threads when serving database connections via TCP. */
    const val H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS = "H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS"

    /**
     * By default, H2 create all schema names in uppercase. Setting this to
     * `true` will preserve the case of the schema name.
     */
    const val H2TOOL_ALWAYS_QUOTE_SCHEMA = "H2TOOL_ALWAYS_QUOTE_SCHEMA"

    /** Forces shutdown of running TCP server. */
    const val H2TOOL_FORCE_SHUTDOWN = "H2TOOL_FORCE_SHUTDOWN"

    /** Host on which the database server should run on. */
    const val H2TOOL_DATABASE_HOST = "localhost"

    /**
     * A password set allowing the remote shutdown of databases running on
     * server.
     * > **NOTE**: Server will generate a random password if none is supplied.
     */
    const val H2TOOL_SERVER_PASSWORD = "h2TOOL_SERVER_PASSWORD"

    /** Admin password size to use. */
    const val H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE = "H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE"
}