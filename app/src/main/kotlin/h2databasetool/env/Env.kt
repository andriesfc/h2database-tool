@file:Suppress("ClassName")

package h2databasetool.env

import com.github.ajalt.clikt.core.CliktError
import h2databasetool.cmd.ui.Style
import h2databasetool.commons.terminal.NL

sealed class Env<out T : Any>(val variable: String, open val default: T, val description: String) {

    val isActive: Boolean get() = System.getenv(variable) != null
    fun get(): String = System.getenv(variable)

    object H2TOOL_ADMIN_PASSWORD_BITS : Env<UShort>(
        "H2TOOL_ADMIN_PASSWORD_BITS", 16u,
        "Bit size used to generate admin passwords."
    )

    data object H2TOOL_ALWAYS_QUOTE_SCHEMA : Env<Boolean>(
        "H2TOOL_ALWAYS_QUOTE_SCHEMA", false,
        "Always quote the schema names when creating/setting up a new schema."
    )

    data object H2TOOL_DATA_DIR : Env<String>(
        "H2TOOL_DATA_DIR", "~/.h2/data",
        "The directory in which H2 databases reside."
    )

    data object H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS : Env<Boolean>(
        "H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS", false,
        "Determine if running database server allows network connections from other than the host the server runs."
    )

    data object H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS : Env<Boolean>(
        "H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS", false,
        "Determine if the database server should employ virtual threads to handle client requests."
    )

    data object H2TOOL_SERVER_HOST : Env<String>(
        "H2TOOL_SERVER_HOST", "localhost",
        "The name/address a running server should bind to at startup."
    ) {
        private const val H2_BIND_ADDRESS = "h2.bindAddress"
        operator fun invoke(value: String) {
            System.setProperty(H2_BIND_ADDRESS, value)
        }
    }

    data object H2TOOL_SERVER_PERMIT_CREATE_DB : Env<Boolean>(
        "H2TOOL_SERVER_PERMIT_CREATE_DB", false,
        "Whether or not to allow client connections to create databases on the server host just by attempting to connect to it."
    )

    data object H2TOOL_SERVER_PORT : Env<UShort>(
        "H2TOOL_SERVER_PORT", 2029u,
        "The port exposed to clients connecting to a running database server."
    )

    data object H2TOOL_DATABASE_USER : Env<String>(
        "H2TOOL_DATABASE_USER", "sa",
        "Default database user name when the tool creates, and/or connects to database."
    )

    data object H2TOOL_DATABASE_PASSWORD : Env<String>(
        "H2TOOL_DATABASE_PASSWORD", "secret",
        "Default database user password when the tool creates, and/or connects to database."
    )

    data object H2TOOL_TRACE_CALLS : Env<Boolean>(
        "H2TOOL_TRACE_CALLS", false,
        "Enabling statement trace files."
    )

    data object H2TOOL_SERVER_PASSWORD : Env<String>(
        "H2TOOL_SERVER_PASSWORD", "",
        """
            |Server admin password used to remotely shutdown a running database server. 
            |(Note that if not set, the tool will create a random one time password)
            |""".trimMargin()
    )

    data object H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE : Env<Int>(
        "H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE", 0,
        """
            |The number of bits a newly generated admin passwords. Note that only
            | certain sizes are permitted to ensure that passwords are reasonably
            |  secure and random.""".trimMargin()
    ) {

        override val default: Int get() = permittedSizes[1].toInt()
        val permittedSizes: List<UShort> = listOf(8u, 16u, 24u, 32u)

        private fun notPermitted(fromEnv: Any?): Nothing = throw CliktError(buildString {
            val envVarName = Style.softFocusError("($variable=$fromEnv)")
            append(
                "Invalid environment variable ", envVarName, " used to generate admin password. ",
                NL,
                "Please set ", Style.notice(variable), " to one of the following choices: "
            )
            permittedSizes.joinTo(this, prefix = "(", postfix = ")", transform = { Style.notice(it.toString()) })
            append('.')
        })

        operator fun invoke(): Int {
            val fromEnvStr = System.getenv(variable) ?: return default
            val fromEnv = fromEnvStr.toUShortOrNull() ?: notPermitted(fromEnvStr)
            return when {
                fromEnv in permittedSizes -> fromEnv.toInt()
                else -> notPermitted(fromEnv)
            }
        }
    }

    companion object {
        private val entries by lazy {
            listOf(
                H2TOOL_ADMIN_PASSWORD_BITS,
                H2TOOL_ALWAYS_QUOTE_SCHEMA,
                H2TOOL_DATA_DIR,
                H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS,
                H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS,
                H2TOOL_SERVER_HOST,
                H2TOOL_SERVER_PERMIT_CREATE_DB,
                H2TOOL_SERVER_PORT,
                H2TOOL_DATABASE_USER,
                H2TOOL_DATABASE_PASSWORD,
                H2TOOL_TRACE_CALLS,
                H2TOOL_SERVER_PASSWORD,
                H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE
            ).sortedBy { env -> env.variable.uppercase() }
        }

        fun entries() = entries
    }
}
