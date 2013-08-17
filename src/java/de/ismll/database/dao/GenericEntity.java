package de.ismll.database.dao;

public class GenericEntity extends Entity{

	private final Table table;

	public GenericEntity(final Table type) {
		super(type);
		table = type;
	}

	protected Table registerType() {
		return table;
	}


	//	public static GenericEntity construct(final String entityTypeName) {
	//		return construct(entityTypeName, new String[0]);
	//	}

	//	public static GenericEntity construct(final String entityTypeName, final String[] columnNames) {
	//
	//		final int columnCount=columnNames.length;
	//		final EntityType shadowType = new EntityType(entityTypeName, GenericEntity.class);
	//
	//		for (int i = columnCount-1; i>=0; i--) {
	//			final Column field = shadowType.addField(columnNames[i], Datatypes.STRING, i);
	//
	//		}
	//		return (GenericEntity) shadowType.createInstance();
	//
	//	}


}
