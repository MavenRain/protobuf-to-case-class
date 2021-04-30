package io.github.mavenrain

import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import io.grpc.examples.helloworld.{GreeterGrpc, HelloRequest}
import io.grpc.examples.helloworld.RequestedFeature.REQUESTED_FEATURE_ONE
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import java.util.concurrent.TimeUnit
import scala.util.Using.{Manager, Releasable}

case class DomainRequest(name: String, value: Int, alive: Boolean, requestedFeature: String)

object HelloWorldClient extends App {
  private implicit object ReleasableChannel extends Releasable[ManagedChannel] {
    def release(resource: ManagedChannel): Unit = resource.shutdown().awaitTermination(1, TimeUnit.SECONDS)
  }
  Manager { use =>
    val channel = use(ManagedChannelBuilder.forAddress("localhost", 8082).usePlaintext().build())
    val blockingStub = GreeterGrpc.newBlockingStub(channel)
    val contractRequest =
      HelloRequest
        .newBuilder()
        .setName("Steve")
        .setValue(42)
        .setAlive(true)
        .setRequestedFeature(REQUESTED_FEATURE_ONE)
        .build()
    val domainRequest = DomainRequest(name = "Wally", value = 24, alive = false, requestedFeature = "REQUESTED_FEATURE_ONE")
    val contractRequestFromDomain = Mappers.mapToContract(domainRequest, classOf[HelloRequest])
    println(s"From converted contract request:")
    println(s"Name = ${contractRequestFromDomain.getName()}")
    println(s"Value = ${contractRequestFromDomain.getValue()}")
    println(s"Alive = ${contractRequestFromDomain.getAlive()}")
    println(s"Requested Feature = ${contractRequestFromDomain.getRequestedFeature()}")
    println(Mappers.mapToDomain(contractRequest, classOf[DomainRequest]))
    println("Calling server")
    val response = blockingStub.sayHello(contractRequest)
    println("Server called")
    println(s"Response from server: ${response.getMessage}")
  }.getOrElse(())
}