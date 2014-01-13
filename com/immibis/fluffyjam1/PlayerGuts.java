package com.immibis.fluffyjam1;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerGuts implements IExtendedEntityProperties {

	public static final String EXT_PROP_ID = "FJ1IMBGT";
	
	public EntityPlayerMP player;
	public Guts data;
	
	@Override
	public void saveNBTData(NBTTagCompound compound) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(Entity entity, World world) {
		player = (EntityPlayerMP)entity;
		data = new Guts();
	}

	public void tick() {
		data.tick();
	}
	
	public static PlayerGuts get(EntityPlayerMP pl) {
		return (PlayerGuts)pl.getExtendedProperties(EXT_PROP_ID);
	}

}
