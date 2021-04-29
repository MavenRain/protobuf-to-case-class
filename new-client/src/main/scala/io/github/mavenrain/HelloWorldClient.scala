import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.hubspot.jackson.datatype.protobuf.ProtobufModule
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import io.grpc.examples.helloworld.GreeterGrpc
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import java.util.concurrent.TimeUnit
import scala.util.Using.{Manager, Releasable}
import scala.util.chaining.scalaUtilChainingOps

case class DomainRequest(name: String, value: Int, alive: Boolean)

object HelloWorldClient extends App {
  private implicit object ReleasableChannel extends Releasable[ManagedChannel] {
    def release(resource: ManagedChannel): Unit = resource.shutdown().awaitTermination(1, TimeUnit.SECONDS)
  }
  private val contractMapper = new ObjectMapper().registerModule(new ProtobufModule)
  private val domainMapper = JsonMapper.builder().addModule(DefaultScalaModule).build()
  private def mapToDomain[S, T](request: S, reference: Class[T]): T =
    request
      .pipe(contractMapper.writeValueAsString(_))
      .pipe(domainMapper.readValue(_, reference))
  Manager { use =>
    val channel = use(ManagedChannelBuilder.forAddress("localhost", 8082).usePlaintext().build())
    val blockingStub = GreeterGrpc.newBlockingStub(channel)
    val request =
      HelloRequest
        .newBuilder()
        .setName("Steve")
        .setValue(42)
        .setAlive(true)
        .build()
    println(mapToDomain(request, classOf[DomainRequest]))
    println("Calling server")
    val response = blockingStub.sayHello(request)
    println("Server called")
    println(s"Response from server: ${response.getMessage}")
  }.getOrElse(())
}