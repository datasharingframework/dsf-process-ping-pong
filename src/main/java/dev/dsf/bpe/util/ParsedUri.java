package dev.dsf.bpe.util;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.PercentCodec;

public final class ParsedUri
{
	private static final PercentCodec PERCENT_CODEC = new PercentCodec();

	/**
	 * @param value
	 *            not <code>null</code>
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static ParsedUri fromUriString(String value)
	{
		return new ParsedUri(value);
	}

	private final URI uri;
	private final Map<String, List<String>> queryParams;
	private final List<String> pathSegments;

	private ParsedUri(String value)
	{
		Objects.requireNonNull(value, "value");

		this.uri = URI.create(value);
		validateHttpUri(uri);

		this.queryParams = parseQuery(uri.getRawQuery());
		this.pathSegments = parsePath(uri.getRawPath());
	}

	private static void validateHttpUri(URI uri)
	{
		String scheme = uri.getScheme();

		if (!uri.isAbsolute() || scheme == null
				|| !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) || uri.getHost() == null
				|| uri.getHost().isEmpty())
		{
			throw new IllegalArgumentException("Http or https URI with valid host expected");
		}
	}

	/**
	 * Returns decoded query parameters in encounter order. Repeated names retain all values in encounter order.
	 */
	public Map<String, List<String>> getQueryParams()
	{
		return queryParams;
	}

	/**
	 * Returns decoded, non-empty path segments. Encoded slashes remain inside their original segment.
	 */
	public List<String> getPathSegments()
	{
		return pathSegments;
	}

	private static Map<String, List<String>> parseQuery(String query)
	{
		if (query == null || query.isEmpty())
			return Map.of();

		Map<String, List<String>> params = new LinkedHashMap<>();

		for (String pair : query.split("&", -1))
		{
			if (pair.isEmpty())
				continue;

			int separator = pair.indexOf('=');

			String rawName = separator < 0 ? pair : pair.substring(0, separator);
			String rawValue = separator < 0 ? "" : pair.substring(separator + 1);

			String name = percentDecode(rawName);
			String value = percentDecode(rawValue);

			params.computeIfAbsent(name, _ -> new ArrayList<>()).add(value);
		}

		Map<String, List<String>> result = new LinkedHashMap<>();
		params.forEach((name, values) -> result.put(name, List.copyOf(values)));

		return Collections.unmodifiableMap(result);
	}

	private static List<String> parsePath(String rawPath)
	{
		if (rawPath == null || rawPath.isEmpty() || rawPath.equals("/"))
			return List.of();

		// sprit by '/' %2F must not create segment boundary
		return Arrays.stream(rawPath.split("/")).filter(segment -> !segment.isEmpty())
				.map(segment -> URLDecoder.decode(segment, StandardCharsets.UTF_8)).toList();
	}

	public static String percentDecode(String encodedInput) throws IllegalArgumentException
	{
		byte[] decodedBytes;

		try
		{
			decodedBytes = PERCENT_CODEC.decode(encodedInput.getBytes(StandardCharsets.UTF_8));

		}
		catch (DecoderException e)
		{
			throw new IllegalArgumentException("Malformed percent escape", e);
		}

		try
		{
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(decodedBytes)).toString();
		}
		catch (CharacterCodingException e)
		{
			throw new IllegalArgumentException("Invalid UTF-8 sequence", e);
		}
	}
}
