package de.ismll.table.impl;



public class DefaultElongableMatrixTest  {

	//	@Test
	public void test1() {
		DefaultElongableMatrix m = new DefaultElongableMatrix(2);
		float[] buffer = new float[2];
		m.dump();
		buffer[0] = 0.2f;
		buffer[1] = 0.4f;
		m.append(buffer);
		m.append(buffer);
		m.append(buffer);
		m.dump();
		m.append(buffer);
		m.append(buffer);
		m.append(buffer);
		buffer[1] = 5f;
		m.append(buffer);
		m.append(buffer);
		m.append(buffer);
		m.append(buffer);
		m.dump();
		for (int i = 0; i < m.getNumRows(); i++) {
			m.set(i, 0, (float) Math.random());
			System.out.println(m.get(i, 0) + "/" + m.get(i, 1));
		}
		m.dump();

	}
}
