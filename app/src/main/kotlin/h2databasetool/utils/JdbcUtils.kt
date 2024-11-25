package h2databasetool.utils

import java.io.File
import java.io.Reader
import java.sql.Connection

private const val endOfStatement = ';'
private const val eof = -1

private enum class State {
    Loading,
    Ready,
    EndOfData
}

fun Connection.executeScript(script: String) = executeScript(script.reader())
fun Connection.executeScript(file: File) = executeScript(file.reader())

fun Connection.executeScript(source: Reader) {
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

                State.Loading -> when (val c = source.read()) {
                    eof -> state = State.EndOfData
                    endOfStatement.code -> state = State.Ready
                    else -> buffer.append(c.toChar())
                }
            }
        }
    }
}