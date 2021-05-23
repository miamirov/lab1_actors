package searcher

import akka.actor.typed.ActorSystem

import java.time.Duration
import java.util.concurrent.CompletableFuture

import searchengine.SearchEngine
import actor.MasterActor
import actor.MasterMessage
import kotlin.random.Random

class Searcher(private val engines: List<SearchEngine>) {
    fun search(query: String, timeout: Duration): Map<String, String> {
        val futureResult: CompletableFuture<Map<String, String>> = CompletableFuture()
        val system: ActorSystem<MasterMessage> = ActorSystem.create(
            MasterActor.create(engines, timeout, futureResult),
            "SearchSystem-${Random.nextInt()}"
        )
        system.tell(MasterMessage.Request(query))
        val result = futureResult.get()
        system.terminate()
        return result
    }
}
