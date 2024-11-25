package h2databasetool.utils

import java.io.File

fun File.containsDb(dbName: String): Boolean =
    resolve("$dbName.mv.db").run { exists() && isFile }