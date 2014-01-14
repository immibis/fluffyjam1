package com.immibis.fluffyjam1;

public final class Reagent {
	
	public static final int R_FOOD = 0;
	public static final int R_BLOOD = 1;
	public static final int R_OXYGEN = 2;
	public static final int R_WATER = 3;
	public static final int R_URINE = 4;
	public static final int R_STOOL = 5;
	public static final int R_MWASTE = 6;
	
	public static final int COUNT = 8;
	
	
	public static final int[] COLOUR = new int[COUNT];
	public static final String[] NAME = new String[COUNT];
	public static final float[] BLOOD_CAP = new float[COUNT]; // proportion this can dissolve in blood

	
	static {
		COLOUR[R_FOOD] = 0xFFAE00;
		COLOUR[R_BLOOD] = 0x8000FF;
		COLOUR[R_OXYGEN] = 0x00FF00;
		COLOUR[R_WATER] = 0x0000FF;
		COLOUR[R_URINE] = 0xFFFF00;
		COLOUR[R_STOOL] = 0x966400;
		COLOUR[R_MWASTE] = 0xFFFF00;
		
		NAME[R_FOOD] = "nutrients";
		NAME[R_BLOOD] = "blood";
		NAME[R_OXYGEN] = "air";
		NAME[R_WATER] = "water";
		NAME[R_URINE] = "urine";
		NAME[R_STOOL] = "stool";
		NAME[R_MWASTE] = "metabolic waste";
		
		BLOOD_CAP[R_FOOD] = 1f;
		BLOOD_CAP[R_OXYGEN] = 0.2f;
		BLOOD_CAP[R_WATER] = 0.3f;
		BLOOD_CAP[R_MWASTE] = 0.1f;
		BLOOD_CAP[R_URINE] = 0.1f;
		BLOOD_CAP[R_STOOL] = 0.1f;
	}
	
	private Reagent() {}
}
