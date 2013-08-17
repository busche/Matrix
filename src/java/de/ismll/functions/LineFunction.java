package de.ismll.functions;


public class LineFunction implements Function1D {

	private float slope;
	private float intercept;

	public LineFunction(float slope, float intercept) {
		super();
		this.slope = slope;
		this.intercept=intercept;
	}

	public float getSlope() {
		return slope;
	}

	public void setSlope(float slope) {
		this.slope = slope;
	}

	public float getIntercept() {
		return intercept;
	}

	public void setIntercept(float intercept) {
		this.intercept = intercept;
	}

	@Override
	public double compute(double x_i) {
		return intercept + slope * x_i;
	}

}
