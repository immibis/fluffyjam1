package com.immibis.fluffyjam1;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.BlockFluidBase;

public class OneTile extends TileEntity {
	
	private int x, y = Integer.MIN_VALUE, z;
	private boolean full, sleeping = true;
	
	Reagents my_buffer = new Reagents();
	{my_buffer.capacity = 1500 * Reagent.COUNT;}
	
	private boolean isValid(int x, int y, int z) {
		if(!worldObj.blockExists(x, y, z))
			return false;
		if(worldObj.isAirBlock(x, y, z))
			return true;
		if(worldObj.getBlockId(x, y, z) == FluffyJam1Mod.blockF_u.blockID && worldObj.getBlockMetadata(x, y, z) != 0)
			return true;
		return false;
	}
	
	private boolean randomStep() {
		while(isValid(x, y-1, z))
			y--;
		
		int mask = 0;
		if(x <= xCoord && isValid(x-1, y, z)) mask |= 1;
		if(x >= xCoord && isValid(x+1, y, z)) mask |= 2;
		if(z <= zCoord && isValid(x, y, z-1)) mask |= 4;
		if(z >= zCoord && isValid(x, y, z+1)) mask |= 8;
		
		if(mask == 0)
			return false;
		
		int which = worldObj.rand.nextInt(Integer.bitCount(mask));
		for(int k = 0; k < which; k++)
			mask &= mask-1; // clear lowest set bit
		switch(Integer.numberOfTrailingZeros(mask)) {
		case 0: x--; break;
		case 1: x++; break;
		case 2: z--; break;
		case 3: z++; break;
		}
		
		return true;
	}
	
	private void scanBlocks() {
		x = xCoord;
		y = yCoord-1;
		z = zCoord;
		
		for(int k = 0; k < 20 && randomStep(); k++)
			;
		
		if(!isValid(x, y, z))
			full = true;
	}
	
	public void updateEntity() {
		if(full)
			return;
		
		if(!isValid(x, y, z) || y == Integer.MIN_VALUE)
			scanBlocks();
		
		if(sleeping)
			return;
		
		for(int k = 0; k < Reagent.COUNT; k++) {
			if(my_buffer.get(k) >= FluffyJam1Mod.REAGENT_PER_BLOCK) {
				Block b = getBlockFor(k);
				if(b != null)
					worldObj.setBlock(x, y, z, b.blockID);
				//System.out.println(x+" "+y+" "+z);
				my_buffer.remove(k, FluffyJam1Mod.REAGENT_PER_BLOCK);
				return;
			}
		}
		
		sleeping = true;
	}

	public void accept(Reagents r) {
		if(full || !isValid(x, y, z))
			return;
		
		r.convertAll(Reagent.R_FOOD, Reagent.R_STOOL);
		
		//if(r.get(Reagent.R_WATER) < r.get(Reagent.R_MWASTE)*1.5f)
			r.convertAll(Reagent.R_WATER, Reagent.R_MWASTE);
		
		r.pourInto(my_buffer);
		sleeping = false;
	}
	
	private Block getBlockFor(int id) {
		switch(id) {
		case Reagent.R_BLOOD: return null;
		case Reagent.R_MWASTE: return FluffyJam1Mod.blockF_u;
		case Reagent.R_OXYGEN: return null;
		case Reagent.R_STOOL: return FluffyJam1Mod.blockSludge;
		//case Reagent.R_WATER: return Block.waterMoving;
		}
		System.err.println("getBlockFor unexpected reagent "+id);
		return null;
	}
}
