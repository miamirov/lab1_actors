package actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

import searchengine.SearchEngine

class MasterActor(
    context: ActorContext<MasterMessage>,
    private val engines: List<SearchEngine>,
    private val timeout: Duration,
    private val futureResult: CompletableFuture<Map<String, String>>,
) : AbstractBehavior<MasterMessage>(context) {
    private val result: MutableMap<String, String> = mutableMapOf()

    override fun createReceive(): Receive<MasterMessage> = newReceiveBuilder()
        .onMessage(MasterMessage.Request::class.java, ::sendRequest)
        .onMessage(MasterMessage.Response::class.java, ::addResponse)
        .onMessage(MasterMessage.Timeout::class.java) { returnResult() }
        .build()

    private fun sendRequest(request: MasterMessage.Request): Behavior<MasterMessage> {
        engines.forEach {
            context.spawn(
                Behaviors.setup(::ChildActor),
                "${context.self.path().name()}:$it:${request.query}:${Random.nextInt()}"
            ).tell(ChildMessage.Request(it, request.query, context.self))
        }
        context.setReceiveTimeout(timeout, MasterMessage.Timeout)

        return this
    }

    private fun addResponse(response: MasterMessage.Response): Behavior<MasterMessage> {
        result[response.name] = response.response
        return if (result.size == engines.size) returnResult()
        else this
    }

    private fun returnResult(): Behavior<MasterMessage> {
        futureResult.complete(result)
        return Behaviors.stopped()
    }

    companion object {
        fun create(
            engines: List<SearchEngine>,
            timeout: Duration,
            futureResult: CompletableFuture<Map<String, String>>
        ): Behavior<MasterMessage> =
            Behaviors.setup { MasterActor(it, engines, timeout, futureResult) }
    }
}

sealed class MasterMessage {
    data class Request(
        val query: String,
    ) : MasterMessage()

    data class Response(
        val name: String,
        val response: String,
    ) : MasterMessage()

    object Timeout : MasterMessage()
}
