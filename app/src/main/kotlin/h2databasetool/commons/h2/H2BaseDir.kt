package h2databasetool.commons.h2

import h2databasetool.commons.ensureDir
import h2databasetool.commons.file
import h2databasetool.commons.files
import java.io.File

@JvmInline
value class H2BaseDir private constructor(private val dir: File) {

    val path: String get() = dir.path
    fun exists(): Boolean = dir.exists()
    fun existsAsDir(): Boolean = dir.exists() && dir.isDirectory
    fun dir(): File = dir
    fun name(): String = dir.name

    fun required(): H2BaseDir =
        validated().also { dir.ensureDir { required -> "Unable create required H2 base directory: $required" } }

    fun validated(): H2BaseDir {

        if (exists()) {
            require(existsAsDir()) { "Current base directory path is not a regular directory: $path" }
        }

        return this
    }

    fun dbFiles(databaseName: String) =
        dir.files { file -> file.name.startsWith(databaseName) && H2FileType.of(file) != null }

    fun dbFilesWithType(databaseName: String) =
        dir.files().mapNotNull { file -> H2FileType.of(file)?.let { type -> file to type } }

    fun dbFilesWithType() =
        dir.files().mapNotNull { file -> H2FileType.of(file)?.let { type -> file to type } }
            .groupBy { (file) -> file.name.substringBefore('.') }

    fun dbFiles() =
        dir.files { file -> H2FileType.of(file) != null }
            .groupBy { file -> file.name.substringBefore('.') }

    fun dbNames() = dbFiles().keys

    companion object {
        @JvmStatic
        fun of(
            directory: String,
            eagerValidate: Boolean = false,
            autoCreate: Boolean = false,
            canonical: Boolean = true,
            absolute: Boolean = true,
        ): H2BaseDir {
            return H2BaseDir(directory.file(canonical = canonical, absolute = absolute))
                .let { h2BaseDir -> if (eagerValidate) h2BaseDir.validated() else h2BaseDir }
                .let { h2BaseDir -> if (autoCreate) h2BaseDir.required() else h2BaseDir }
        }
    }
}

fun File.toH2BaseDir(): H2BaseDir =
    path.toH2BaseDir()

fun String.toH2BaseDir(): H2BaseDir =
    H2BaseDir.of(this, autoCreate = true, eagerValidate = true, canonical = true)

