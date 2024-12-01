package h2databasetool.env

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty

class EnvTest : ExpectSpec({
    expect("Env.entries() to return all available entries") {

        val allEntries = Env::class.nestedClasses
            .map { it.objectInstance }
            .filterIsInstance<Env<Any>>().toSet().also { it.shouldNotBeEmpty() }

        val missed = allEntries - Env.entries()

        missed.shouldBeEmpty()
    }
})

