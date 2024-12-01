package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import h2databasetool.cmd.ui.Style
import h2databasetool.env.Env
import org.h2.util.MathUtils.secureRandomBytes
import org.h2.util.StringUtils.convertBytesToHex

class GenerateAdminPasswordCommand : Runnable, CliktCommand(NAME) {


    override fun help(context: Context): String =
        """ |Generates a random number of bytes using s secure random generator.
            |
            |> **${Style.notice("NOTE:")}** By default, the generator uses ${Env.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE.default} bits
            |> size.    
        """.trimMargin()

    private val bits by option("--bits", "-b", envvar = Env.H2TOOL_ADMIN_PASSWORD_BITS.envVariable)
        .choice(*sizeChoices)
        .help("Number of bits size of generated password")

    override fun run() {

        val password = (bits?.toInt() ?: Env.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE())
            .let(::secureRandomBytes)
            .let(::convertBytesToHex)

        echo("admin password: $password")
    }

    companion object {
        private const val NAME = "generateAdminPassword"
        private val sizeChoices = Env.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE
            .permittedSizes
            .sorted()
            .map { "$it" to it }
            .toTypedArray()
    }

}