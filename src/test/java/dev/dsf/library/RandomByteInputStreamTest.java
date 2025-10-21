package dev.dsf.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import dev.dsf.bpe.service.RandomByteInputStream;

public class RandomByteInputStreamTest
{
	@Test
	@Ignore
	public void ReadLargeLongValueTest()
	{
		try (RandomByteInputStream inputStream = new RandomByteInputStream(10000000000L))
		{
			int out;
			long mod = 10;
			long count = 0;
			while ((out = inputStream.read()) >= 0)
			{
				count++;
				if (count % mod == 0)
				{
					mod *= 10;
					System.out.println(count);
				}
			}
			assertEquals(-1, out);
			assertTrue(inputStream.isClosed());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void ReadSmallLongValueTest()
	{
		try (RandomByteInputStream inputStream = new RandomByteInputStream(1000000L))
		{
			int out;
			long mod = 10;
			long count = 0;
			while ((out = inputStream.read()) >= 0)
			{
				count++;
				if (count % mod == 0)
				{
					mod *= 10;
					System.out.println(count);
				}
			}
			assertEquals(-1, out);
			assertTrue(inputStream.isClosed());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void ReadPartIntoArrayTest()
	{
		try (RandomByteInputStream inputStream = new RandomByteInputStream(1000000L))
		{
			byte[] bytes = new byte[10000];
			int out = inputStream.read(bytes);
			assertEquals(bytes.length, out);
			assertFalse(inputStream.isClosed());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void ReadAllIntoArrayTest()
	{
		try (RandomByteInputStream inputStream = new RandomByteInputStream(1000000L))
		{
			byte[] bytes = new byte[1000000];
			int out = inputStream.read(bytes);
			assertEquals(bytes.length, out);
			assertTrue(inputStream.isClosed());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void ReadPartIntoArrayOffsetTest()
	{
		try (RandomByteInputStream inputStream = new RandomByteInputStream(1000000L))
		{
			byte[] bytes = new byte[10000];
			int offset = bytes.length / 2;
			int amount = bytes.length / 10;
			int out = inputStream.read(bytes, offset, amount);
			assertEquals(amount, out);
			assertFalse(inputStream.isClosed());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void ReadHalfIntoArrayOffsetTest()
	{
		try (RandomByteInputStream inputStream = new RandomByteInputStream(1000000L))
		{
			byte[] bytes = new byte[1000000];
			int offset = bytes.length / 2;
			int amount = bytes.length / 2;
			int out = inputStream.read(bytes, offset, amount);
			assertEquals(amount, out);
			assertFalse(inputStream.isClosed());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void ReadAllIntoArrayOffsetTest()
	{
		try (RandomByteInputStream inputStream = new RandomByteInputStream(1000000L))
		{
			byte[] bytes = new byte[1000000];
			int offset = 0;
			int amount = bytes.length;
			int out = inputStream.read(bytes, offset, amount);
			assertEquals(amount, out);
			assertTrue(inputStream.isClosed());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void ReadAllIntoArrayOffsetAmountTooHighTest()
	{
		try (RandomByteInputStream inputStream = new RandomByteInputStream(1000000L))
		{
			byte[] bytes = new byte[1000000];
			int offset = bytes.length / 2;
			int amount = bytes.length;
			int out = inputStream.read(bytes, offset, amount);
			assertEquals(offset, out);
			assertFalse(inputStream.isClosed());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
