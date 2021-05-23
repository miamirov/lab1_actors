package searcher

import org.junit.Test
import searchengine.SearchEngine
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearcherTest {
    @Test
    fun testOne() {
        val query = "test"


        SearcherStubServer(yandex, Duration.ofSeconds(0)).use { server ->
            val searcher = Searcher(listOf(yandex))
            val actual = searcher.search(query, Duration.ofSeconds(1))

            assertEquals(1, actual.size)
            assertTrue(actual.contains(yandex.name))
            assertEquals(server.genResponse(query), actual[yandex.name])
        }
    }


    @Test
    fun testMultiple() {
        val query = "test"

        SearcherStubServer(yandex, Duration.ofSeconds(0)).use {
            SearcherStubServer(google, Duration.ofSeconds(3)).use {
                val searcher = Searcher(listOf(yandex, google))
                val actual = searcher.search(query, Duration.ofSeconds(1))
                assertEquals(1, actual.size)
                assertTrue(actual.contains(yandex.name))
                assertFalse(actual.contains(google.name))
            }
        }

    }

    companion object {
        private val yandex = SearchEngine("yandex", "localhost", 8888)
        private val google = SearchEngine("google", "localhost", 8889)
    }
}
