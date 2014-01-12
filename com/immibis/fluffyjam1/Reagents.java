package com.immibis.fluffyjam1;

public class Reagents {
	
	// All amounts in mL
	
	/** If this is used for a fixed size container, contains the capacity of that container.
	 * Some operations don't use this. */
	public float capacity = 0;
	
	private float total = 0;
	private float[] amount = new float[Reagent.COUNT];
	
	/** Returns the amount of one reagent */
	public float get(int id) {
		return amount[id];
	}
	
	/** Sets the amount of one reagent */
	public void set(int id, float val) {
		amount[id] = val;
	}
	
	public void pourInto(Reagents dest) {
		if(dest.total >= dest.capacity)
			return;
		else if(total < 0.0001)
			pourInto(dest, 1);
		else
			pourInto(dest, Math.min(1, (dest.capacity - dest.total) / total));
	}
	
	public void pourInto(Reagents dest, float pct) {
		for(int i = Reagent.COUNT-1; i >= 0; i--) {
			float transfer = amount[i] * pct;
			dest.amount[i] += transfer;
			amount[i] -= transfer;
			dest.total += transfer;
		}
		calc_total(); 
	}
	
	private void calc_total() {
		total = 0;
		for(int i = Reagent.COUNT-1; i >= 0; i--)
			total += amount[i];
	}
}
