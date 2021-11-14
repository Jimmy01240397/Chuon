package com.jimmiker;

public interface Func<T, R>
{
	R run(T...args) throws Exception;
}
