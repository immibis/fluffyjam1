package com.immibis.fluffyjam1;

public final class Reagent {
	
	
	public static final int R_FOOD = 0;
	
	public static final int COUNT = 1;
	
	
	public static final int COLOUR[] = new int[COUNT];
	
	static {
		COLOUR[R_FOOD] = 0xFFAE00;
	}
	
	private Reagent() {}
}
