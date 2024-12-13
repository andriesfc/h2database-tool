package h2databasetool.commons

import java.io.File
import java.io.IOException
import java.io.Reader
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.sql.Connection
import java.sql.SQLException
import kotlin.Throws

private const val endOfStatement = ';'
private const val eof = -1
private const val NEW_LINE = '\n'

private enum class State {
    Loading,
    Ready,
    EndOfData
}

class ScriptExecutionError internal constructor(
    val script: Any,
    val lineNo: Int,
    cause: Exception,
) : Exception(
    /* message = */ cause.message(script.script(), lineNo),
    /* cause = */ cause,
    /* enableSuppression = */ cause.isSuppressed(),
    /* writableStackTrace = */ cause.writeStacktrace()
) {
    companion object {
        private const val NO_MESSAGE_SUPPLIED_MESSAGE = "(No message supplied)"
        private fun Exception.message(script: String, lineNo: Int): String {
            return when (this) {
                is SQLException -> """
                |This was caused by an SQL error: ${message ?: NO_MESSAGE_SUPPLIED_MESSAGE}. (For more
                | information please consult the H2 documentation for the following error code: $errorCode)
                """.stripLineFeedsToMargin()

                is IOException -> "IO Error - ${message ?: NO_MESSAGE_SUPPLIED_MESSAGE} "
                else -> message ?: NO_MESSAGE_SUPPLIED_MESSAGE
            }
        }

        private fun Exception.isSuppressed(): Boolean = !(this is SQLException || this is IOException)
        private fun Exception.writeStacktrace(): Boolean = !(this is SQLException || this is IOException)
        private fun Any.script(): String {
            return when (this) {
                is BuildStatementSummary -> build()
                is File -> path
                is URL -> toString()
                is URI -> toString()
                is String -> this
                is Appendable -> toString()
                is Path -> toFile().path
                else -> toString()
            }
        }
    }

}

@Throws(ScriptExecutionError::class)
fun Connection.executeScript(sql: String, source: Any? = null) =
    executeScript(sql.reader(), source ?: sql.asStatementSummaryBuilder())

@Throws(ScriptExecutionError::class)
fun Connection.executeScript(file: File) = executeScript(file.reader(), file)

@Throws(ScriptExecutionError::class)
fun Connection.executeScript(reader: Reader, script: Any = "snippet") {
    var lineNo = 0
    try {
        createStatement().use { statement ->

            val buffer = StringBuilder()
            var state = State.Loading

            fun executeStatement() {
                if (buffer.isEmpty()) return
                val sql = buffer.toString()
                statement.execute(sql)
                buffer.clear()
            }

            while (true) {
                when (state) {

                    State.EndOfData -> {
                        executeStatement()
                        break
                    }

                    State.Ready -> {
                        executeStatement()
                        state = State.Loading
                    }

                    State.Loading -> when (val code = reader.read()) {
                        eof -> state = State.EndOfData
                        endOfStatement.code -> state = State.Ready
                        NEW_LINE.code -> {
                            ++lineNo
                            buffer.append(code.toChar())
                        }

                        else -> buffer.append(code.toChar())
                    }
                }
            }
        }
    } catch (e: SQLException) {
        e.raiseByScript(script, lineNo)
    } catch (e: IOException) {
        e.raiseByScript(script, lineNo)
    }
}

private fun Exception.raiseByScript(script: Any, lineNo: Int): Nothing =
    throw ScriptExecutionError(
        script = (script as? BuildStatementSummary)?.build()
            ?: (script as? String)
            ?: (script as? File)?.path
            ?: script.toString(),
        lineNo = lineNo,
        cause = this
    )

private const val ELLIPSIS_CHAR = '\u2026'
private const val SQL_STATEMENT_MAX_SIZE = 200
private const val SQL_STATEMENT_PREFIX = "Statement: "
private const val SQL_STATEMENT_SHORTENED_PREFIX = "Statement (shortened): "

private fun interface BuildStatementSummary {
    fun build(): String
}

private fun String.asStatementSummaryBuilder() = BuildStatementSummary {
    val shortenIt = length > SQL_STATEMENT_MAX_SIZE
    val statementPrefix = if (shortenIt) SQL_STATEMENT_SHORTENED_PREFIX else SQL_STATEMENT_PREFIX
    val statement = if (shortenIt) substring(0, SQL_STATEMENT_MAX_SIZE) + ELLIPSIS_CHAR else this
    buildString(statementPrefix.length + statement.length) {
        append(statementPrefix)
        append(statement)
    }
}


