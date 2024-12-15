package h2databasetool.commons.h2

import h2databasetool.commons.files
import h2databasetool.commons.randomItem
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.IOException

class H2FileTypeTest : FunSpec({

    val h2BaseDir = tempdir("H2FileTypeTest", "h2BaseDir")
    val db1Name = "testDb1"
    val db2Name = "testDb2"
    lateinit var db1FilesAndType: List<Pair<File, H2FileType>>
    lateinit var db2FilesAndType: List<Pair<File, H2FileType>>

    beforeSpec {

        val populateBaseDirDataFiles = { databaseName: String ->
            H2FileType.entries
                .map { type -> h2BaseDir.resolve("$databaseName${type.nameSuffix}") to type }
                .onEach { (file) ->
                    if (!file.createNewFile())
                        throw IOException("Failed to create $file")
                }
        }

        db1FilesAndType = populateBaseDirDataFiles(db1Name)
        db2FilesAndType = populateBaseDirDataFiles(db2Name)

    }

    context("H2FileType.of() resolve all file known types") {
        db1FilesAndType.forEach { (file, expectedType) ->
            test("${file.name} resolves to $expectedType") {
                val actualType = H2FileType.of(file).shouldNotBeNull()
                actualType shouldBe expectedType
            }
        }
    }

    test("distinguish between files from different databases") {
        val (dbFile, dbFileType) = db1FilesAndType.randomItem()
        val databaseName = dbFile.name.removeSuffix(dbFileType.nameSuffix)
        databaseName shouldBe db1Name
    }

    test("scanning baseDir for database files") {

        val db1Files = h2BaseDir
            .files { file -> H2FileType.of(file) != null && file.name.startsWith(db1Name) }
            .toSet()

        val expectedDb1Files = db1FilesAndType.map { (file) -> file }.toSet()
        db1Files shouldBe expectedDb1Files
    }

})
