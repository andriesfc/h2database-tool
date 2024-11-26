package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import h2databasetool.env.EnvDefault
import h2databasetool.env.EnvVar
import org.h2.util.MathUtils.secureRandomBytes
import org.h2.util.StringUtils.convertBytesToHex

class GenerateAdminPassword : Runnable, CliktCommand("generateAdminPassword") {

    override fun help(context: Context): String {
        return """
            Generates a new admin password.
            
            **NOTE:** Only the following size options are allowed: ${sizeOptions.keys.joinToString()}
            """.trimIndent()
    }

    private val bits by option(envvar = EnvVar.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE)
        .switch(sizeOptions)
        .default(EnvDefault.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE)
        .help("Number of bits size of generated password")

    override fun run() {
        val password = convertBytesToHex(secureRandomBytes(bits))
        echo("admin password: $password")
    }

    companion object {
        private val sizeOptions = listOf(8, 16, 24, 32).sorted().associateBy { "-${it}" }
    }
}