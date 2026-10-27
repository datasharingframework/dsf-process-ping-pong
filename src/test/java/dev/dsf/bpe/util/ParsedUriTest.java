package dev.dsf.bpe.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ParsedUriTest
{
	@Test
	public void preservesPlusInQueryParameters()
	{
		ParsedUri parsed = ParsedUri.fromUriString("https://example.com/?q=C++&encoded=%2B");

		assertEquals(Map.of("q", List.of("C++"), "encoded", List.of("+")), parsed.getQueryParams());
	}

	@Test
	public void decodesEncodedDelimitersAfterSplitting()
	{
		ParsedUri parsed = ParsedUri.fromUriString("https://example.com/?x=a%26b%3Dc");

		assertEquals(List.of("a&b=c"), parsed.getQueryParams().get("x"));
	}

	@Test
	public void preservesEncodedSlashWithinPathSegment()
	{
		ParsedUri parsed = ParsedUri.fromUriString("https://example.com/a%2Fb/c");

		assertEquals(List.of("a/b", "c"), parsed.getPathSegments());
	}

	@Test
	public void decodesOnlyOnce()
	{
		ParsedUri parsed = ParsedUri.fromUriString("https://example.com/%252F?q=%2526");

		assertEquals(List.of("%2F"), parsed.getPathSegments());
		assertEquals(List.of("%26"), parsed.getQueryParams().get("q"));
	}

	@Test
	public void rejectsInvalidUtf8()
	{
		assertThrows(IllegalArgumentException.class, () -> ParsedUri.fromUriString("https://example.com/?q=%FF"));
	}

	@Test
	public void rejectsRelativeAndNonHttpUris()
	{
		assertThrows(IllegalArgumentException.class, () -> ParsedUri.fromUriString("/relative/path"));
		assertThrows(IllegalArgumentException.class, () -> ParsedUri.fromUriString("ftp://example.com/path"));
	}

	@Test
	public void preservesRepeatedParametersAndOrder()
	{
		ParsedUri parsed = ParsedUri.fromUriString("https://example.com/?a=1&b=2&a=3");

		assertEquals(List.of("1", "3"), parsed.getQueryParams().get("a"));
		assertEquals(List.of("a", "b"), List.copyOf(parsed.getQueryParams().keySet()));
	}

	@Test
	public void returnsImmutableResults()
	{
		ParsedUri parsed = ParsedUri.fromUriString("https://example.com/?a=1");

		assertThrows(UnsupportedOperationException.class, () -> parsed.getQueryParams().put("b", List.of("2")));
		assertThrows(UnsupportedOperationException.class, () -> parsed.getQueryParams().get("a").add("2"));
		assertThrows(UnsupportedOperationException.class, () -> parsed.getPathSegments().add("extra"));
	}
}
