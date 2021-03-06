package io.github.mavenrain

import com.google.protobuf.GeneratedMessageV3
import com.fasterxml.jackson.databind.DeserializationFeature.{
  FAIL_ON_INVALID_SUBTYPE, FAIL_ON_UNKNOWN_PROPERTIES,
  FAIL_ON_UNRESOLVED_OBJECT_IDS, READ_UNKNOWN_ENUM_VALUES_AS_NULL
}
import com.fasterxml.jackson.databind.SerializationFeature.{
  CLOSE_CLOSEABLE, FAIL_ON_EMPTY_BEANS, FAIL_ON_SELF_REFERENCES,
  FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, WRITE_DATES_AS_TIMESTAMPS,
  WRITE_DURATIONS_AS_TIMESTAMPS
}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.hubspot.jackson.datatype.protobuf.ProtobufModule
import scala.util.chaining.scalaUtilChainingOps
import shapeless.Generic

object Mappers {
  // Q: Why don't the return types of mapToDomain and mapToContract have to be
  // managed by a failure-like effect (Try, Either, ZIO, Coproduct, etc.)?
  // A: Because of a combination of how the object mappers are configured and
  // what the type constraints on the input parameters are, one can nearly guarantee
  // infallability.  In fact, the only reason why that assertion cannot be exhaustively
  // unit tested is because of the size of the GeneratedMessageV3 type.  For any
  // finite HList where each element of the HList is a child of GeneratedMessageV3,
  // this assertion can be unit tested exhaustively with a property-based testing framework.
  def mapToDomain[S <: GeneratedMessageV3, T](request: S, reference: Class[T])(implicit evidence: Generic[T]): T =
    request
      .pipe(contractMapper.writeValueAsString(_))
      .pipe(domainMapper.readValue(_, reference))
  def mapToContract[S, T <: GeneratedMessageV3](request: S, reference: Class[T])(implicit evidence: Generic[S]): T =
    request
      .pipe(domainMapper.writeValueAsString(_))
      .pipe(contractMapper.readValue(_, reference))
  private val contractMapper =
    new ObjectMapper()
      .registerModule(new ProtobufModule)
      .configure(CLOSE_CLOSEABLE, true)
      .configure(FAIL_ON_EMPTY_BEANS, false)
      .configure(FAIL_ON_INVALID_SUBTYPE, false)
      .configure(FAIL_ON_SELF_REFERENCES, false)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(FAIL_ON_UNRESOLVED_OBJECT_IDS, false)
      .configure(FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false)
      .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
      .configure(WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(WRITE_DURATIONS_AS_TIMESTAMPS, false)
  private val domainMapper =
    JsonMapper
      .builder()
      .addModule(DefaultScalaModule)
      .build()
      .configure(CLOSE_CLOSEABLE, true)
      .configure(FAIL_ON_EMPTY_BEANS, false)
      .configure(FAIL_ON_INVALID_SUBTYPE, false)
      .configure(FAIL_ON_SELF_REFERENCES, false)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(FAIL_ON_UNRESOLVED_OBJECT_IDS, false)
      .configure(FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false)
      .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
      .configure(WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(WRITE_DURATIONS_AS_TIMESTAMPS, false)
}