package com.immibis.fluffyjam1;

import java.util.HashSet;
import java.util.Set;

public final class Guts {
	private static String DEFAULT_SETUP_STR =
		"     N  M           "+
		"     |  |           "+
		"  /--L--+-----A     "+
		"  |     A-----+--A  "+
		"  |           |  |  "+
		"  A-----H-----/  |  "+
		"                 |  "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    ";
	
	public static final int W = 20, H = 15;
	
	private static void parse(String s, Tile[] rv) {
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
			case 'A': rv[k] = new PipeCrossTile(DM_L | DM_D, DM_U | DM_R); break;
			case '/': rv[k] = new PipeCrossTile(DM_L | DM_U, DM_D | DM_R); break;
			case '+': rv[k] = new PipeCrossTile(DM_L | DM_R, DM_U | DM_D); break;
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
			nets[D_D].new_contents.addRespectingCapacity(Reagent.R_FOOD, 5.0f);
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
	}
	
	public static class HeartTile extends Tile {
		HeartTile() {
			initNets(DM_L | DM_R);
		}
		@Override
		public void tick() {
			nets[D_L].new_contents.addRespectingCapacity(Reagent.R_BLOOD, 2);
			nets[D_R].new_contents.remove(nets[D_R].contents);
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
					if(n[D_U] != null) mergeNets(x, y, D_U, x, y-1);
					if(n[D_D] != null) mergeNets(x, y, D_D, x, y+1);
					if(n[D_L] != null) mergeNets(x, y, D_L, x-1, y);
					if(n[D_R] != null) mergeNets(x, y, D_R, x+1, y);
				}
		
		Set<PipeNetwork> nets = new HashSet<PipeNetwork>();
		for(int y = 0; y < H; y++)
			for(int x = 0; x < W; x++)
				if(tiles[x + y*W].nets != null) {
					PipeNetwork[] n = tiles[x + y*W].nets;
					for(int k = 0; k < 4; k++)
						if(n[k] != null) {
							nets.add(n[k] = n[k].getRoot());
							n[k].size++;
						}
				}
		
		this.nets = nets.toArray(new PipeNetwork[nets.size()]);
		
		for(PipeNetwork pn : nets) {
			pn.contents = new Reagents();
			pn.new_contents = new Reagents();
			pn.contents.capacity = pn.new_contents.capacity = 30 * pn.size; 
		}
	}
	
	private PipeNetwork[] nets;
	
	private void mergeNets(int x, int y, int dir, int x2, int y2) {
		if(!validCoords(x2, y2)) return;
		
		PipeNetwork[] n = tiles[x2 + y2*W].nets;
		if(n == null) return;
		
		if(n[dir^1] == null) return;
		
		PipeNetwork t_set = tiles[x + y*W].nets[dir].getRoot();
		PipeNetwork o_set = n[dir^1].getRoot();
		if(t_set != o_set)
			o_set.parent = t_set;
	}
	
	private boolean validCoords(int x, int y) {
		return x >= 0 && y >= 0 && x < W && y < H;
	}

	public void tick() {
		int i = 0;
		for(int y = 0; y < H; y++)
		for(int x = 0; x < W; x++, i++) {
			tiles[i].tick();
		}
		
		for(PipeNetwork n : nets)
			n.new_contents.copyTo(n.contents);
	}
}
