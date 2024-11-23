
package h2databasetool.utils

import java.io.File
import java.io.Reader
import java.sql.Connection

private const val END_OF_STATEMENT = ';'
private const val EOF = -1

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
        fun executeScript() {
            if (buffer.isEmpty()) return
            val sql = buffer.toString().also { buffer.clear() }
            @Suppress("SqlSourceToSinkFlow")
            statement.execute(sql)
        }

        while (true) {
            when (state) {

                State.EndOfData -> {
                    executeScript()
                    break
                }

                State.Ready -> {
                    executeScript()
                    state = State.Loading
                }

                State.Loading -> when (val c = source.read()) {
                    EOF -> state = State.EndOfData
                    END_OF_STATEMENT.code -> state = State.Ready
                    else -> buffer.append(c.toChar())
                }
            }
        }
    }
}