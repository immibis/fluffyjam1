package com.immibis.fluffyjam1;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Guts implements Serializable {
	private static final long serialVersionUID = 1L;
	
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
		"     X           |  "+
		"     |           T  "+
		"     |           |  "+
		"     1           X  "+
		"                 |  "+
		"                 2  ";
	
	public static final int W = 20, H = 15;
	
	public transient GutsListener listener;
	
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
			case '1': rv[k] = new OrificeTile(1); break;
			case '2': rv[k] = new OrificeTile(2); break;
			case 'X': rv[k] = new ValveTile(); break;
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
	public static final float MAX_BLOOD_WATER = 1.0f; // target ratio of water:blood - kidneys will excrete excess 
	
	// energy units
	public static final float METAB_RATE_BRAIN = 0.03f;
	public static final float METAB_RATE_HEART = 0.005f;
	
	
	// The amount of oxygen and food used, and mwaste produced, per energy unit (in mL/EU)
	public static final float BASE_METABOLIC_OXYGEN_RATIO = 1;
	public static final float BASE_METABOLIC_FOOD_RATIO = 1;
	public static final float BASE_METABOLIC_WASTE_RATIO = 1;
	
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
			initNets(DM_D);
		}
		
		int ticks = 0;
		
		@Override
		public void tick() {
			//if((++ticks) % 100 == 0) {
			//	nets[D_D].new_contents.set(Reagent.R_FOOD, 80.0f);
			//	nets[D_D].new_contents.set(Reagent.R_WATER, 30.0f);
			//}
			if(mouthBuffer.getTotal() > 0)
				mouthBuffer = mouthBuffer;
			mouthBuffer.pourInto(nets[D_D].new_contents);
		}
	}
	
	public static class NoseTile extends Tile {
		private static final long serialVersionUID = 1L;
		
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
		private static final long serialVersionUID = 1L;
		
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
			
			if(nets[D_R].new_contents.getTotal() < nets[D_R].new_contents.capacity * TARGET_BLOOD_PRESSURE)
				nets[D_R].new_contents.addRespectingCapacity(Reagent.R_BLOOD, 8); // TODO this was 2
		}
	}
	
	public class KidneyTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		KidneyTile() {
			initNets(DM_L | DM_R | DM_D);
		}
		
		@Override
		public void tick() {
			float flow = calcFlow(nets[D_L], nets[D_R]);
			
			checkCap(nets[D_L]);
			checkCap(nets[D_R]);
			
			if(flow > 0) {
				doKidneyStuff(nets[D_L], nets[D_R], flow);
			} else {
				doKidneyStuff(nets[D_R], nets[D_L], -flow);
			}
		}
		
		private void checkCap(PipeNetwork pn) {
			if(pn.contents.getTotal() > pn.contents.capacity * 0.9f) {
				Reagents remove = pn.contents.getVolume(pn.contents.getTotal() - pn.contents.capacity * 0.9f);
				pn.new_contents.remove(remove);
				remove.set(Reagent.R_BLOOD, 0);
				remove.pourInto(nets[D_D].new_contents);
				pn.new_contents.add(remove);
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
				
				drain.new_contents.add(Reagent.R_URINE, water_removed + mwaste_removed);
			}
			
			//System.out.print(transfer+" --- ");
			transfer.pourInto(to.new_contents);
			//System.out.println(transfer);
			//from.new_contents.add(transfer);
			transfer.pourInto(from.new_contents);
		}
	}
	
	public class ValveTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		ValveTile() {
			initNet(DM_U | DM_L);
			initNet(DM_D | DM_R);
		}
		
		boolean open = false;
		
		@Override
		public void tick() {
			if(open) {
				if(nets[D_U].new_contents.getTotal() < 3)
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
		
		int number;
		
		OrificeTile(int number) {
			initNet(DM_U | DM_D | DM_L | DM_R);
			this.number = number;
		}
		
		//boolean open = false;
		
		@Override
		public void tick() {
			PipeNetwork pn = nets[D_U];
			
			//if(pn.contents.getTotal() > pn.contents.capacity * 0.9f)
			//	open = true;
			//if(pn.contents.getTotal() < pn.contents.capacity * 0.1f)
			//	open = false;
			
			//if(open) {
				Reagents transfer = pn.new_contents.getFraction(1);
				pn.new_contents.remove(transfer);
				if(listener != null)
					listener.eject(transfer, this.number);
			//}
		}
	}
	
	public static class IntestineTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		IntestineTile() {
			initNets(DM_U | DM_D);
			initNet(DM_L | DM_R);
		}
		
		@Override
		public void tick() {
			float flow = calcFlow(nets[D_U], nets[D_D]);
			
			flow = Math.max(Math.min(flow, 1), 0.1f);
			
			Reagents blood = nets[D_L].contents, new_blood = nets[D_L].new_contents;
			//float max_food_transfer = new_blood.getDissolveSpace(Reagent.R_FOOD);
			
			Reagents transfer = nets[D_U].contents.getVolume(flow);
			
			//if(transfer.get(Reagent.R_FOOD) > max_food_transfer)
			//	transfer.set(Reagent.R_FOOD, max_food_transfer);
			
			nets[D_U].new_contents.remove(transfer);
			
			float avail_food = transfer.get(Reagent.R_FOOD);
			float used_food = avail_food * 0.3f;
			float leftover_food = used_food - new_blood.dissolve(Reagent.R_FOOD, used_food);
			transfer.remove(Reagent.R_FOOD, used_food);
			transfer.add(Reagent.R_STOOL, used_food*1.2f + leftover_food);
			
			float avail_water = transfer.get(Reagent.R_WATER);
			if(avail_water > 0)
				transfer.remove(Reagent.R_WATER, new_blood.addRespectingCapacity(Reagent.R_WATER, avail_water * 0.8f));
			
			
			
			transfer.pourInto(nets[D_D].new_contents);
			transfer.pourInto(nets[D_L].new_contents);
			transfer.pourInto(nets[D_U].new_contents);
		}
	}
	
	// 0 = dead, 0-0.5 = coma, 0.5-1 = various symptoms, 0 = normal
	public float brain_function;
	
	// 0 = empty, 1 = full
	public float bladder, poop;
	
	public class BrainTile extends Tile {
		private static final long serialVersionUID = 1L;
		
		BrainTile() {
			initNets(DM_D);
		}
		
		@Override
		public void tick() {
			PipeNetwork blood = nets[D_D];
			
			float energy = metabolize(blood.contents, blood.new_contents, METAB_RATE_BRAIN, 1);
			float mwaste_pct = blood.contents.get(Reagent.R_MWASTE) / blood.contents.get(Reagent.R_BLOOD);
			float stool_pct = blood.contents.get(Reagent.R_STOOL) / blood.contents.get(Reagent.R_BLOOD);

			// normal: <10, coma: 100+, death: 200+
			float toxin_rel = (blood.contents.get(Reagent.R_MWASTE) + blood.contents.get(Reagent.R_URINE)*2 + blood.contents.get(Reagent.R_STOOL)*5) / blood.contents.get(Reagent.R_BLOOD) * 70;
			
			if(toxin_rel < 10)
				brain_function = 1;
			else if(toxin_rel < 200)
				brain_function = 1 - toxin_rel / 200;
			else
				brain_function = 0;
			
			float energy_avail_pct = energy / METAB_RATE_BRAIN;
			if(energy_avail_pct < 0.05f)
				brain_function = Math.min(brain_function, 0.6f);
		}
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
		
		Reagents r = new Reagents();
		PipeNetwork internal_net = new PipeNetwork();
		public TankTile() {
			initNet(DM_L | DM_U);
			initNet(DM_D | DM_R);
			r.capacity = 1000;
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
			float pct_full = r.getTotal() / r.capacity;
			float flow = calcFlow(internal_net, out) + 10;
			
			transfer = r.getVolume(flow);
			
			if(r.get(Reagent.R_STOOL) > 0)
				poop = r.getTotal() / r.capacity;
			if(r.get(Reagent.R_URINE) > 0)
				bladder = r.getTotal() / r.capacity;
		}
		
		@Override
		public List<String> describe() {
			return r.describe();
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
		
		// used when building networks - disjoint set data structure
		PipeNetwork parent;
		PipeNetwork getRoot() {if(parent != null) return parent = parent.getRoot(); else return this;}
	}
	
	Tile[] tiles = new Tile[W*H];
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
							// increment n[k].size if this tile-side is actually connected to another tile-side
							switch(k) {
							case D_U: if(y > 0 && tiles[x + (y-1)*W].nets != null && tiles[x + (y-1)*W].nets[D_D] != null) n[k].size++; break;
							case D_D: if(y<H-1 && tiles[x + (y+1)*W].nets != null && tiles[x + (y+1)*W].nets[D_U] != null) n[k].size++; break;
							case D_L: if(x > 0 && tiles[(x-1) + y*W].nets != null && tiles[(x-1) + y*W].nets[D_R] != null) n[k].size++; break;
							case D_R: if(x<W-1 && tiles[(x+1) + y*W].nets != null && tiles[(x+1) + y*W].nets[D_L] != null) n[k].size++; break;
							}
						}
				}
		
		this.nets = nets.toArray(new PipeNetwork[nets.size()]);
		
		for(PipeNetwork pn : nets) {
			if(pn.size == 0) pn.size = 1;
			pn.contents = new Reagents();
			pn.new_contents = new Reagents();
			pn.contents.capacity = pn.new_contents.capacity = 30 * pn.size; 
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
