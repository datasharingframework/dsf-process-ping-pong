package dev.dsf.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import dev.dsf.bpe.util.FhirSearchQuery;

public class FhirSearchQueryTest
{
	@Test
	public void parsesUrlWithoutQuery()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint");

		assertEquals("/fhir/Endpoint", query.getPath());
		assertEquals(Map.of(), query.getQueryParams());
	}

	@Test
	public void parsesSingleParameter()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint?status=active");

		assertEquals("/fhir/Endpoint", query.getPath());
		assertEquals(Map.of("status", List.of("active")), query.getQueryParams());
	}

	@Test
	public void parsesMultipleParameters()
	{
		FhirSearchQuery query = new FhirSearchQuery(
				"https://example.org/fhir/Endpoint?status=active&connection-type=ihe-iid");

		assertEquals(Map.of("status", List.of("active"), "connection-type", List.of("ihe-iid")),
				query.getQueryParams());
	}

	@Test
	public void collectsRepeatedParameters()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint?status=active&status=requested");

		assertEquals(Map.of("status", List.of("active", "requested")), query.getQueryParams());
	}

	@Test
	public void preservesEqualsInParameterValue()
	{
		FhirSearchQuery query = new FhirSearchQuery(
				"https://example.org/fhir/Endpoint?identifier=http://example.org|a=b");

		assertEquals(Map.of("identifier", List.of("http://example.org|a=b")), query.getQueryParams());
	}

	@Test
	public void preservesUnencodedFhirTokenSeparator()
	{
		FhirSearchQuery query = new FhirSearchQuery(
				"https://example.org/fhir/Endpoint?identifier=http://dsf.dev/sid/endpoint-identifier|123");

		assertEquals(Map.of("identifier", List.of("http://dsf.dev/sid/endpoint-identifier|123")),
				query.getQueryParams());
	}

	@Test
	public void decodesPercentEncodedCharacters()
	{
		FhirSearchQuery query = new FhirSearchQuery(
				"https://example.org/fhir/Endpoint?identifier=http%3A%2F%2Fdsf.dev%2Fsid%2Fendpoint-identifier%7C123");

		assertEquals(Map.of("identifier", List.of("http://dsf.dev/sid/endpoint-identifier|123")),
				query.getQueryParams());
	}

	@Test
	public void treatsPlusAsLiteralPlus()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint?name=C+FHIR");

		assertEquals(Map.of("name", List.of("C+FHIR")), query.getQueryParams());
	}

	@Test
	public void decodesPercentEncodedPlus()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint?name=C%2B%2BFHIR");

		assertEquals(Map.of("name", List.of("C++FHIR")), query.getQueryParams());
	}

	@Test
	public void decodesUtf8()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint?name=J%C3%B6rg");

		assertEquals(Map.of("name", List.of("Jörg")), query.getQueryParams());
	}

	@Test
	public void parsesFhirSearchModifier()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Organization?name:exact=Acme");

		assertEquals(Map.of("name:exact", List.of("Acme")), query.getQueryParams());
	}

	@Test
	public void parsesFhirSearchPrefix()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Observation?date=ge2024-01-01");

		assertEquals(Map.of("date", List.of("ge2024-01-01")), query.getQueryParams());
	}

	@Test
	public void acceptsEmptyParameterValue()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint?identifier=");

		assertEquals(Map.of("identifier", List.of("")), query.getQueryParams());
	}

	@Test
	public void treatsOnlyFirstQuestionMarkAsQueryDelimiter()
	{
		FhirSearchQuery query = new FhirSearchQuery(
				"https://example.org/fhir/Endpoint?_url=https://remote.example/fhir/Endpoint?status=active");

		assertEquals(Map.of("_url", List.of("https://remote.example/fhir/Endpoint?status=active")),
				query.getQueryParams());
	}

	@Test
	public void decodesAmpersandInsideParameterValue()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint?name=foo%26bar&status=active");

		assertEquals(Map.of("name", List.of("foo&bar"), "status", List.of("active")), query.getQueryParams());
	}

	@Test
	public void preservesOriginalUrlInToString()
	{
		String url = "https://example.org/fhir/Endpoint?identifier=http://foo|123";

		FhirSearchQuery query = new FhirSearchQuery(url);

		assertEquals(url, query.toString());
	}

	@Test
	public void rejectsNullUrl()
	{
		assertThrows(NullPointerException.class, () -> new FhirSearchQuery(null));
	}

	@Test
	public void rejectsBlankUrl()
	{
		assertThrows(IllegalArgumentException.class, () -> new FhirSearchQuery("   "));
	}

	@Test
	public void rejectsEmptyQuery()
	{
		assertThrows(IllegalArgumentException.class, () -> new FhirSearchQuery("https://example.org/fhir/Endpoint?"));
	}

	@Test
	public void rejectsParameterWithoutEquals()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?status"));
	}

	@Test
	public void rejectsEmptyParameterBetweenParameters()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?status=active&&identifier=123"));
	}

	@Test
	public void rejectsEmptyParameterAtEnd()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?status=active&"));
	}

	@Test
	public void rejectsBlankParameterName()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?=active"));
	}

	@Test
	public void rejectsInvalidPercentEncoding()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?name=%ZZ"));
	}

	@Test
	public void rejectsIncompletePercentEncoding()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?name=%2"));
	}

	@Test
	public void rejectsIncompletePercentEncodingAtEnd()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?name=foo%"));
	}

	@Test
	public void rejectsInvalidUtf8()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?name=%FF"));
	}

	@Test
	public void rejectsMalformedUtf8Sequence()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new FhirSearchQuery("https://example.org/fhir/Endpoint?name=%C3%28"));
	}

	@Test
	public void returnsUnmodifiableQueryParameters()
	{
		FhirSearchQuery query = new FhirSearchQuery("https://example.org/fhir/Endpoint?status=active");

		assertThrows(UnsupportedOperationException.class, () -> query.getQueryParams().put("foo", List.of("bar")));

		assertThrows(UnsupportedOperationException.class, () -> query.getQueryParams().get("status").add("inactive"));
	}
}