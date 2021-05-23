package actor

import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Receive

import actor.ChildMessage
import actor.MasterMessage
import akka.actor.typed.ActorRef
import searchengine.SearchEngine

class ChildActor(
    context: ActorContext<ChildMessage>,
) : AbstractBehavior<ChildMessage>(context) {
    override fun createReceive(): Receive<ChildMessage> =
        newReceiveBuilder()
            .onMessage(ChildMessage.Request::class.java) {
                it.respondTo.tell(
                    MasterMessage.Response(
                        "${it.engine}",
                        it.engine.search(it.query),
                    )
                )
                this
            }.build()
}

sealed class ChildMessage {
    data class Request(
        val engine: SearchEngine,
        val query: String,
        val respondTo: ActorRef<MasterMessage>,
    ) : ChildMessage()
}
