package com.immibis.fluffyjam1;

public final class Reagent {
	
	
	public static final int R_FOOD = 0;
	public static final int R_BLOOD = 1;
	public static final int R_OXYGEN = 2;
	
	public static final int COUNT = 3;
	
	
	public static final int COLOUR[] = new int[COUNT];

	
	static {
		COLOUR[R_FOOD] = 0xFFAE00;
		COLOUR[R_BLOOD] = 0x8000FF;
		COLOUR[R_OXYGEN] = 0xFF0000;
	}
	
	private Reagent() {}
}
