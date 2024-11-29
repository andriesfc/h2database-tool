package h2databasetool.commons

import java.io.File

private const val USER_HOME_PREFIX = "~/"

fun userHome(): File = File(System.getProperty("user.home"))
fun cwd():File = File(System.getProperty("user.dir"))

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

fun File.files(predicate: (File) -> Boolean) = listFiles(predicate)?.toList() ?: emptyList()

fun File.canonical(): File = canonicalFile
fun File.absolute(): File = absoluteFile