package de.ismll.database;

/**
 * the statementIdentifier may be used for preparing and reusing statements. e.g., if multiple inserts are called for one and the same entity type with different values.
 * 
 * @author Andre Busche
 *
 */
public class SqlStatement implements ISqlStatementSource{

	private final String commandText;

	private final Object[] data;

	private String statementIdentifier;

	public SqlStatement(String commandText, Object[] data) {
		this.commandText = commandText;
		this.data = data;
	}

	public SqlStatement(String commandText, String statementIdentifier, Object[] data) {
		this.commandText = commandText;
		this.statementIdentifier = statementIdentifier;
		this.data = data;
	}

	public SqlStatement(String commandText) {
		this(commandText, new Object[0]);
	}

	public SqlStatement getStatement() {
		return this;
	}


	public Object[] getParametersAsObjectArray() {
		return data;
	}

	public String getCommandText() {
		return commandText;
	}

	public SqlStatement setStatementIdentifier(String statementIdentifier) {
		this.statementIdentifier = statementIdentifier;
		return this;
	}

	public String getStatementIdentifier() {
		return statementIdentifier;
	}

}
