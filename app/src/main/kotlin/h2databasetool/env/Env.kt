@file:Suppress("ClassName")

package h2databasetool.env

import com.github.ajalt.clikt.core.CliktError
import h2databasetool.cmd.InitializeDatabaseCommand
import h2databasetool.cmd.ui.Style
import h2databasetool.commons.file
import h2databasetool.commons.stripMultiLine
import h2databasetool.commons.stripMultiLineToMargin
import h2databasetool.commons.terminal.NL
import h2databasetool.env.Env.Companion._entries
import h2databasetool.env.Env.H2TOOL_DB_FORCE_INIT_MODE.Choice.entries

/**
 * All environment values applicable to setting up defaults for this tool.
 *
 * @param T The value type.
 * @param T Type of default.
 * @property envVariable The environment variable used to configure
 *    defaults with.
 * @property default The value to use in the absence of an environment
 *    variable, or an option.
 * @property description A human friendly description (used in help).
 */
sealed class Env<out T : Any>(
    val envVariable: String,
    open val default: T,
    val description: String,
) : Comparable<Env<Any>> {

    val isSet: Boolean get() = System.getenv(envVariable) != null

    final override fun toString(): String =
        "$envVariable [$description]"

    override fun compareTo(other: Env<Any>): Int =
        envVariable.compareTo(other.envVariable, ignoreCase = true)

    open fun get(): String = System.getenv(envVariable)

    data object H2TOOL_DB_FORCE_INIT_MODE : Env<H2TOOL_DB_FORCE_INIT_MODE.Choice>(
        "H2TOOL_DB_INIT_MODE",
        Choice.FAIL,
        """Determine how to the tool should handle when the user attempts
            | to run the ${InitializeDatabaseCommand.NAME} command
            | against an existing database.""".stripMultiLineToMargin()
    ) {

        enum class Choice(val choice: String) {
            ALWAYS("always"),
            FAIL("fail"),
            SKIP("skip"),
            INIT_ONLY_SCHEMAS("schemas"),
            ;

            companion object {
                fun choices(): Map<String, Choice> = entries.associateBy(Choice::choice)
            }
        }
    }

    data object H2TOOL_SERVER_FORCE_SHUTDOWN : Env<Boolean>(
        "hen",
        false,
        "Attempts to force shutdown if the first attempt failed"
    )

    data object H2TOOL_ADMIN_PASSWORD_BITS : Env<UShort>(
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
    ) {
        override fun get(): String = when {
            isSet -> super.get()
            else -> default.file(absolute = true).path
        }
    }

    data object H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS : Env<Boolean>(
        "H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS", false,
        """Determine if running database server allows
            | network connections from other than the 
            | host the server runs.""".stripMultiLineToMargin()
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
        """
            |Whether or not to allow client connections to create databases on
            |the server host just by attempting to connect to it.""".trimMargin().stripMultiLine()
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
            |(Note that if not set, or is empty, the tool will create a random one time password)
            |""".stripMultiLineToMargin()
    )

    data object H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE : Env<Int>(
        "H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE", 0,
        """
            |The number of bits a newly generated admin passwords. Note that only
            | certain sizes are permitted to ensure that passwords are reasonably
            | secure and random.""".stripMultiLineToMargin()
    ) {

        override val default: Int get() = permittedSizes[1].toInt()
        val permittedSizes: List<UShort> = listOf<UShort>(8u, 16u, 24u, 32u).sorted()

        private fun notPermitted(fromEnv: Any?): Nothing = throw CliktError(buildString {
            val envVarName = Style.softFocusError("($envVariable=$fromEnv)")
            append(
                "Invalid environment variable ", envVarName, " used to generate admin password. ",
                NL,
                "Please set ", Style.notice(envVariable), " to one of the following choices: "
            )
            permittedSizes.joinTo(this, prefix = "(", postfix = ")", transform = { Style.notice(it.toString()) })
            append('.')
        })

        operator fun invoke(): Int {
            val fromEnvStr = System.getenv(envVariable) ?: return default
            val fromEnv = fromEnvStr.toUShortOrNull() ?: notPermitted(fromEnvStr)
            return when {
                fromEnv in permittedSizes -> fromEnv.toInt()
                else -> notPermitted(fromEnv)
            }
        }
    }

    companion object {

        /**
         * Build the list of entries on lazily otherwise the compiler croaks with a
         * NPE.
         * > **VERY IMPORTANT**: This is a hand-coded list because we need to avoid
         * > reflection as much as possible on account of using the
         * > Graal-toolchain.
         *
         * todo: **Please report the NPE upstream as bug.**
         */
        private val _entries: List<Env<Any>> by lazy {
            listOf(
                H2TOOL_ADMIN_PASSWORD_BITS,
                H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE,
                H2TOOL_ALWAYS_QUOTE_SCHEMA,
                H2TOOL_DATABASE_PASSWORD,
                H2TOOL_DATABASE_USER,
                H2TOOL_DATA_DIR,
                H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS,
                H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS,
                H2TOOL_SERVER_FORCE_SHUTDOWN,
                H2TOOL_SERVER_HOST,
                H2TOOL_SERVER_PASSWORD,
                H2TOOL_SERVER_PERMIT_CREATE_DB,
                H2TOOL_SERVER_PORT,
                H2TOOL_TRACE_CALLS,
                H2TOOL_DB_FORCE_INIT_MODE,
            ).sorted()
        }

        /**
         * A sorted list of `Env` entries.
         * > **Important**: Remember to add any new object instance to the
         * > [_entries] list.
         *
         * @see _entries
         */
        @JvmStatic
        fun entries(): List<Env<Any>> = _entries
    }
}
