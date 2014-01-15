package com.immibis.fluffyjam1;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Reagents implements Serializable {
	private static final long serialVersionUID = 1L;
	
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

	public float addRespectingCapacity(int id, float amt) {
		amt = Math.min(amt, capacity - total);
		if(amt < 0) amt = 0;
		set(id, get(id) + amt);
		return amt;
	}

	/** Sets 'what' to contain the amount of stuff actually removed. */
	public void remove(Reagents what) {
		for(int i = Reagent.COUNT-1; i >= 0; i--) {
			amount[i] -= (what.amount[i] = Math.min(amount[i], what.amount[i]));
		}
		calc_total();
	}
	
	public void remove(float fraction) {
		fraction = 1-fraction;
		for(int i = Reagent.COUNT-1; i >= 0; i--)
			amount[i] *= fraction;
		calc_total();
	}

	public Reagents getVolume(float amt) {
		return total < 0.0001 ? new Reagents() : getFraction(Math.min(1, amt / total));
	}

	public Reagents getFraction(float f) {
		Reagents rv = new Reagents();
		for(int i = Reagent.COUNT-1; i >= 0; i--)
			rv.set(i, get(i) * f);
		return rv;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int k = 0; k < Reagent.COUNT; k++) {
			if(amount[k] > 0.001) {
				if(sb.length() > 0)
					sb.append(", ");
				sb.append(amount[k]);
				sb.append(' ');
				sb.append(Reagent.NAME[k]);
			}
		}
		return sb.toString();
	}
	
	public List<String> describe() {
		List<String> rv = new ArrayList<String>();
		DecimalFormat fmt = new DecimalFormat("0.0");
		for(int k = 0; k < Reagent.COUNT; k++)
			if(amount[k] > 0.001)
				rv.add(fmt.format(amount[k])+"mL "+Reagent.NAME[k]);
		return rv;
	}

	public void add(Reagents what) {
		for(int i = Reagent.COUNT-1; i >= 0; i--)
			amount[i] += what.amount[i];
		calc_total();
	}

	/** Returns amount actually removed */
	public float remove(int id, float amt) {
		amt = Math.min(amt, amount[id]);
		set(id, amount[id] - amt);
		return amt;
	}

	public float add(int id, float amt) {
		set(id, amount[id] + amt);
		return amt;
	}

	public float getTotal() {
		return total;
	}
	
	/** Returns the amount of reagent that could be dissolved right not */
	public float getDissolveSpace(int id) {
		return Math.max(0, amount[Reagent.R_BLOOD] * Reagent.BLOOD_CAP[id] - amount[id]);
	}

	/** "Dissolve into blood" some reagent - same as addRespectingCapacity,
	 * but the capacity used is get(R_BLOOD) * Reagent.BLOOD_CAP[id].
	 * Returns amount actually dissolved. */
	public float dissolve(int id, float amt) {
		float cap = amount[Reagent.R_BLOOD] * Reagent.BLOOD_CAP[id];
		amt = Math.min(amt, cap - amount[id]);
		if(amt < 0)
			return 0;
		set(id, amount[id] + amt);
		return amt;
	}

	public float getRemainingCapacity() {
		return capacity - total;
	}

	public double getFractionFull() {
		return total / capacity;
	}
}
