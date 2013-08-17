package de.ismll.filter;

import de.ismll.table.Matrix;

public interface Filter {


	String getVisibleName();

	/**
	 * applied the filter
	 * 
	 * @param which
	 * @return
	 */
	Matrix apply(Matrix which);

}
