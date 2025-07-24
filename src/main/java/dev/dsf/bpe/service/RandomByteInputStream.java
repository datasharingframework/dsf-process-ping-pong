package dev.dsf.bpe.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class RandomByteInputStream extends InputStream
{
	private final long numBytes;
	private long numBytesRead = 0;
	private final Random random;
	private boolean closed = false;

	public RandomByteInputStream(long numBytes)
	{
		this.numBytes = numBytes;
		this.random = new Random();
	}

	@Override
	public synchronized int read()
	{
		if (!closed && numBytesRead < numBytes)
		{
			int next = random.nextInt(255);
			numBytesRead++;
			return next;
		}
		else
		{
			this.close();
			return -1;
		}
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}

	@Override
	public synchronized int read(byte[] b, int off, int len)
	{
		if (len == 0)
			return 0;
		if (closed || numBytesRead >= numBytes)
		{
			this.close();
			return -1;
		}
		int localNumBytesRead = 0;
		for (int i = 0; i < Math.min(len, b.length - off); i++)
		{
			if (!closed && numBytesRead < numBytes)
			{
				byte next = (byte) random.nextInt(255);
				b[off + localNumBytesRead] = next;
				localNumBytesRead++;
				numBytesRead++;
			}
			else
			{
				this.close();
				break;
			}
		}
		if (localNumBytesRead == numBytes)
			this.close();
		return localNumBytesRead;
	}

	@Override
	public synchronized byte[] readAllBytes()
	{
		if (numBytes <= Integer.MAX_VALUE)
		{
			byte[] b = new byte[(int) numBytes];
			read(b, 0, (int) (numBytes - numBytesRead));
			return b;
		}
		else
		{
			throw new UnsupportedOperationException(
					"JVM does not support array lengths longer than Integer.MAX_VALUE values");
		}
	}

	@Override
	public int readNBytes(byte[] b, int off, int len)
	{
		return read(b, off, len);
	}

	@Override
	public synchronized long transferTo(OutputStream out) throws IOException
	{
		return super.transferTo(out);
	}

	@Override
	public synchronized long skip(long n)
	{
		if (n <= 0)
			return 0;
		long skippableBytes = numBytes - numBytesRead;
		if (skippableBytes < n)
		{
			numBytesRead += skippableBytes;
			return skippableBytes;
		}
		else
		{
			numBytesRead += n;
			return n;
		}
	}

	@Override
	public synchronized int available() throws IOException
	{
		if (closed || numBytesRead >= numBytes)
			throw new IOException("Stream is closed");
		return (int) (numBytes - numBytesRead);
	}

	@Override
	public void mark(int readAheadLimit)
	{
		throw new UnsupportedOperationException("RandomByteInputStream does not support mark/reset");
	}

	@Override
	public synchronized void reset()
	{
		throw new UnsupportedOperationException("RandomByteInputStream does not support mark/reset");
	}

	@Override
	public void close()
	{
		this.closed = true;
	}

	public boolean isClosed()
	{
		return closed;
	}
}
