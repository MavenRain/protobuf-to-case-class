import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import io.grpc.examples.helloworld.GreeterGrpc
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import java.util.concurrent.TimeUnit
import scala.util.Using.{Manager, Releasable}

object HelloWorldClient extends App {
  private implicit object ReleasableChannel extends Releasable[ManagedChannel] {
    def release(resource: ManagedChannel): Unit = resource.shutdown().awaitTermination(5, TimeUnit.SECONDS)
  }
  Manager { use =>
    val channel = use(ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build())
    val blockingStub = GreeterGrpc.newBlockingStub(channel)
    val request =
      HelloRequest
        .newBuilder()
        .setName("Steve")
        .build()
    println("Calling server")
    val response = blockingStub.sayHello(request)
    println("Server called")
    println("Response from server: ${response.message}")
  }.getOrElse(())
}