package h2databasetool.env

/** Various environment variables used to control tool commands. */
object EnvVar {

    /** The base directory used to locate existing databases. */
    const val H2TOOL_BASE_DIR = "H2TOOL_BASE_DIR"

    /** Username when creating initializing a new database. */
    const val H2TOOL_DATABASE_USER = "H2TOOL_DATABASE_USER"

    /** User password used when initializing a new database. */
    const val H2TOOL_DATABASE_PASSWORD = "H2TOOL_DATABASE_PASSWORD"

    /** Port to use when serving databases via a TCP connection. */
    const val H2TOOL_TCP_SERVER_DATABASE_NETWORK_PORT = "H2TOOL_TCP_SERVER_DATABASE_NETWORK_PORT"

    /** Enable virtual threads when serving database connections via TCP. */
    const val H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS = "H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS"

    /**
     * By default, H2 create all schema names in uppercase. Setting this to
     * `true` will preserve the case of the schema name.
     */
    const val H2TOOL_ALWAYS_QUOTE_SCHEMA = "H2TOOL_ALWAYS_QUOTE_SCHEMA"
}