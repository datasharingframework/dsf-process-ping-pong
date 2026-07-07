package dev.dsf.bpe;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessErrors
{
	private final List<ProcessError> entries;

	public ProcessErrors()
	{
		entries = new Vector<>();
	}

	@JsonCreator
	public ProcessErrors(@JsonProperty("entries") Collection<ProcessError> entries)
	{
		if (Objects.nonNull(entries))
		{
			this.entries = new Vector<>(entries);
		}
		else
		{
			this.entries = new Vector<>();
		}
	}

	@JsonProperty("entries")
	public List<ProcessError> getEntries()
	{
		return entries;
	}

	public void add(ProcessError error)
	{
		entries.add(error);
	}

	public void addAll(ProcessErrors errors)
	{
		entries.addAll(errors.getEntries());
	}

	@JsonIgnore
	public boolean isEmpty()
	{
		return entries.isEmpty();
	}
}
