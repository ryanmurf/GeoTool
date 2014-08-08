package ryanmurf.powellcenter.wrapper.tools;

public interface ListListener<T> {
	void beforeAdd(T item);
	void afterAdd(T item);
	void beforeRemove(T item);
	void afterRemove();
}
