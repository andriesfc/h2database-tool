package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import h2databasetool.env.env
import h2databasetool.utils.resourceOfClassWithExt
import h2databasetool.utils.second
import org.h2.util.MathUtils.secureRandomBytes
import org.h2.util.StringUtils.convertBytesToHex

class GenerateAdminPasswordCommand : Runnable, CliktCommand("adminpassword") {

    private val helpDoc = resourceOfClassWithExt<GenerateAdminPasswordCommand>("help.md")

    override fun help(context: Context): String = helpDoc.readText()

    private val bits by option("--bits", "-b", envvar = env.H2TOOL_ADMIN_PASSWORD_BITS.envvar)
        .choice(*sizeChoices)
        .default(sizeChoices.second().second)
        .help("Number of bits size of generated password")

    override fun run() {

        val password = bits.toInt()
            .let(::secureRandomBytes)
            .let(::convertBytesToHex)

        echo("admin password: $password")
    }

    companion object {
        private val sizeChoices = env.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE
            .permittedSizes
            .sorted()
            .map { "$it" to it }
            .toTypedArray()
    }

}