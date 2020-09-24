package ch.epfl.bluebrain.nexus.delta.rdf

import ch.epfl.bluebrain.nexus.delta.rdf.Vocabulary.{owl, schema, xsd}
import org.scalatest.Inspectors
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import ch.epfl.bluebrain.nexus.delta.rdf.implicits._
import ch.epfl.bluebrain.nexus.testkit.EitherValuable
import io.circe.Json
import io.circe.syntax._
import org.apache.jena.iri.IRI

class IriSpec extends AnyWordSpecLike with Matchers with Inspectors with EitherValuable {

  "An Iri" should {
    val iriString = "http://example.com/a"
    val iri       = iri"$iriString"

    "be a prefix mapping" in {
      forAll(List(schema.base, xsd.base, owl.base)) { iri =>
        iri.isPrefixMapping shouldEqual true
      }
    }

    "not be a prefix mapping" in {
      forAll(List(schema.Person, xsd.int, owl.oneOf)) { iri =>
        iri.isPrefixMapping shouldEqual false
      }
    }

    "strip prefix" in {
      schema.Person.stripPrefix(schema.base) shouldEqual "Person"
      xsd.integer.stripPrefix(xsd.base) shouldEqual "integer"
    }

    "append segment" in {
      val list     = List(
        iri"http://example.com/a"  -> "b",
        iri"http://example.com/a/" -> "/b",
        iri"http://example.com/a/" -> "b",
        iri"http://example.com/a"  -> "/b"
      )
      val expected = iri"http://example.com/a/b"
      forAll(list) { case (iri, segment) => (iri / segment) shouldEqual expected }
    }
    "be converted to Json" in {
      iri.asJson shouldEqual Json.fromString(iriString)
    }

    "be constructed from Json" in {
      Json.fromString(iriString).as[IRI].rightValue shouldEqual iri
    }
  }

}
