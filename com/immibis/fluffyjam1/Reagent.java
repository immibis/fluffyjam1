package com.immibis.fluffyjam1;

public final class Reagent {
	
	public static final int R_FOOD = 0;
	public static final int R_BLOOD = 1;
	public static final int R_OXYGEN = 2;
	public static final int R_WATER = 3;
	//public static final int R_UNUSED = 4;
	public static final int R_STOOL = 5;
	public static final int R_MWASTE = 6;
	
	public static final int COUNT = 8;
	
	
	public static final int[] COLOUR = new int[COUNT];
	public static final String[] NAME = new String[COUNT];
	public static final float[] BLOOD_CAP = new float[COUNT]; // proportion this can dissolve in blood
	public static final boolean[] IS_LIQUID = new boolean[COUNT];

	
	static {
		COLOUR[R_FOOD] = 0xFFAE00;
		COLOUR[R_BLOOD] = 0xFF0000;
		COLOUR[R_OXYGEN] = 0x00FF00;
		COLOUR[R_WATER] = 0x0000FF;
		COLOUR[R_STOOL] = 0x966400;
		COLOUR[R_MWASTE] = 0xFFFF00;
		
		NAME[R_FOOD] = "nutrients";
		NAME[R_BLOOD] = "blood";
		NAME[R_OXYGEN] = "air";
		NAME[R_WATER] = "water";
		NAME[R_STOOL] = "stool";
		NAME[R_MWASTE] = "metabolic waste";
		
		IS_LIQUID[R_FOOD] = false;
		IS_LIQUID[R_BLOOD] = true;
		IS_LIQUID[R_OXYGEN] = true;
		IS_LIQUID[R_WATER] = true;
		IS_LIQUID[R_STOOL] = false;
		IS_LIQUID[R_MWASTE] = true;
		
		BLOOD_CAP[R_FOOD] = 1f;
		BLOOD_CAP[R_OXYGEN] = 0.2f;
		BLOOD_CAP[R_WATER] = 1f;
		BLOOD_CAP[R_MWASTE] = 0.1f;
		BLOOD_CAP[R_STOOL] = 0.1f;
	}
	
	private Reagent() {}
}
