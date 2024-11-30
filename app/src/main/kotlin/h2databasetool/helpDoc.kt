package h2databasetool

import com.github.ajalt.clikt.core.Context
import h2databasetool.cmd.ui.Style
import h2databasetool.commons.line
import h2databasetool.env.Env


fun appHelpDoc(context: Context, exe: String = BuildInfo.APP_NAME): String {

    var lastSampleSeq = 0
    fun Env<*>.row() = "| $variable | ${Style.softFocus(default.toString())} | ${description.line()} |"

    fun sample(title: String, script: String, moreDetails: String = "") =
        """ | ${if (lastSampleSeq > 0) '\n' else ""}
            | ${Style.h2("${Style.info1("SAMPLE ${++lastSampleSeq}")} : $title")}
            | 
            |```shell
            |$script
            |```
            |${if (moreDetails.isNotEmpty()) "\n${moreDetails.line()}" else ""}
        """.trimMargin()

    return """ 
            |The $exe Database Tool is a set of common operations to ease the use of the H2 database by developers:
            |
            |1. Start a server to access H2 databases using a remote **(not embedded)** mode.
            |2. Initialize a new database
            |3. Initialize a new database with multiple schemas
            |4. Initialize a new database with multiple schemas with an schema SQL script for each script.
            |
            |${Style.h1("Configuration")}
            |
            |Configuration is done via the following environment variables:
            |
            | | Environment Variable | Default                  | Usage                                 |
            | |----------------------|:------------------------:|---------------------------------------|
            | ${Env.H2TOOL_ADMIN_PASSWORD_BITS.row()}
            | ${Env.H2TOOL_ALWAYS_QUOTE_SCHEMA.row()} 
            | ${Env.H2TOOL_DATA_DIR.row()}
            | ${Env.H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS.row()}
            | ${Env.H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS.row()}
            | ${Env.H2TOOL_SERVER_HOST.row()}
            | ${Env.H2TOOL_SERVER_PERMIT_CREATE_DB.row()}
            | ${Env.H2TOOL_SERVER_PORT.row()}
            | ${Env.H2TOOL_DATABASE_USER.row()}
            | ${Env.H2TOOL_DATABASE_PASSWORD.row()}
            | ${Env.H2TOOL_TRACE_CALLS.row()}
            | ${Env.H2TOOL_SERVER_PASSWORD.row()}
            | ${Env.H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE.row()}
            | 
            |${Style.h1("Samples")}
            |
            |${sample(
            "Start a database server",
            "$exe startDb",
            "Here the tool $exe will use either the defaults, or the values from the host environment."
            )}
            |${sample(
                "Initialize a database with an external script", 
                "$exe initDb scrapedData --init ../prepare-scrape-db.sql",
                "The database will be created based on the value default value of ${Env.H2TOOL_DATA_DIR.variable}."
             )}
             |${sample(
                 "Force initialize a database even if it exists already.",
                 "$exe initDb scrapedData --init ../prepare-scrape-db.sql --force",
                 "${Style.softFocusError("IMPORTANT:")} This will completely destroy the existing database."
             )}
            | 
            """.trimMargin()
}

