package h2databasetool.commons

import java.io.File
import java.io.Reader
import java.sql.Connection
import java.sql.SQLException
import kotlin.jvm.Throws

private const val endOfStatement = ';'
private const val eof = -1
private const val NEW_LINE = '\n'

private enum class State {
    Loading,
    Ready,
    EndOfData
}

class ScriptExecutionError internal constructor(
    val script: String,
    val lineNo: Int,
    cause: SQLException
) : Exception(buildString {
    append("Execution of script ", script, " failed at line number ", lineNo, ". ")
    append("This was caused by a sql error #", cause.errorCode)
    when (cause.message) {
        null -> append(" (No specific detail message was supplied)")
        else -> append("with message: ", cause.message)
    }
    append('.')
}, cause)

@Throws(ScriptExecutionError::class)
fun Connection.executeScript(sql: String, source: Any? = null) = executeScript(sql.reader(), source ?: summaryBuilderOf(sql))

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
        throw ScriptExecutionError(
            script = (script as? BuildStatementSummary)?.summarizeStatement()
                ?: (script as? String?)
                ?: (script as? File)?.path
                ?: script.toString(),
            lineNo = lineNo,
            cause = e
        )
    }
}


private const val ELLIPSIS_CHAR = '\u2026'
private const val SQL_STATEMENT_MAX_SIZE = 200
private const val SQL_STATEMENT_PREFIX = "Statement: "
private const val SQL_STATEMENT_SHORTENED_PREFIX = "Statement (shortened): "

private fun interface BuildStatementSummary {
    fun summarizeStatement(): String
}

private fun summaryBuilderOf(script: String) = BuildStatementSummary {
    val shortenIt = script.length > SQL_STATEMENT_MAX_SIZE
    val statementPrefix = if (shortenIt) SQL_STATEMENT_SHORTENED_PREFIX else SQL_STATEMENT_PREFIX
    val statement = if (shortenIt) script.substring(0, SQL_STATEMENT_MAX_SIZE) + ELLIPSIS_CHAR else script
    buildString(statementPrefix.length + statement.length) {
        append(statementPrefix)
        append(statement)
    }
}


