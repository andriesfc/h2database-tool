package h2databasetool.commons.h2

import java.io.File
import java.util.*

class H2BaseDirListing private constructor(
    val baseDir: H2BaseDir,
    val databaseName: String,
    private val list: MutableList<H2FileWithType>,
) : List<H2FileWithType> by list {

    constructor(baseDir: H2BaseDir, databaseName: String) : this(
        baseDir,
        databaseName,
        mutableListOf()
    )

    fun load(): H2BaseDirListing {
        val update =
            H2FileType.entries.mapNotNull { type ->
                baseDir.dir().resolve(type.fileNameOf(databaseName))
                    .takeIf { file -> file.exists() && file.isFile }
                    ?.let { file -> file to type }
            }.toSet()
        list.clear()
        list.addAll(update)
        return this
    }

    operator fun get(desiredType: H2FileType): File =
        getOrNull(desiredType)
            ?: throw NoSuchElementException("No file of type: ${desiredType.name}")

    fun getOrNull(desiredType: H2FileType): File? =
        firstOrNull { (_, type) -> type == desiredType }?.first

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other == null -> false
            other !is H2BaseDirListing -> false
            else -> baseDir == other.baseDir && list == other.list
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(baseDir, databaseName, list)
    }

}
