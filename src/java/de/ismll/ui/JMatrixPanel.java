package de.ismll.ui;

import java.awt.Dimension;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;

public class JMatrixPanel extends AbstractVolatileImagePanel {

	protected Matrix data;
	protected int maxMatrixValue;
	protected int minMatrixValue;

	public JMatrixPanel() {
		super();
	}

	public final void setData(Matrix m) {
		this.data = m;

		int max = (int) Math.ceil(Matrices.max(m));
		int min = (int) Math.floor(Matrices.min(m));

		maxMatrixValue = Math.max(max, maxMatrixValue);
		minMatrixValue = Math.min(min, minMatrixValue);
		width = Math.max(width, m.getNumColumns());
		height = Math.max(height, m.getNumRows());

		setSize(width, height);
		setPreferredSize(new Dimension(width, height));
		// render();
	}

	public Matrix getData() {
		return data;
	}

}