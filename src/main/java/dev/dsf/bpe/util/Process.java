package dev.dsf.bpe.util;

public enum Process
{
	PING, PONG;

	public String toString()
	{
		return this.name().toLowerCase();
	}
}
