package buildlogic

import org.gradle.api.Project
import org.semver4j.Semver
import java.io.File
import java.io.IOException
import kotlin.io.path.name

fun File.ensureDirs(): File {

    if (!exists() && !mkdirs())
        throw IOException("Unable to create directory tree: $absolutePath")

    if (!isDirectory)
        throw IOException("Expected regular directory here: $absolutePath")

    return this
}

fun File.ensureParents(): File {
    parentFile.ensureDirs()
    return this
}

fun File.join(vararg path: String): File {
    return File(this, path.joinToString(File.separatorChar.toString()))
}

inline fun File?.files(predicate: (File) -> Boolean = { true }): List<File> {

    if (this == null)
        return emptyList()

    if (!exists())
        return emptyList()

    if (!isDirectory)
        return if (predicate(this)) listOf(this) else emptyList()

    return listFiles()?.toList()?.filter(predicate) ?: emptyList()
}


/** Takes the root project's version, and applies it to each child. */
fun Project.propagateVersioning() {
    fun propagateVersion(project: Project, version: Semver) {
        project.version = version
        project.subprojects.forEach { propagateVersion(it, version) }
    }
    propagateVersion(rootProject, Semver.parse(rootProject.version.toString()) ?: return)
}

fun Project.internalizePath(file: File): String {

    val projectRoot = layout.projectDirectory.asFile.toPath()
    val filePath = file.absoluteFile.toPath()
    val internalPath = when {
        filePath.startsWith(projectRoot) -> filePath.subpath(projectRoot.nameCount, filePath.nameCount)
        else -> filePath
    }.toFile().path

    return projectRoot.name + File.separator + internalPath
}
