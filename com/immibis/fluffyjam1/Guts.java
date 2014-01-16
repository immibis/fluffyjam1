package com.immibis.fluffyjam1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.immibis.fluffyjam1.Guts.DrawingTile;

public final class Guts implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public int w, h; // normally 20x15
	
	public transient GutsListener listener;
	
	private void parseDefault() {
		try {
			BufferedReader s = new BufferedReader(new InputStreamReader(Guts.class.getResourceAsStream("default.txt")));
			
			w = 0;
			h = 0;
			String l;
			while(!(l = s.readLine()).equals("END"));
			w = s.readLine().length();
			while((l = s.readLine()) != null)
				h++;
			
			s.close();
			s = new BufferedReader(new InputStreamReader(Guts.class.getResourceAsStream("default.txt")));
			
			String[] defs = new String[256];
			while(!(l = s.readLine()).equals("END"))
				defs[l.charAt(0)] = l.substring(2);
			
			
			s.readLine(); // skip width marker line
			
			
			tiles = new Tile[w*h];
			
			for(int y = 0; y < h; y++) {
				l = s.readLine();
				
				String[] xdata = l.substring(w).trim().split(" ");
				
				for(int x = 0; x < w; x++) {
					char c = (x < l.length() ? l.charAt(x) : '#');
					int k = x + y*w;
					
					if(c == ' ')
						tiles[k] = EmptyTile.instance;
					else if(c >= '1' && c <= '9')
						tiles[k] = createTileFromDef(xdata[c - '1']);
					else if(defs[c] == null)
						throw new RuntimeException("not defined: "+c);
					else
						tiles[k] = createTileFromDef(defs[c]);
				}
			}
			
			s.close();
			
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private int parseDirList(String dl) {
		int rv = 0;
		for(int k = 0; k < dl.length(); k++)
			switch(dl.charAt(k)) {
			case 'L': rv |= DM_L; break;
			case 'R': rv |= DM_R; break;
			case 'U': rv |= DM_U; break;
			case 'D': rv |= DM_D; break;
			}
		return rv;
	}
	
	private Tile createTileFromDef(String def) {
		switch(def.charAt(0)) {
		case 'M': return new MouthTile();
		case 'N': return new NoseTile();
		case 'H': return new HeartTile();
		case 'L': return new LungTile();
		case 'P': return new PipeTile(parseDirList(def.substring(1)));
		case 'I': return new IntestineTile(def.equals("IH"));
		case 'X': return new PipeCrossTile(parseDirList(def.substring(1)), 15^parseDirList(def.substring(1)));
		case 'T': return new TankTile();
		case 'B': return new BrainTile();
		case 'G': return new LegTile();
		case 'S': return new SensorTile(Integer.parseInt(def.substring(1), 10));
		case 'K':  {
			String[] p = def.substring(1).split(",");
			return new KidneyTile(Integer.parseInt(p[0], 10), Integer.parseInt(p[1], 10));
		}
		case 'O': return new OrificeTile(Integer.parseInt(def.substring(1), 10));
		case 'V':
			if(def.charAt(1) == 'A')
				return new ValveTile(Integer.parseInt(def.substring(2), 10), true);
			else
				return new ValveTile(Integer.parseInt(def.substring(1), 10), false);
		case '#': {
			String[] p = def.substring(1).split(",");
			return new ObstacleTile(Integer.parseInt(p[0], 10), Integer.parseInt(p[1], 10));
		}
		case '!': {
			String[] p = def.substring(1).split(",");
			return new PipeObstacleTile(parseDirList(p[0]), Integer.parseInt(p[1], 10), Integer.parseInt(p[2], 10));
		}
		default:
			throw new AssertionError("invalid: "+def);
		}
	}

	// directions
	public static final int D_U = 0;
	public static final int D_D = 1;
	public static final int D_L = 2;
	public static final int D_R = 3;
	// direction masks
	public static final int DM_U = 1;
	public static final int DM_D = 2;
	public static final int DM_L = 4;
	public static final int DM_R = 8;
	
	public static final float TARGET_BLOOD_PRESSURE = 0.1f;
	public static final float MAX_BLOOD_WATER = 1.0f; // target ratio of water:blood - kidneys will remove excess 
	
	// energy units per tick, aka EU/t
	public static final float METAB_RATE_BRAIN		= 1.0f / 20f;
	public static final float METAB_RATE_HEART		= 0.1f / 20f;
	public static final float METAB_RATE_LEG		= 0.2f / 20f;
	public static final float METAB_RATE_LEG_SPRINT	= 2.0f / 20f;
	
	
	// The amount of oxygen and food used, and mwaste produced, per energy unit (in mL/EU)
	public static final float BASE_METABOLIC_OXYGEN_RATIO = 3f;
	public static final float BASE_METABOLIC_FOOD_RATIO = 0.5f;
	public static final float BASE_METABOLIC_WASTE_RATIO = 0.35f;
	
	// amount of food/tick expected to be used when not doing anything
	public static final float FOOD_USE_RATE_IDLE = (METAB_RATE_BRAIN + METAB_RATE_HEART) * BASE_METABOLIC_FOOD_RATIO;
	
	
	public static class Tile implements Serializable {
		private static final long serialVersionUID = 1L;
		
		PipeNetwork[] nets = null;
		
		protected void initNets(int mask) {
			if(nets == null) nets = new PipeNetwork[4];
			for(int k = 0; k < 4; k++)
				if((mask & (1 << k)) != 0)
					nets[k] = new PipeNetwork();
		}
		
		protected void initNet(int mask) {
			if(nets == null) nets = new PipeNetwork[4];
			PipeNetwork net = new PipeNetwork();
			for(int k = 0; k < 4; k++)
				if((mask & (1 << k)) != 0)
					nets[k] = net;
		}
		
		public List<String> describe() {
			return Collections.emptyList();
		}
		
		/**
		 * Helper method to try to consume some energy and oxygen.
		 * @param r The blood supply (net.contents)
		 * @param nr The blood supply next tick (net.new_contents)
		 * @param amount The amount of energy needed.
		 * @param ox_ratio Multiplier for the amount of oxygen needed. Some tiles need more oxygen, some less. Usually 1.
		 * @return The amount of energy actually obtained.
		 */
		protected static float metabolize(Reagents r, Reagents nr, float amount, float ox_ratio) {
			ox_ratio *= BASE_METABOLIC_OXYGEN_RATIO;
			
			float to_use = Math.min(Math.min(r.get(Reagent.R_FOOD), nr.get(Reagent.R_FOOD))/BASE_METABOLIC_FOOD_RATIO, Math.min(r.get(Reagent.R_OXYGEN), nr.get(Reagent.R_OXYGEN))/ox_ratio);
			to_use = Math.min(to_use, amount);
			//to_use = Math.min(to_use, r.getDissolveSpace(Reagent.R_MWASTE)/BASE_METABOLIC_WASTE_RATIO);
			
			nr.remove(Reagent.R_FOOD, to_use*BASE_METABOLIC_FOOD_RATIO);
			nr.remove(Reagent.R_OXYGEN, to_use*ox_ratio);
			nr.add(Reagent.R_MWASTE, to_use*BASE_METABOLIC_WASTE_RATIO);
			
			return to_use;
		}
		
		protected static float calcFlow(PipeNetwork from, PipeNetwork to) {
			// (R_cur + flow) / R_size = (L_cur - flow) / L_size
			// R_cur + flow = (L_cur - flow) / L_size * R_size
			// R_cur + flow = L_cur/L_size*R_size - flow/L_size*R_size
			// flow + flow/L_size*R_size = L_cur/L_size*R_size - R_cur
			// flow*(1 + 1/L_size*R_size) = L_cur/L_size*R_size - R_cur
			// flow*(L_size + R_size) = L_cur*R_size - R_cur*L_size
			// flow = (L_cur*R_size - R_cur*L_size) / (L_size + R_size)
			
			float R_cur = to.contents.getTotal();
			float L_cur = from.contents.getTotal();
			float R_size = to.contents.capacity;
			float L_size = from.contents.capacity;
			
			return (L_cur*R_size - R_cur*L_size) / (L_size + R_size);
		}

		public void tick() {}
	}
	
	public static class EmptyTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		static EmptyTile instance = new EmptyTile();
	}
	
	public Reagents mouthBuffer = new Reagents();
	
	public class MouthTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		MouthTile() {
			initNets(DM_R);
		}
		
		int ticks = 0;
		
		@Override
		public void tick() {
			//if((++ticks) % 100 == 0) {
			//	nets[D_D].new_contents.set(Reagent.R_FOOD, 80.0f);
			//	nets[D_D].new_contents.set(Reagent.R_WATER, 30.0f);
			//}
			mouthBuffer.pourInto(nets[D_R].new_contents);
		}
	}
	
	public class NoseTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		NoseTile() {
			initNets(DM_R);
		}
		
		@Override
		public void tick() {
			
			final float EXCH_VOLUME = 3f; 
			
			Reagents to_remove = nets[D_R].contents.getFraction(EXCH_VOLUME / nets[D_R].contents.capacity);
			nets[D_R].new_contents.remove(to_remove);
			
			nets[D_R].new_contents.addRespectingCapacity(drowning ? Reagent.R_WATER : Reagent.R_OXYGEN, EXCH_VOLUME);
		}
	}
	
	public static class LungTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		LungTile() {
			initNets(DM_U | DM_L | DM_R);
		}
		
		@Override
		public void tick() {
			float flow = calcFlow(nets[D_L], nets[D_R]);
			
			if(flow <= 0)
				return; // nothing to do
			
			Reagents transfer = nets[D_L].contents.getVolume(flow);
			nets[D_L].new_contents.remove(transfer);
			
			if(nets[D_U].contents.get(Reagent.R_WATER) < 0.05f) { // this is called drowning.
				float avail_oxy = nets[D_U].contents.get(Reagent.R_OXYGEN);
				nets[D_U].new_contents.remove(Reagent.R_OXYGEN, transfer.dissolve(Reagent.R_OXYGEN, avail_oxy));
			
			} else {
				// if drowning, why not inhale some water?
				Reagents transfer2 = nets[D_U].contents.getVolume(0.2f);
				transfer2.set(Reagent.R_OXYGEN, 0);
				nets[D_U].new_contents.remove(transfer2);
				transfer.add(transfer2);
			}
			
			transfer.pourInto(nets[D_R].new_contents);
			transfer.pourInto(nets[D_L].new_contents);
		}
	}
	
	public static class HeartTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		HeartTile() {
			initNets(DM_L | DM_R);
		}
		@Override
		public void tick() {
			float flow = Math.max(20, calcFlow(nets[D_R], nets[D_L]));
			Reagents transfer = nets[D_R].contents.getVolume(flow);
			nets[D_R].new_contents.remove(transfer);
			
			float energy = metabolize(transfer, transfer, METAB_RATE_HEART, 1);
			
			transfer.pourInto(nets[D_L].new_contents);
			transfer.pourInto(nets[D_R].new_contents);
			
			if(nets[D_R].new_contents.get(Reagent.R_BLOOD) < nets[D_R].new_contents.capacity * TARGET_BLOOD_PRESSURE)
				nets[D_R].new_contents.addRespectingCapacity(Reagent.R_BLOOD, 8); // TODO should not be in HeartTile
		}
	}
	
	public class KidneyTile extends ObstacleTile {
		private static final long serialVersionUID = 1L;
		
		KidneyTile(int u, int v) {
			super(u, v);
			initNets(DM_U | DM_D);
			initNet(DM_L | DM_R);
		}
		
		@Override
		public void tick() {
			float flow = calcFlow(nets[D_U], nets[D_D]);
			
			checkCap(nets[D_U]);
			checkCap(nets[D_D]);
			
			PipeNetwork drain = nets[D_L];
			
			flow *= 0.3f;
			
			if(flow > 0) {
				doKidneyStuff(nets[D_U], nets[D_D], drain, flow);
			} else {
				doKidneyStuff(nets[D_D], nets[D_U], drain, -flow);
			}
		}
		
		private void checkCap(PipeNetwork pn) {
			if(pn.contents.getTotal() > pn.contents.capacity * 0.9f) {
				float to_remove = pn.contents.getTotal() - pn.contents.capacity * 0.9f;
				to_remove *= 0.2f;
				Reagents remove = pn.contents.getVolume(to_remove);
				pn.new_contents.remove(remove);
				remove.set(Reagent.R_BLOOD, 0);
				remove.set(Reagent.R_OXYGEN, 0);
				remove.pourInto(nets[D_R].new_contents);
				pn.new_contents.add(remove);
			}
		}

		private void doKidneyStuff(PipeNetwork from, PipeNetwork to, PipeNetwork drain, float flow) {
			
			Reagents transfer = from.contents.getVolume(flow);
			from.new_contents.remove(transfer);
			
			if(transfer.getTotal() < 0.01) {
				from.new_contents.add(transfer);
				return;
			}
			
			final float WATER_TO_MWASTE_RATIO = 0.5f;
			final float EXCESS_WATER_REMOVE_RATE = 0.3f;
			final float MWASTE_REMOVE_RATE = 0.8f;
			final float STOOL_TO_MWASTE_RATE = 10f;
			
			float s = Math.min(transfer.get(Reagent.R_STOOL), transfer.getDissolveSpace(Reagent.R_MWASTE) / STOOL_TO_MWASTE_RATE);
			transfer.remove(Reagent.R_STOOL, s);
			transfer.add(Reagent.R_MWASTE, s*STOOL_TO_MWASTE_RATE);
			
			float transfer_water_pct = transfer.get(Reagent.R_WATER) / transfer.get(Reagent.R_BLOOD);
			float transfer_excess_water = Math.max(0, transfer.get(Reagent.R_WATER) * (transfer_water_pct - MAX_BLOOD_WATER)); 
			float water_removed = Math.min(transfer.get(Reagent.R_WATER), Math.max(transfer.get(Reagent.R_MWASTE) * MWASTE_REMOVE_RATE * WATER_TO_MWASTE_RATIO, transfer_excess_water * EXCESS_WATER_REMOVE_RATE));
			float mwaste_removed = Math.min(transfer.get(Reagent.R_MWASTE), water_removed / WATER_TO_MWASTE_RATIO);
			
			float u_capacity = Math.min(drain.new_contents.getRemainingCapacity(), drain.contents.getRemainingCapacity());
			
			if(u_capacity > 0) {
				float factor = Math.min(1, u_capacity / (water_removed + mwaste_removed));
				water_removed *= factor;
				mwaste_removed *= factor;
				
				water_removed = transfer.remove(Reagent.R_WATER, water_removed);
				mwaste_removed = transfer.remove(Reagent.R_MWASTE, mwaste_removed);
				
				drain.new_contents.add(Reagent.R_WATER, water_removed);
				drain.new_contents.add(Reagent.R_MWASTE, mwaste_removed);
			}
			
			transfer.pourInto(to.new_contents);
			transfer.pourInto(from.new_contents);
			transfer.pourInto(drain.new_contents);
		}
	}
	
	public class ValveTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		int id;
		boolean autoOpen;
		
		ValveTile(int id, boolean autoOpen) {
			initNet(DM_U | DM_L);
			initNet(DM_D | DM_R);
			this.id = id;
			this.autoOpen = autoOpen;
		}
		
		boolean open = false;
		
		@Override
		public void tick() {
			if(autoOpen && !open && nets[D_U].new_contents.getFractionFull() > 0.99)
				open = true;
			
			if(open) {
				if(nets[D_U].new_contents.getFractionFull() < 0.01)
					open = false;
				
				nets[D_U].new_contents.pourInto(nets[D_D].new_contents);
				/*float flow = calcFlow(nets[D_U], nets[D_D]);
				if(flow > 0) {
					Reagents transfer = nets[D_U].contents.getVolume(flow);
					nets[D_U].new_contents.remove(transfer);
					nets[D_D].new_contents.add(transfer);
				} else {
					Reagents transfer = nets[D_D].contents.getVolume(-flow);
					nets[D_D].new_contents.remove(transfer);
					nets[D_U].new_contents.add(transfer);
				}*/
			}
		}
	}
	
	public class OrificeTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		int id;
		
		OrificeTile(int id) {
			initNet(DM_U | DM_D | DM_L | DM_R);
			this.id = id;
		}
		
		@Override
		public void tick() {
			PipeNetwork pn = nets[D_U];
			
			Reagents transfer = pn.contents.getVolume(30);
			pn.new_contents.remove(transfer);
			if(listener != null)
				listener.eject(transfer, this.id);
		}
	}
	
	public static class IntestineTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		boolean horiz;
		IntestineTile(boolean horiz) {
			this.horiz = horiz;
			
			if(horiz) {
				initNets(DM_L | DM_R);
				initNet(DM_U | DM_D);
			} else {
				initNets(DM_U | DM_D);
				initNet(DM_L | DM_R);
			}
		}
		
		@Override
		public void tick() {
			PipeNetwork net_in = nets[horiz ? D_L : D_U];
			PipeNetwork net_out = nets[horiz ? D_R : D_D];
			PipeNetwork net_blood = nets[horiz ? D_U : D_L];
			
			//if(net_in.contents.getFractionFull() > 0.5f) {
			//	net_in.new_contents.remove(Reagent.R_STOOL, net_out.new_contents.addRespectingCapacity(Reagent.R_STOOL, 0.5f * (net_in.contents.getFractionFull() - 0.5f) * Math.min(net_in.new_contents.get(Reagent.R_STOOL), net_in.contents.get(Reagent.R_STOOL))));
			//}
				
			float flow = calcFlow(net_in, net_out);
			//flow = Math.min(flow, 1);
			flow = Math.max(flow, 0.1f);
			
			Reagents blood = net_blood.contents, new_blood = net_blood.new_contents;
			//float max_food_transfer = new_blood.getDissolveSpace(Reagent.R_FOOD);
			
			Reagents transfer = net_in.contents.getVolume(flow);
			
			//if(transfer.get(Reagent.R_FOOD) > max_food_transfer)
			//	transfer.set(Reagent.R_FOOD, max_food_transfer);
			
			net_in.new_contents.remove(transfer);
			
			float avail_food = transfer.get(Reagent.R_FOOD);
			float used_food = avail_food * 0.3f;
			float leftover_food = used_food - new_blood.dissolve(Reagent.R_FOOD, used_food);
			transfer.remove(Reagent.R_FOOD, used_food);
			transfer.add(Reagent.R_STOOL, used_food*1.2f + leftover_food);
			
			float avail_water = transfer.get(Reagent.R_WATER);
			if(avail_water > 0)
				transfer.remove(Reagent.R_WATER, new_blood.addRespectingCapacity(Reagent.R_WATER, avail_water * 0.8f));
			
			
			
			transfer.pourInto(net_out.new_contents);
			transfer.pourInto(net_blood.new_contents);
			transfer.pourInto(net_in.new_contents);
		}
	}
	
	public class SensorTile extends Tile {
		int id;
		
		SensorTile(int id) {
			this.id = id;
			initNet(DM_L | DM_R | DM_U | DM_D);
		}
		
		@Override
		public void tick() {
			float value = nets[D_U].contents.getFractionFull();
			if(id == 1)
				ex1bar = value;
			else if(id == 2)
				ex2bar = value;
		}
		
		@Override
		public List<String> describe() {
			return Arrays.asList("Pressure sensor", "ID: "+id);
		}
	}
	
	// 0 = dead, 0-0.5 = coma, 0.5-1 = various symptoms, 0 = normal
	public float brain_function;
	
	// 0 = empty, 1 = full
	public float ex1bar, ex2bar, fbar, wbar, oxygen_level;
	
	public boolean drowning;
	
	public float leg_energy_level;
	public boolean is_sprinting;
	
	public class LegTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		LegTile() {
			initNet(DM_U);
		}
		
		@Override
		public void tick() {
			float energy_req = is_sprinting ? METAB_RATE_LEG_SPRINT : METAB_RATE_LEG;
			float energy_level_here = metabolize(nets[D_U].contents, nets[D_U].new_contents, energy_req, 1) / energy_req;
			leg_energy_level = Math.min(leg_energy_level, energy_level_here);
		}
	}
	
	public class BrainTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		BrainTile() {
			initNets(DM_D);
		}
		
		// So you don't die immediately.
		float startup_food = FOOD_USE_RATE_IDLE * 200f;
		float startup_water = 50;
		
		@Override
		public void tick() {
			PipeNetwork blood = nets[D_D];
			
			float energy = metabolize(blood.contents, blood.new_contents, METAB_RATE_BRAIN, 1);
			float mwaste_pct = blood.contents.get(Reagent.R_MWASTE) / blood.contents.get(Reagent.R_BLOOD);
			float stool_pct = blood.contents.get(Reagent.R_STOOL) / blood.contents.get(Reagent.R_BLOOD);
			
			if(startup_food > 0) startup_food -= blood.new_contents.dissolve(Reagent.R_FOOD, startup_food);
			if(startup_water > 0) startup_water -= blood.new_contents.dissolve(Reagent.R_WATER, startup_water);

			// normal: <10, coma: 100+, death: 200+
			float toxin_rel = (blood.contents.get(Reagent.R_MWASTE) + blood.contents.get(Reagent.R_STOOL)*5) / blood.contents.get(Reagent.R_BLOOD) * 70;
			
			if(toxin_rel < 10)
				brain_function = 1;
			else if(toxin_rel < 200)
				brain_function = 1 - toxin_rel / 200;
			else
				brain_function = 0;
			
			float energy_avail_pct = energy / METAB_RATE_BRAIN;
			if(energy_avail_pct < 0.05f)
				brain_function = Math.min(brain_function, 0.75f);
			
			fbar = Math.min(1, blood.contents.get(Reagent.R_FOOD) / (blood.contents.get(Reagent.R_BLOOD) * Reagent.BLOOD_CAP[Reagent.R_FOOD]));
			wbar = Math.min(1, blood.contents.get(Reagent.R_WATER) / (blood.contents.get(Reagent.R_BLOOD) * Reagent.BLOOD_CAP[Reagent.R_WATER]));
			oxygen_level = Math.min(1, blood.contents.get(Reagent.R_OXYGEN) / (blood.contents.get(Reagent.R_BLOOD) * Reagent.BLOOD_CAP[Reagent.R_OXYGEN]));
		}
	}
	
	public static class PipeObstacleTile extends ObstacleTile {
		private static final long serialVersionUID = 1L;
		
		PipeObstacleTile(int mask, int u, int v) {
			super(u, v);
			initNet(mask);
		}
	}
	
	public static class DrawingTile extends Tile {
		// TODO
	}
	
	public static class PipeTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		private byte mask;
		PipeTile(int mask) {
			this.mask = (byte)mask;
			initNet(mask);
		}
		public int getMask() {
			return mask;
		}
		private PipeNetwork getNet() {
			for(int k = 0; k < 4; k++)
				if(nets[k] != null)
					return nets[k];
			throw new AssertionError();
		}
		@Override
		public List<String> describe() {
			return getNet().new_contents.describe();
		}
	}
	
	public class TankTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		public TankTile() {
			initNet(DM_L | DM_R | DM_U | DM_D);
		}
		
		@Override
		public List<String> describe() {
			return nets[D_L].contents.describe();
		}
	}
	
	public static class ObstacleTile extends Tile {
		public int u, v;
		
		ObstacleTile(int u, int v) {
			this.u = u;
			this.v = v;
		}
	}

	public static class PipeCrossTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		private byte mask1, mask2;
		PipeCrossTile(int mask1, int mask2) {
			this.mask1 = (byte)mask1;
			this.mask2 = (byte)mask2;
			initNet(mask1);
			initNet(mask2);
		}
		public int getMask1() {
			return mask1;
		}
	}
	
	public static class PipeNetwork implements Serializable {
		private static final long serialVersionUID = 1L;
		
		Reagents contents, new_contents;
		int size; // number of tile-sides in this network
		boolean leak;
		Set<Tile> tiles = new HashSet<Tile>();
		
		// used when building networks - disjoint set data structure
		PipeNetwork parent;
		PipeNetwork getRoot() {if(parent != null) return parent = parent.getRoot(); else return this;}
	}
	
	public Tile[] tiles;
	private Reagents leaked_reagents;
	
	{
		leaked_reagents = new Reagents();
		leaked_reagents.capacity = Float.POSITIVE_INFINITY;
		
		parseDefault();
		buildNetworks();
	}
	
	public Tile getTile(int x, int y) {
		if(!validCoords(x, y))
			return null;
		return tiles[x + y*w];
	}
	
	public void buildNetworks() {
		for(int k = 0; k < tiles.length; k++)
			if(tiles[k].nets != null)
				Arrays.fill(tiles[k].nets, null);
		
		for(int y = 0; y < h; y++)
			for(int x = 0; x < w; x++)
				if(tiles[x + y*w].nets != null) {
					PipeNetwork[] n = tiles[x + y*w].nets;
					if(n[D_U] != null) n[D_U].leak |= mergeNets(x, y, D_U, x, y-1);
					if(n[D_D] != null) n[D_D].leak |= mergeNets(x, y, D_D, x, y+1);
					if(n[D_L] != null) n[D_L].leak |= mergeNets(x, y, D_L, x-1, y);
					if(n[D_R] != null) n[D_R].leak |= mergeNets(x, y, D_R, x+1, y);
				}
		
		Set<PipeNetwork> nets = new HashSet<PipeNetwork>();
		for(int y = 0; y < h; y++)
			for(int x = 0; x < w; x++)
				if(tiles[x + y*w].nets != null) {
					PipeNetwork[] n = tiles[x + y*w].nets;
					for(int k = 0; k < 4; k++)
						if(n[k] != null) {
							PipeNetwork pn = n[k];
							nets.add(n[k] = pn.getRoot());
							n[k].tiles.add(tiles[x + y*w]);
							n[k].leak |= pn.leak;
							// increment n[k].size if this tile-side is actually connected to another tile-side
							switch(k) {
							case D_U: if(y > 0 && tiles[x + (y-1)*w].nets != null && tiles[x + (y-1)*w].nets[D_D] != null) n[k].size++; break;
							case D_D: if(y<h-1 && tiles[x + (y+1)*w].nets != null && tiles[x + (y+1)*w].nets[D_U] != null) n[k].size++; break;
							case D_L: if(x > 0 && tiles[(x-1) + y*w].nets != null && tiles[(x-1) + y*w].nets[D_R] != null) n[k].size++; break;
							case D_R: if(x<w-1 && tiles[(x+1) + y*w].nets != null && tiles[(x+1) + y*w].nets[D_L] != null) n[k].size++; break;
							}
						}
				}
		
		this.nets = nets.toArray(new PipeNetwork[nets.size()]);
		
		for(PipeNetwork pn : nets) {
			if(pn.size == 0) pn.size = 1;
			pn.contents = new Reagents();
			pn.new_contents = new Reagents();
			pn.contents.capacity = 10 * pn.tiles.size();
			
			for(Tile t : pn.tiles)
				if(t instanceof TankTile)
					pn.contents.capacity += 100;
			
			pn.new_contents.capacity = pn.contents.capacity;
		}
	}
	
	private PipeNetwork[] nets;
	
	// returns true if leaking
	private boolean mergeNets(int x, int y, int dir, int x2, int y2) {
		if(!validCoords(x2, y2))
			return true;
		
		PipeNetwork[] n = tiles[x2 + y2*w].nets;
		if(n == null)
			return true;
		
		if(n[dir^1] == null)
			return true;
		
		PipeNetwork t_set = tiles[x + y*w].nets[dir].getRoot();
		PipeNetwork o_set = n[dir^1].getRoot();
		if(t_set != o_set)
			o_set.parent = t_set;
		
		return false;
	}
	
	public boolean validCoords(int x, int y) {
		return x >= 0 && y >= 0 && x < w && y < h;
	}

	public void tick() {
		int i = 0;
		for(int y = 0; y < h; y++)
		for(int x = 0; x < w; x++, i++) {
			tiles[i].tick();
		}
		
		for(PipeNetwork n : nets) {
			/*if(n.leak) {
				Reagents transfer = n.new_contents.getFraction(0.5f);
				n.new_contents.remove(transfer);
				leaked_reagents.add(transfer);
				
				String s = transfer.toString();
				if(!s.equals(""))
					System.out.println("leaked "+s);
			}*/
			n.new_contents.copyTo(n.contents);
		}
	}

	public void setTile(int x, int y, Tile tile) {
		if(validCoords(x, y))
			tiles[x + y*w] = tile;
	}
}
