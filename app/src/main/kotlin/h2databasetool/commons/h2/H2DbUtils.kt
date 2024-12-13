@file:JvmName("H2DbUtils")

package h2databasetool.commons.h2

import h2databasetool.commons.files
import java.io.File
import org.h2.store.fs.FileUtils as h2FileOps

typealias H2FileWithType = Pair<File, H2FileType>
typealias H2FileList = List<H2FileWithType>

fun File.tryDelete() = isFile && h2FileOps.tryDelete(path)

fun H2FileType.Companion.of(file: File): H2FileType? = H2FileType.entries
    .firstOrNull { type -> file.name.endsWith(type.nameSuffix, ignoreCase = true) }

fun File.listDatabaseFiles(databaseName: String): H2FileList = files()
    .mapNotNull { file ->
        H2FileType.of(file)?.let { type -> file to type }
    }
    .filter { (file) ->
        file.name.startsWith(databaseName) && file.name.length > databaseName.length
    }

