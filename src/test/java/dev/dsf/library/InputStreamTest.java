package dev.dsf.library;

import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Test;

public class InputStreamTest
{
	@Test
	public void testSkipNBytesSuccessWhenAllSkipped() throws IOException
	{
		int length = 1000;
		int toSkip = length;
		byte[] data = randomData(length);
		InputStream in = new ByteArrayInputStream(data);
		in.skipNBytes(toSkip);
	}

	@Test
	public void testSkipNBytesSuccessWhenSomeSkipped() throws IOException
	{
		int length = 1000;
		int toSkip = length / 2;
		byte[] data = randomData(length);
		InputStream in = new ByteArrayInputStream(data);
		in.skipNBytes(toSkip);
	}

	@Test
	public void testSkipNBytesFailOnSkipTooMany() throws IOException
	{
		int length = 1000;
		int toSkip = length + 1;
		byte[] data = randomData(length);
		InputStream in = new ByteArrayInputStream(data);
		assertThrows(EOFException.class, () -> in.skipNBytes(toSkip));
	}

	private byte[] randomData(int length)
	{
		byte[] data = new byte[length];
		new Random().nextBytes(data);
		return data;
	}
}
