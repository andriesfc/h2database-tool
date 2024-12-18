package h2databasetool.commons.h2

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.engine.spec.tempdir
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe

class H2BaseDirListingTest : ExpectSpec({

    context("H2 base dir with testDb1 and testDb2") {
        val baseDir = tempdir().toH2BaseDir()
        val testDb1 = "testDb1"
        val testDb2 = "testDb2"
        val (
            database1Files,
            database2Files,
        ) = listOf(testDb1, testDb2)
            .onEach { databaseName ->
                H2FileType.entries.forEach { type ->
                    val fileName = "$databaseName${type.nameSuffix}"
                    baseDir.dir().resolve(fileName).createNewFile()
                }
            }.map { databaseName -> H2BaseDirListing(baseDir, databaseName).load() }

        expect("database 1 to contain only files for testDb1") {
            database1Files
                .map { (file, _) -> file.name }
                .forAll { fileName -> fileName.substringBefore('.') shouldBe testDb1 }

        }

        expect("database 2 to contain only files for testDb2") {
            database2Files
                .map { (file, _) -> file.name }
                .forAll { fileName -> fileName.substringBefore('.') shouldBe testDb2 }
        }


        expect("empty database1 dir of all files") {
            database1Files.shouldNotBeEmpty()
            database1Files.onEach { (file, _) -> file.delete() }
            database1Files.load()
            database1Files.shouldBeEmpty()
        }

    }

})

