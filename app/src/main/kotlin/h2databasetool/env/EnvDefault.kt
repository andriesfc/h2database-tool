package h2databasetool.env

import h2databasetool.utils.second

object EnvDefault {
    const val H2TOOL_BASE_DIR = "~/.h2/data"
    const val H2TOOL_DATABASE_USER = "sa"
    const val H2TOOL_ALWAYS_QUOTE_SCHEMA = false
    const val H2TOOL_USER_PASSWORD = "secret"
    const val H2TOOL_SERVER_PORT: UShort = 9092u
    const val H2TOOL_DATABASE_HOST = "localhost"
    const val H2TOOL_FORCE_SHUTDOWN = false
    const val H2TOOL_PERMIT_DB_CREATION = false
    const val H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS = false
    const val H2TOOL_TCP_ALLOW_REMOTE_CONNECTIONS = false
    const val H2TOOL_TRACE_CALLS = false

    val passwordSizes = setOf(8, 16, 24, 32).toSortedSet()

    val H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE: Int = passwordSizes.second()
}