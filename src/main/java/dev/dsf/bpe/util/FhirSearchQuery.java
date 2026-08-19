package dev.dsf.bpe.util;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FhirSearchQuery
{
	private final URI uriWithoutQuery;
	private final Map<String, List<String>> queryParams;
	private final String sourceUrl;

	public FhirSearchQuery(String url)
	{
		Objects.requireNonNull(url, "URL must not be null");

		if (url.isBlank())
			throw new IllegalArgumentException("URL must not be blank");

		sourceUrl = url;

		int firstQuestionMark = url.indexOf("?");

		if (firstQuestionMark < 0)
		{
			uriWithoutQuery = URI.create(url);
			queryParams = Map.of();
		}
		else
		{
			uriWithoutQuery = URI.create(url.substring(0, firstQuestionMark));
			String query = url.substring(firstQuestionMark + 1);
			queryParams = parseQuery(query);
		}
	}

	private Map<String, List<String>> parseQuery(String query)
	{
		Objects.requireNonNull(query, "Query must not be null");

		if (query.isBlank())
			throw new IllegalArgumentException("Query must not be empty if '?' is present");

		Map<String, List<String>> queryParams = new LinkedHashMap<>();

		for (String parameter : query.split("&", -1))
		{
			int equals = parameter.indexOf("=");

			if (equals < 0)
				throw new IllegalArgumentException("Invalid search parameter: " + parameter);

			String name = decodeUriComponent(parameter.substring(0, equals));
			String value = decodeUriComponent(parameter.substring(equals + 1));

			if (name.isBlank())
				throw new IllegalArgumentException("Search parameter name is blank");

			queryParams.computeIfAbsent(name, _ -> new ArrayList<>()).add(value);
		}

		queryParams.replaceAll((_, values) -> List.copyOf(values));

		return Collections.unmodifiableMap(queryParams);
	}

	public String getPath()
	{
		return uriWithoutQuery.getPath();
	}

	public Map<String, List<String>> getQueryParams()
	{
		return queryParams;
	}

	@Override
	public String toString()
	{
		return sourceUrl;
	}

	private static String decodeUriComponent(String value)
	{
		StringBuilder result = new StringBuilder(value.length());

		for (int i = 0; i < value.length(); i++)
		{
			char c = value.charAt(i);

			if (c != '%')
			{
				result.append(c);
				continue;
			}

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();

			while (i < value.length() && value.charAt(i) == '%')
			{
				if (i + 2 >= value.length() || !isHexDigit(value.charAt(i + 1)) || !isHexDigit(value.charAt(i + 2)))
				{
					throw new IllegalArgumentException("Invalid percent encoding in query parameter: " + value);
				}

				bytes.write((Character.digit(value.charAt(i + 1), 16) << 4) | Character.digit(value.charAt(i + 2), 16));

				i += 3;
			}

			try
			{
				result.append(StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
						.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes.toByteArray())));
			}
			catch (CharacterCodingException e)
			{
				throw new IllegalArgumentException("Invalid UTF-8 percent encoding in query parameter: " + value, e);
			}

			i--;
		}

		return result.toString();
	}

	private static boolean isHexDigit(char c)
	{
		return Character.digit(c, 16) >= 0;
	}
}
