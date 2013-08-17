package de.ismll.table.impl;

import de.ismll.bootstrap.Parameter;

public class BootstrapLoadTest {

	@Parameter(cmdline="m")
	private DefaultMatrix m;

	public void setM(DefaultMatrix m) {
		this.m = m;
	}

	public DefaultMatrix getM() {
		return m;
	}
}
