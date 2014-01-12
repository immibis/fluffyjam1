package com.immibis.fluffyjam1;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

public class OpTableContainer extends Container {
	
	private World world;
	private int x, y, z;
	private EntityPlayer operee;
	private EntityPlayer operator;
	
	Guts guts = new Guts();
	
	void clientTick() {
		guts.tick();
	}
	
	public OpTableContainer(EntityPlayer operator, World world, int x, int y, int z) {
		if(!world.isRemote) {
			this.operator = operator;
			this.operee = OpTableBlock.getPlayerInBed(world, x, y, z);
		}
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		if(entityplayer.getDistanceSq(x+0.5, y+0.5, z+0.5) >= 64)
			return false;
		
		if(world.isRemote)
			return true;
		
		if(operee == null || operee.isDead || operee.getDistanceSq(x+0.5, y+0.5, z+0.5) >= 64)
			return false;
		
		if(entityplayer != operator)
			return false;
		
		return true;
	}

}
