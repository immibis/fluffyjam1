package com.immibis.fluffyjam1;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Guts {
	private static String DEFAULT_SETUP_STR =
		"     N  M           "+
		"     |  A----T---A  "+
		"  /--L----------A|  "+
		"  |       B     >I  "+
		"  |       |     >I  "+
		"  A--K--H-^--A  >I  "+
		"     |       |  >I  "+
		"     T       |  >I  "+
		"     |       A--/|  "+
		"     T           |  "+
		"     |           T  "+
		"     T           |  "+
		"                 T  "+
		"                 |  "+
		"                 T  ";
	
	public static final int W = 20, H = 15;
	
	private void parse(String s, Tile[] rv) {
		if(s.length() != rv.length)
			throw new AssertionError(s.length()+" "+rv.length);
		
		for(int k = 0; k < s.length(); k++) {
			switch(s.charAt(k)) {
			case ' ': rv[k] = EmptyTile.instance; break;
			case 'M': rv[k] = new MouthTile(); break;
			case 'N': rv[k] = new NoseTile(); break;
			case 'H': rv[k] = new HeartTile(); break;
			case 'L': rv[k] = new LungTile(); break;
			case '|': rv[k] = new PipeTile(DM_U | DM_D); break;
			case '-': rv[k] = new PipeTile(DM_L | DM_R); break;
			case 'I': rv[k] = new IntestineTile(); break;
			case '>': rv[k] = new PipeTile(DM_R | DM_U | DM_D); break;
			case '^': rv[k] = new PipeTile(DM_R | DM_U | DM_L); break;
			case '<': rv[k] = new PipeTile(DM_L | DM_U | DM_D); break;
			case 'A': rv[k] = new PipeCrossTile(DM_L | DM_D, DM_U | DM_R); break;
			case '/': rv[k] = new PipeCrossTile(DM_L | DM_U, DM_D | DM_R); break;
			case '+': rv[k] = new PipeCrossTile(DM_L | DM_R, DM_U | DM_D); break;
			case 'T': rv[k] = new TankTile(); break;
			case 'B': rv[k] = new BrainTile(); break;
			case 'K': rv[k] = new KidneyTile(); break;
			default:
				throw new AssertionError("invalid char "+s.charAt(k));
			}
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
	public static final float TARGET_BLOOD_WATER = 0.3f;
	
	public static class Tile {
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
		
		// The amount of oxygen and food used, and mwaste produced, per energy unit
		public static final float BASE_METABOLIC_OXYGEN_RATIO = 1;
		public static final float BASE_METABOLIC_FOOD_RATIO = 1;
		public static final float BASE_METABOLIC_WASTE_RATIO = 1;
		
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
		static EmptyTile instance = new EmptyTile();
	}
	
	public static class MouthTile extends Tile {
		MouthTile() {
			initNets(DM_D);
		}
		
		@Override
		public void tick() {
			if(Math.random() < 0.01) {
				nets[D_D].new_contents.addRespectingCapacity(Reagent.R_FOOD, 50.0f);
				nets[D_D].new_contents.addRespectingCapacity(Reagent.R_WATER, 5000.0f);
			}
		}
	}
	
	public static class NoseTile extends Tile {
		NoseTile() {
			initNets(DM_D);
		}
		
		@Override
		public void tick() {
			
			// TODO drowning
			
			Reagents to_remove = nets[D_D].contents.getFraction(0.05f);
			nets[D_D].new_contents.remove(to_remove);
			
			nets[D_D].new_contents.addRespectingCapacity(Reagent.R_OXYGEN, 3);
		}
	}
	
	public static class LungTile extends Tile {
		LungTile() {
			initNets(DM_U | DM_L | DM_R);
		}
		
		@Override
		public void tick() {
			if(nets[D_U].contents.get(Reagent.R_WATER) > 0.1f)
				return; // lungs don't function if you're breathing water (aka drowning)
						// TODO this is broken because it doesn't even flow
			
			if(nets[D_U].contents.get(Reagent.R_URINE) > 0.1f)
				return; // !!!???
			
			float flow = calcFlow(nets[D_L], nets[D_R]);
			
			if(flow <= 0)
				return; // nothing to do
			
			Reagents transfer = nets[D_L].contents.getVolume(flow);
			nets[D_L].new_contents.remove(transfer);
			
			// oxygen + blood -> blood_ox
			float avail_oxy = nets[D_U].contents.get(Reagent.R_OXYGEN);
			nets[D_U].new_contents.remove(Reagent.R_OXYGEN, transfer.dissolve(Reagent.R_OXYGEN, avail_oxy));
			
			transfer.pourInto(nets[D_R].new_contents);
			transfer.pourInto(nets[D_L].new_contents);
		}
	}
	
	public static class HeartTile extends Tile {
		HeartTile() {
			initNets(DM_L | DM_R);
		}
		@Override
		public void tick() {
			float flow = Math.max(3, calcFlow(nets[D_R], nets[D_L]));
			Reagents transfer = nets[D_R].contents.getVolume(flow);
			nets[D_R].new_contents.remove(transfer);
			
			float energy = metabolize(transfer, transfer, 1, 1);
			
			transfer.pourInto(nets[D_L].new_contents);
			transfer.pourInto(nets[D_R].new_contents);
			
			if(nets[D_L].new_contents.getTotal() < nets[D_L].new_contents.capacity * TARGET_BLOOD_PRESSURE)
				nets[D_L].new_contents.addRespectingCapacity(Reagent.R_BLOOD, 8); // TODO this was 2
		}
	}
	
	public class KidneyTile extends Tile {
		KidneyTile() {
			initNets(DM_L | DM_R | DM_D);
		}
		
		@Override
		public void tick() {
			float flow = calcFlow(nets[D_L], nets[D_R]);
			
			if(flow > 0) {
				doKidneyStuff(nets[D_L], nets[D_R], flow);
			} else {
				doKidneyStuff(nets[D_R], nets[D_L], -flow);
			}
		}
		
		private void doKidneyStuff(PipeNetwork from, PipeNetwork to, float flow) {
			
			Reagents transfer = from.contents.getVolume(flow);
			from.new_contents.remove(transfer);
			
			if(transfer.getTotal() < 0.01) {
				from.new_contents.add(transfer);
				return;
			}
			
			PipeNetwork drain = nets[D_D];
			
			final float WATER_TO_MWASTE_RATIO = 1;
			final float EXCESS_WATER_REMOVE_RATE = 0.3f;
			final float MWASTE_REMOVE_RATE = 0.8f;
			
			float transfer_water_pct = transfer.get(Reagent.R_WATER) / transfer.get(Reagent.R_BLOOD);
			float transfer_excess_water = Math.max(0, transfer.get(Reagent.R_WATER) * (transfer_water_pct - TARGET_BLOOD_WATER)); 
			float water_removed = Math.min(transfer.get(Reagent.R_WATER), Math.max(transfer.get(Reagent.R_MWASTE) * MWASTE_REMOVE_RATE * WATER_TO_MWASTE_RATIO, transfer_excess_water * EXCESS_WATER_REMOVE_RATE));
			float mwaste_removed = Math.min(transfer.get(Reagent.R_MWASTE), water_removed / WATER_TO_MWASTE_RATIO);
			
			float u_capacity = Math.min(drain.new_contents.getRemainingCapacity(), drain.contents.getRemainingCapacity());
			
			if(u_capacity > 0) {
				float factor = Math.min(1, u_capacity / (water_removed + mwaste_removed));
				water_removed *= factor;
				mwaste_removed *= factor;
				
				water_removed = transfer.remove(Reagent.R_WATER, water_removed);
				mwaste_removed = transfer.remove(Reagent.R_MWASTE, mwaste_removed);
				
				drain.new_contents.add(Reagent.R_URINE, water_removed + mwaste_removed);
			}
			
			Reagents urine = new Reagents();
			urine.add(Reagent.R_URINE, water_removed + mwaste_removed);
			
			transfer.pourInto(to.new_contents);
			from.new_contents.add(transfer);
		}
	}
	
	public static class IntestineTile extends Tile {
		IntestineTile() {
			initNets(DM_U | DM_D);
			initNet(DM_L | DM_R);
		}
		
		@Override
		public void tick() {
			float flow = calcFlow(nets[D_U], nets[D_D]);
			
			flow = Math.max(Math.min(flow, 1), 0.1f);
			
			Reagents transfer = nets[D_U].contents.getVolume(flow);
			nets[D_U].new_contents.remove(transfer);
			
			float avail_food = transfer.get(Reagent.R_FOOD);
			Reagents blood = nets[D_L].contents, new_blood = nets[D_L].new_contents;
			float used_food = new_blood.dissolve(Reagent.R_FOOD, avail_food * 0.3f);
			transfer.remove(Reagent.R_FOOD, used_food);
			transfer.add(Reagent.R_STOOL, used_food*1.2f);
			
			float avail_water = transfer.get(Reagent.R_WATER);
			if(avail_water > 0)
				transfer.remove(Reagent.R_WATER, new_blood.add(Reagent.R_WATER, avail_water * 0.5f));
			
			
			
			transfer.pourInto(nets[D_D].new_contents);
			transfer.pourInto(nets[D_U].new_contents);
			transfer.pourInto(nets[D_L].new_contents);
		}
	}
	
	public static class BrainTile extends Tile {
		BrainTile() {
			initNets(DM_D);
		}
		
		@Override
		public void tick() {
			PipeNetwork blood = nets[D_D];
			
			metabolize(blood.contents, blood.new_contents, 100, 1);
		}
	}
	
	public static class PipeTile extends Tile {
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
	
	public static class TankTile extends Tile {
		Reagents r = new Reagents();
		PipeNetwork internal_net = new PipeNetwork();
		public TankTile() {
			initNet(DM_L | DM_U);
			initNet(DM_D | DM_R);
			r.capacity = 2000; 
			internal_net.contents = internal_net.new_contents = r;
		}
		
		@Override
		public void tick() {
			PipeNetwork in = nets[D_L], out = nets[D_R];
			
			// input
			Reagents transfer = in.contents.getFraction(1f);
			in.new_contents.remove(transfer);
			transfer.pourInto(r);
			transfer.pourInto(in.new_contents);
			
			// output
			r.pourInto(out.new_contents);
		}
		
		@Override
		public List<String> describe() {
			return r.describe();
		}
	}

	public static class PipeCrossTile extends Tile {
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
	
	public static class PipeNetwork {
		Reagents contents, new_contents;
		int size; // number of tile-sides in this network
		boolean leak;
		
		// used when building networks - disjoint set data structure
		PipeNetwork parent;
		PipeNetwork getRoot() {if(parent != null) return parent = parent.getRoot(); else return this;}
	}
	
	private Tile[] tiles = new Tile[W*H];
	private Reagents leaked_reagents;
	
	{
		tiles = new Tile[W*H];
		leaked_reagents = new Reagents();
		leaked_reagents.capacity = Float.POSITIVE_INFINITY;
		
		parse(DEFAULT_SETUP_STR, tiles);
		buildNetworks();
	}
	
	public Tile getTile(int x, int y) {
		Tile t = tiles[x + y*W];
		assert t != null : "tile is null at "+x+","+y;
		return t;
	}
	
	private void buildNetworks() {
		for(int y = 0; y < H; y++)
			for(int x = 0; x < W; x++)
				if(tiles[x + y*W].nets != null) {
					PipeNetwork[] n = tiles[x + y*W].nets;
					if(n[D_U] != null) n[D_U].leak |= mergeNets(x, y, D_U, x, y-1);
					if(n[D_D] != null) n[D_D].leak |= mergeNets(x, y, D_D, x, y+1);
					if(n[D_L] != null) n[D_L].leak |= mergeNets(x, y, D_L, x-1, y);
					if(n[D_R] != null) n[D_R].leak |= mergeNets(x, y, D_R, x+1, y);
				}
		
		Set<PipeNetwork> nets = new HashSet<PipeNetwork>();
		for(int y = 0; y < H; y++)
			for(int x = 0; x < W; x++)
				if(tiles[x + y*W].nets != null) {
					PipeNetwork[] n = tiles[x + y*W].nets;
					for(int k = 0; k < 4; k++)
						if(n[k] != null) {
							PipeNetwork pn = n[k];
							nets.add(n[k] = pn.getRoot());
							n[k].leak |= pn.leak;
							n[k].size++;
						}
				}
		
		this.nets = nets.toArray(new PipeNetwork[nets.size()]);
		
		for(PipeNetwork pn : nets) {
			pn.contents = new Reagents();
			pn.new_contents = new Reagents();
			pn.contents.capacity = pn.new_contents.capacity = 60 * pn.size; 
		}
	}
	
	private PipeNetwork[] nets;
	
	// returns true if leaking
	private boolean mergeNets(int x, int y, int dir, int x2, int y2) {
		if(!validCoords(x2, y2))
			return true;
		
		PipeNetwork[] n = tiles[x2 + y2*W].nets;
		if(n == null)
			return true;
		
		if(n[dir^1] == null)
			return true;
		
		PipeNetwork t_set = tiles[x + y*W].nets[dir].getRoot();
		PipeNetwork o_set = n[dir^1].getRoot();
		if(t_set != o_set)
			o_set.parent = t_set;
		
		return false;
	}
	
	public boolean validCoords(int x, int y) {
		return x >= 0 && y >= 0 && x < W && y < H;
	}

	public void tick() {
		int i = 0;
		for(int y = 0; y < H; y++)
		for(int x = 0; x < W; x++, i++) {
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
}
