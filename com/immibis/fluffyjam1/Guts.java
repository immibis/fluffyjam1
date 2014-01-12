package com.immibis.fluffyjam1;

public final class Guts {
	private static String DEFAULT_SETUP_STR =
		"        M           "+
		"        |           "+
		"        |           "+
		"        |           "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    "+
		"                    ";
	
	public static final int W = 20, H = 15;
	
	private static GutTile[] parse(String s) {
		GutTile[] rv = new GutTile[W*H];
		if(s.length() != W*H)
			throw new AssertionError("expected string length "+(W*H)+" = "+W+"*"+H+", but got "+s.length());
		
		for(int k = 0; k < W*H; k++) {
			switch(s.charAt(k)) {
			case ' ': rv[k] = GutTile.EMPTY; break;
			case 'M': rv[k] = GutTile.MOUTH; break;
			case '|': rv[k] = GutTile.TUBE_V; break;
			case '-': rv[k] = GutTile.TUBE_H; break;
			default:
				throw new AssertionError("invalid char "+s.charAt(k));
			}
		}
		
		return rv;
	}
	
	private GutTile[] tiles;
	private Reagents[] reagents;
	private Reagents[] added_reagents;
	private Reagents leaked_reagents;
	
	{
		tiles = new GutTile[W*H];
		reagents = new Reagents[W*H];
		added_reagents = new Reagents[W*H];
		leaked_reagents = new Reagents();
		leaked_reagents.capacity = Float.POSITIVE_INFINITY;
		
		GutTile[] def = parse(DEFAULT_SETUP_STR);
		for(int y = 0; y < H; y++)
			for(int x = 0; x < W; x++)
				setTile(x, y, def[x+y*W]);
	}
	
	public GutTile getTile(int x, int y) {
		GutTile t = tiles[x + y*W];
		assert t != null : "tile is null at "+x+","+y;
		return t;
	}
	
	public void setTile(int x, int y, GutTile tile) {
		if(tile == null) throw new NullPointerException("tile is null");
		if(x < 0 || y < 0 || x >= W || y >= H) throw new IndexOutOfBoundsException(x+","+y);
		
		int i = x + y*W;
		
		tiles[i] = tile;
		
		if(tile == GutTile.EMPTY)
			reagents[i] = null;
		else {
			if(reagents[i] == null) {
				reagents[i] = new Reagents();
				reagents[i].capacity = 20;
				added_reagents[i] = new Reagents();
				added_reagents[i].capacity = 20;
			}
		}
	}
	
	public void tick() {
		int i = 0;
		for(int y = 0; y < H; y++)
		for(int x = 0; x < W; x++, i++) {
			switch(tiles[i]) {
			case MOUTH:
				added_reagents[i].set(Reagent.R_FOOD, 5);
				break;
			case TUBE_V:
				if(y > 0 && reagents[i-W] != null && tiles[i-W] != GutTile.TUBE_V)
					reagents[i-W].pourInto(added_reagents[i]);
				reagents[i].pourInto(y < H-1 && reagents[i+W] != null ? added_reagents[i+W] : leaked_reagents);
				break;
			}
		}
		
		for(int k = W*H-1; k >= 0; k--)
			if(reagents[k] != null)
				added_reagents[k].pourInto(reagents[k], 1);
	}

	public Reagents getReagents(int x, int y) {
		return reagents[x+y*W];
	}
}
