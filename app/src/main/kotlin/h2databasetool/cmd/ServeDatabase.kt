package h2databasetool.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

class ServeDatabase : CliktCommand("serve") {

    override fun help(context: Context): String {
        return "Serves a local database on network port."
    }

    override fun run() {
        TODO("Not yet implemented")
    }
}