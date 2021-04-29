import com.fasterxml.jackson.databind.ObjectMapper
import com.hubspot.jackson.datatype.protobuf.ProtobufModule
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import io.grpc.examples.helloworld.GreeterGrpc
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import java.util.concurrent.TimeUnit
import scala.util.Using.{Manager, Releasable}


object HelloWorldClient extends App {
  private implicit object ReleasableChannel extends Releasable[ManagedChannel] {
    def release(resource: ManagedChannel): Unit = resource.shutdown().awaitTermination(1, TimeUnit.SECONDS)
  }
  private val mapper = new ObjectMapper().registerModule(new ProtobufModule)
  Manager { use =>
    val channel = use(ManagedChannelBuilder.forAddress("localhost", 8081).usePlaintext().build())
    val blockingStub = GreeterGrpc.newBlockingStub(channel)
    val request =
      HelloRequest
        .newBuilder()
        .setName("Steve")
        .setValue(42)
        .setAlive(true)
        .build()
    println(mapper.writeValueAsString(request))
    println("Calling server")
    val response = blockingStub.sayHello(request)
    println("Server called")
    println(s"Response from server: ${response.getMessage}")
  }.getOrElse(())
}