package io.github.mavenrain

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
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
  private val contractMapper = new ObjectMapper().registerModule(new ProtobufModule).configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
  private val domainMapper = JsonMapper.builder().addModule(DefaultScalaModule).build().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
  private def mapToDomain[S, T](request: S, reference: Class[T]): T =
    request
      .pipe(contractMapper.writeValueAsString(_))
      .pipe(domainMapper.readValue(_, reference))
  private def mapToContract[S, T](request: S, reference: Class[T]): T =
    request
      .pipe(domainMapper.writeValueAsString(_))
      .pipe(contractMapper.readValue(_, reference))
  Manager { use =>
    val channel = use(ManagedChannelBuilder.forAddress("localhost", 8082).usePlaintext().build())
    val blockingStub = GreeterGrpc.newBlockingStub(channel)
    val contractRequest =
      HelloRequest
        .newBuilder()
        .setName("Steve")
        .setValue(42)
        .setAlive(true)
        .build()
    val domainRequest = DomainRequest(name = "Wally", value = 24, alive = false)
    val contractRequestFromDomain = mapToContract(domainRequest, classOf[HelloRequest])
    println(s"From converted contract request:")
    println(s"Name = ${contractRequestFromDomain.getName()}")
    println(s"Value = ${contractRequestFromDomain.getValue()}")
    println(s"Alive = ${contractRequestFromDomain.getAlive()}")
    println(mapToDomain(contractRequest, classOf[DomainRequest]))
    println("Calling server")
    val response = blockingStub.sayHello(contractRequest)
    println("Server called")
    println(s"Response from server: ${response.getMessage}")
  }.getOrElse(())
}