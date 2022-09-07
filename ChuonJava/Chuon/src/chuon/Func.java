package chuon;

public interface Func<T, R>
{
	R run(T...args) throws Exception;
}
