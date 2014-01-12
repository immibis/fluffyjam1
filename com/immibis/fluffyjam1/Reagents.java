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
		total += val - amount[id];
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

	public void copyTo(Reagents dest) {
		for(int i = Reagent.COUNT-1; i >= 0; i--)
			dest.amount[i] = amount[i];
		dest.total = total;
		dest.capacity = capacity;
	}

	public void addRespectingCapacity(int id, float amt) {
		amt = Math.min(amt, capacity - total);
		if(amt > 0)
			set(id, get(id) + amt);
	}

	public void remove(Reagents what) {
		for(int i = Reagent.COUNT-1; i >= 0; i--)
			amount[i] = Math.max(0, amount[i] - what.amount[i]);
		calc_total();
	}
	
	public void remove(float fraction) {
		fraction = 1-fraction;
		for(int i = Reagent.COUNT-1; i >= 0; i--)
			amount[i] *= fraction;
		calc_total();
	}

	public Reagents getVolume(float amt) {
		return total < 0.0001 ? new Reagents() : getFraction(amt / total);
	}

	public Reagents getFraction(float f) {
		Reagents rv = new Reagents();
		for(int i = Reagent.COUNT-1; i >= 0; i--)
			rv.set(i, get(i) * f);
		return rv;
	}
}
