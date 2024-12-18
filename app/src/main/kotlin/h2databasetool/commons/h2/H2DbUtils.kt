@file:JvmName("H2DbUtils")

package h2databasetool.commons.h2

import h2databasetool.commons.files
import java.io.File

typealias H2FileWithType = Pair<File, H2FileType>
typealias H2FileList = List<H2FileWithType>


fun H2FileType.fileNameOf(databaseName: String): String = when {
    databaseName.endsWith(nameSuffix) -> databaseName
    else -> databaseName + nameSuffix
}

fun File.listDatabaseFiles(databaseName: String): H2FileList = files()
    .mapNotNull { file ->
        H2FileType.of(file)?.let { type -> file to type }
    }
    .filter { (file) ->
        file.name.startsWith(databaseName) && file.name.length > databaseName.length
    }
