package h2databasetool.utils

import java.io.EOFException
import java.io.IOException

inline fun <RESOURCE : AutoCloseable, T> using(resource: RESOURCE, block: RESOURCE.() -> T): T = resource.use(block)

inline fun <T : AutoCloseable> T.closeSilent(closingFailed: (T, IOException) -> Unit = { _, _ -> }): Boolean {
    return try {
        close()
        true
    } catch (e: EOFException) {
        true
    } catch (e: IOException) {
        closingFailed(this, e)
        false
    }
}

