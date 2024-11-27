package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import h2databasetool.env.EnvDefault
import h2databasetool.env.EnvVar
import h2databasetool.utils.resourceOfClassWithExt
import org.h2.util.MathUtils.secureRandomBytes
import org.h2.util.StringUtils.convertBytesToHex

class GenerateAdminPasswordCommand : Runnable, CliktCommand("adminpassword") {

    private val helpDoc = resourceOfClassWithExt<GenerateAdminPasswordCommand>("help.md")

    override fun help(context: Context): String = helpDoc.readText()

    private val bits by option(envvar = EnvVar.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE)
        .switch(sizeOptions)
        .default(EnvDefault.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE)
        .help("Number of bits size of generated password")

    override fun run() {
        val password = convertBytesToHex(secureRandomBytes(bits))
        echo("admin password: $password")
    }

    companion object {
        private val sizeOptions = listOf(8, 16, 24, 32).sorted().associateBy { "--${it}bits" }
    }
}