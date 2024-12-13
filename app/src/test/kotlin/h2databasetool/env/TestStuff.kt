package h2databasetool.env

import io.kotest.core.spec.style.FunSpec
import java.util.*

class TestStuff : FunSpec({
    test("OS name") {
        val os = System.getProperty("os.name").lowercase(Locale.ENGLISH).replace(" ", "")
        val osVersion = System.getProperty("os.version").lowercase(Locale.ENGLISH).replace(" ", "")
        val osArchitecture = System.getProperty("os.arch").lowercase(Locale.ENGLISH).replace(" ", "")
        println("        OS name: $os")
        println("     OS version: $osVersion")
        println("OS architecture: $osArchitecture")
    }
})
