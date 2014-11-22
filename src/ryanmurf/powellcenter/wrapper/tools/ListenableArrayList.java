package ryanmurf.powellcenter.wrapper.tools;

import java.util.ArrayList;

public class ListenableArrayList<T> extends ArrayList<T>
implements Listenable<T> {
	private static final long serialVersionUID = 1L;
	//private ArrayList<T> internalList;
	private ListListener<T> listener;

	public boolean add(T item) {
		if(listener != null)
			listener.beforeAdd(item);
		super.add(item);
		if(listener != null)
			listener.afterAdd(item);
		return true;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if(listener != null)
			listener.beforeRemove((T) o);
		super.remove(o);
		if(listener != null)
			listener.afterRemove();
		return false;
	};

	public void setListener(ListListener<T> listener) {
		this.listener = listener;
	}
}
