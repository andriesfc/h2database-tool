package h2databasetool.commons

import java.io.File
import java.io.IOException

private const val USER_HOME_PREFIX = "~/"

fun userHome(): File = File(System.getProperty("user.home"))

fun String.file(canonical: Boolean = true, absolute: Boolean = false): File =
    when {
        startsWith(USER_HOME_PREFIX) -> File(userHome(), substringAfter(USER_HOME_PREFIX))
        else -> File(this)
    }.let { file ->
        file.takeIf { canonical }?.canonicalFile ?: file
    }.let { file ->
        file.takeIf { absolute }?.absoluteFile ?: file
    }

operator fun File.component1(): File = parentFile
operator fun File.component2(): String = nameWithoutExtension
operator fun File.component3(): String? = extension.takeUnless(String::isEmpty)

inline fun File.files(crossinline predicate: (File) -> Boolean): List<File> =
    { file: File -> file.isFile && predicate(file) }
        .let { andFileOnly -> listFiles(andFileOnly)?.toList() ?: emptyList() }

fun File.files(): List<File> = files({ true })

inline fun File.directories(crossinline predicate: (File) -> Boolean): List<File> =
    { file: File -> file.isDirectory && predicate(file) }
        .let { andDirectoryOnly -> listFiles(andDirectoryOnly)?.toList() ?: emptyList() }

fun File.directories(): List<File> = directories { true }

fun File.ensureDir(): File {
    if (!exists() && !mkdirs())
        throw IOException("Unable to create directory: $this")
    return this
}
