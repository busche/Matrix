package de.ismll.database.dao;


/**
 * an interface alike java.util.Map, with reduced amount of methods.
 * 
 * @author Busche
 *
 */
public interface IContentHolder {
	public Object get(String key);
	public <T> T get(String key, Class<T> type);
	public boolean isEmpty();
	public Object put(String key, Object value);
	public int size();
	//	public Enumeration keys();
	public void clear();
}
