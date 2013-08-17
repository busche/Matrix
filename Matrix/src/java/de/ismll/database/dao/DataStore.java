package de.ismll.database.dao;

import java.util.Arrays;
import java.util.Vector;

/**
 * A
 * 
 * @author Busche
 * 
 */
public abstract class DataStore {

	/**
	 * control whether to throw Exceptions on error, or not.
	 */
	protected boolean throwIfNotExists;

	public DataStore() {
		super();
	}

	/**
	 * this method should be called to perform clean-up operations (e.g., freeing resources)
	 */
	public abstract void close();

	public abstract void ensureTableExists(final Table type) throws DataStoreException ;

	public IEntityBacked query(final Entity template) throws DataStoreException {
		return query(template, null, null);
	}

	/**
	 * Queries for all Entities that exactly match the equality criterion on the
	 * given Entity, e.g.:
	 * 
	 * - If some String is given, all entities are returned which exactly match
	 * that string on the field
	 * 
	 * - If some id is given (esp. on linked entity types), return a list where
	 * all returned entities match on that field (e.g., return all
	 * LinkedPresentations where the session ID is equal to that given one.
	 * 
	 * 
	 * 
	 * 
	 * @param template
	 * @return
	 * @throws DataStoreException
	 * @throws DataStoreException
	 */
	public IEntityBacked query(final Entity template, final Column orderField, final String how) throws DataStoreException, DataStoreException {
		// SELECT * FROM type.getTableName() WHERE _id=id;
		final Table entityType = template.getEntityType();
		//		if (template.id>=0) {
		// TODO: re-enable
		//			return  wrap(query(entityType, template.id));
		//		}

		final Object[] templateData = template.data;

		final Vector<Column> fields = entityType.getFields();
		final int fieldsSize = fields.size();
		final boolean[] mask = new boolean[fieldsSize];
		Arrays.fill(mask, false);

		/*
		 * which fields in the template are given?
		 */
		int numFieldsSet = 0;
		// find field which are not null, then construct Query.
		for (int i = fieldsSize-1; i >= 0; i--)
			if (templateData[i]!=null) {
				mask[i]=true;
				numFieldsSet++;
			}

		/*
		 * construct new arrays, holding nativeName->value mappings.
		 */
		final Column[] fieldsSet = new Column[numFieldsSet];
		final Object[] equalityConditions = new Object[numFieldsSet];

		int cnt = 0;

		for (int i = fieldsSize-1; i >= 0; i--)
			if (mask[i]) {
				final Column elementAt =  fields.elementAt(i);
				fieldsSet[cnt] = elementAt;
				equalityConditions[cnt] = templateData[i];
				cnt++;
			}


		return query(entityType, fieldsSet, equalityConditions, orderField, how);
	}

	/**
	 * makes a range query on the entity type where the entity values must be within the given range.
	 * 
	 * @param type
	 * @param field the field reference (must be a field within entity!.
	 * @param from
	 * @param to
	 * @param inclusive whether to include the from/to values, or not. If true, then return [from,to], else ]from,to[
	 * @return
	 * @throws DataStoreException
	 */

	//	public IEntityBacked query(final Column field,
	//			final String from, final String to, final boolean inclusive) throws DataStoreException {
	//		final Table type = field.getAssociatedType();
	//
	//		return queryImpl(type, field, from, to, inclusive);
	//	}

	public IEntityBacked query(final Table entityType, final Column[] nativeFieldNames, final String[] requiredFieldValues)throws DataStoreException {
		return query(entityType, nativeFieldNames, requiredFieldValues, null, null);
	}

	public abstract IEntityBacked query(final Table entityType, final Column[] nativeFieldNames, final Object[] requiredFieldValues, Column order, String how) throws DataStoreException;

	//	/**
	//	 * Queries a certain Entity of that type with the given ID
	//	 *
	//	 * @param id
	//	 * @param type
	//	 * @return
	//	 * @throws DataStoreException
	//	 * @throws DataStoreException
	//	 */
	//	public abstract Entity query(final Table type, final long id) throws DataStoreException, DataStoreException;

	//	/**
	//	 * makes a range query on the entity type where the entity values must be within the given range.
	//	 *
	//	 * @param type
	//	 * @param field the public field name of the field to query.
	//	 * @param from
	//	 * @param to
	//	 * @param inclusive whether to include the from/to values, or not. If true, then return [from,to], else ]from,to[
	//	 * @return
	//	 * @throws DataStoreException
	//	 */
	//	public IEntityBacked query(final Table type, final String field, final String from, final String to, final boolean inclusive) throws DataStoreException {
	//		return query(type.getField(field, false), from, to, inclusive);
	//	}

	//	/**
	//	 * subclasses need to implement this! implementation of the {@link #query(Table, String, String, String, boolean)} and {@link #query(Table, String, String, String, boolean)}
	//	 *
	//	 *
	//	 * @param type
	//	 * @param field
	//	 * @param from
	//	 * @param to
	//	 * @param inclusive whether to include the from/to values, or not. If true, then return [from,to], else ]from,to[
	//	 * @return
	//	 * @throws DataStoreException
	//	 */
	//	protected abstract IEntityBacked queryImpl(final Table type, final Column field2,
	//			final String from, final String to, final boolean inclusive) throws DataStoreException;

	public void setThrowIfNotExists(final boolean throwIfNotExists) {
		this.throwIfNotExists = throwIfNotExists;
	}

	public abstract long insertOrUpdate(Entity e) throws DataStoreException, DataStoreException;

	/**
	 * returns all entities for the given type.
	 * 
	 * @param type
	 * @return
	 * @throws DataStoreException
	 * @throws DataStoreException
	 */
	public abstract IEntityBacked query(final Table type) throws DataStoreException, DataStoreException;

}
