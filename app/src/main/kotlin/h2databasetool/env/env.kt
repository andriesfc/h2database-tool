package h2databasetool.env

import com.github.ajalt.clikt.core.CliktError
import h2databasetool.cmd.ui.Styles
import h2databasetool.commons.NL


@Suppress("ClassName")
sealed class env<out T>(val variable: String, open val default: T) {

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
    data object H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE : env<Int>("H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE", 16) {

        val permittedSizes: List<UShort> = listOf(8u, 16u, 24u, 32u)

        private fun notPermitted(fromEnv: Any?): Nothing = throw CliktError(buildString {

            val envVarName = Styles.softFocusError("($variable=$fromEnv)")

            append(
                "Invalid environment variable ", envVarName, " used to generate admin password. ",
                NL,
                "Please set ", Styles.notice(variable), " to one of the following choices: "
            )
            permittedSizes.joinTo(this, prefix = "(", postfix = ")", transform = { Styles.notice(it.toString()) })
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
}
