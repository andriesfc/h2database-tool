package h2databasetool.env


@Suppress("ClassName")
sealed class env<out T>(val envvar: String, val default: T) {
    data object H2TOOL_ADMIN_PASSWORD_BITS : env<UShort>("H2TOOL_ADMIN_PASSWORD_BITS", 16u)
    data object H2TOOL_ALWAYS_QOUTE_SCHEMA : env<Boolean>("H2TOOL_ALWAYS_QUOTE_SCHEMA", false)
    data object H2TOOL_DATA_DIR : env<String>("H2TOOL_DATA_DIR", "~/.h2/data")
    data object H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS : env<Boolean>("H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS", false)
    data object H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS : env<Boolean>("H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS", false)
    data object H2TOOL_SERVER_HOST : env<String>("H2TOOL_SERVER_HOST", "localhost")
    data object H2TOOL_SERVER_PERMIT_CREATE_DB : env<Boolean>("H2TOOL_SERVER_PERMIT_CREATE_DB", false)
    data object H2TOOL_SERVER_PORT : env<UShort>("H2TOOL_SERVER_PORT", 2029u)
    data object H2TOOL_DATABASE_USER : env<String>("H2TOOL_DATABASE_USER", "sa")
    data object H2TOOL_DATABASE_PASSWORD : env<String>("H2TOOL_DATABASE_PASSWORD", "sa")
    data object H2TOOL_TRACE_CALLS : env<Boolean>("H2TOOL_TRACE_CALLS", false)
    data object H2TOOL_SERVER_PASSWORD : env<String>("H2TOOL_SERVER_PASSWORD", "")
    data object H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE : env<UShort>("H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE", 16u) {
        val permittedSizes: List<UShort> = listOf(8u, 16u, 24u, 32u)
    }
}
