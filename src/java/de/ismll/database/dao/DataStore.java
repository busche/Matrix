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
	 */
	public IEntityBacked query(final Entity template, final Column orderField, final String how) throws DataStoreException, DataStoreException {
		final Table entityType = template.getEntityType();

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

	public IEntityBacked query(final Table entityType, final Column[] nativeFieldNames, final String[] requiredFieldValues)throws DataStoreException {
		return query(entityType, nativeFieldNames, requiredFieldValues, null, null);
	}

	public abstract IEntityBacked query(final Table entityType, final Column[] nativeFieldNames, final Object[] requiredFieldValues, Column order, String how) throws DataStoreException;


	public void setThrowIfNotExists(final boolean throwIfNotExists) {
		this.throwIfNotExists = throwIfNotExists;
	}

	public abstract long insertOrUpdate(Entity e) throws DataStoreException, DataStoreException;

	/**
	 * returns all entities for the given type.
	 */
	public abstract IEntityBacked query(final Table type) throws DataStoreException, DataStoreException;

}
