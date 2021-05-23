package searchengine

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class SearchEngine(
    val name: String,
    val host: String,
    val port: Int,
) {
    override fun toString(): String = name
    fun search(query: String): String = HttpClient
        .newBuilder()
        .build()
        .send(
            HttpRequest.newBuilder().uri(URI.create("http://$host:$port/search?q=$query")).build(),
            HttpResponse.BodyHandlers.ofString()
        )
        .body()
}
