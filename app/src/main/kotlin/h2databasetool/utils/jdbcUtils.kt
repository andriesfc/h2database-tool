@file:Suppress("SqlSourceToSinkFlow")

package h2databasetool.utils

import java.io.File
import java.io.Reader
import java.io.StringReader
import javax.sql.DataSource

private const val SCRIPT_STATEMENT_DELIMITER = ';'
private const val EOF = -1

private enum class ScriptProcessState {
    READING,
    READY,
    END_OF_SCRIPT
}

fun DataSource.executeScript(
    script: Reader,
    autoClose: Boolean = true,
    statementDelimiter: Char = SCRIPT_STATEMENT_DELIMITER,
) {
    try {
        connection.using {
            val statement = createStatement()
            var state = ScriptProcessState.READING
            val ready = StringBuilder()

            fun processReadyStatement() {
                if (ready.isEmpty()) return
                val sql = ready.toString()
                statement.execute(sql)
                ready.clear()
            }

            script.using {
                do {
                    when (state) {

                        ScriptProcessState.READING -> do {
                            when (val c = read()) {
                                EOF -> state = ScriptProcessState.END_OF_SCRIPT
                                statementDelimiter.code -> state = ScriptProcessState.READY
                                else -> ready.append(c)
                            }
                        } while (state == ScriptProcessState.READING)

                        ScriptProcessState.READY -> {
                            processReadyStatement()
                            state = ScriptProcessState.READING
                        }

                        ScriptProcessState.END_OF_SCRIPT ->
                            processReadyStatement()
                    }
                } while (state != ScriptProcessState.END_OF_SCRIPT)
            }
        }
    } finally {
        if (autoClose) script.closeSilent()
    }
}

fun DataSource.executeScriptText(
    scriptText: String,
    statementDelimiter: Char = SCRIPT_STATEMENT_DELIMITER
) = executeScript(StringReader(scriptText), statementDelimiter = statementDelimiter)

fun DataSource.executeScriptFile(
    scriptFile: File,
    statementDelimiter: Char = SCRIPT_STATEMENT_DELIMITER
) = executeScript(scriptFile.reader(), statementDelimiter = statementDelimiter)