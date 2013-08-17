package de.ismll.table.io.weka;

public class AllNominalEncoder implements ArffEncoder {

	private final float missingValueIndicator;

	public AllNominalEncoder() {
		this(-1f);
	}

	public AllNominalEncoder(float missingValueIndicator) {
		this.missingValueIndicator = missingValueIndicator;
	}

	@Override
	public Type getAttributeType(int column) {
		return Type.Nominal;
	}

	@Override
	public String getAttributeName(int column) {
		return "nom" + column;
	}

	@Override
	public String encode(int column, float value) {
		if (Math.abs(value - missingValueIndicator) < 0.001)
			return "?";
		return value + "";
	}

	@Override
	public String getName() {
		return "all-nominal-dataset";
	}

}
