import io.grpc.ServerBuilder
import io.grpc.examples.helloworld.GreeterGrpc
import io.grpc.examples.helloworld.HelloReply
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.stub.StreamObserver

class GreeterImpl : GreeterGrpc.GreeterImplBase() {
  override fun sayHello(
    request: HelloRequest?,
    responseObserver: StreamObserver<HelloReply>?
  ) =
    HelloReply
      .newBuilder()
      .setMessage("Hello ${request?.name}")
      .setValue(request?.value?.let { it * 2 } ?: 0)
      .setAlive(request?.alive ?: false)
      .build()
      .let { responseObserver?.onNext(it) }
      .let { responseObserver?.onCompleted() }
      .let { Unit }
}

fun main(args: Array<String>) =
  ServerBuilder
    .forPort(8082)
    .addService(GreeterImpl())
    .build()
    .also { it.start() }
    .also { println("Server started with args: $args") }
    .also { Runtime.getRuntime().addShutdownHook(Thread() { println("Ups, JVM shutdown") }) }
    .also { it.awaitTermination() }
    .also { println("Server stopped") }
