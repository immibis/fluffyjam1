package com.immibis.fluffyjam1;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.fluids.BlockFluidBase;

public class PlayerExtData implements IExtendedEntityProperties, GutsListener {

	public static final String EXT_PROP_ID = "FJ1IMBGT";
	
	public EntityPlayerMP player;
	public Guts data;
	
	public FakeFoodStats fakeFoodStats = new FakeFoodStats();
	
	
	// each food point gives enough to run idle for this many ticks (in normal configuration)
	public static final int TICKS_PER_FOOD_POINT = 30 * 20;
	
	public static class ClientFakeFoodStats extends FoodStats {
		@Override public void addExhaustion(float par1) {}
		@Override public void addStats(int par1, float par2) {}
		@Override public void onUpdate(EntityPlayer par1EntityPlayer) {}
		@Override public boolean needFood() {return true;}
	}
	
	private class FakeFoodStats extends FoodStats {
		private int foodLevel, prevFoodLevel;
		@Override
		public void addExhaustion(float par1) {
			// TODO Auto-generated method stub
			super.addExhaustion(par1);
		}
		@Override
		public void addStats(int food, float nourishmentValue) {
			float saturation = food * nourishmentValue * 2.0f;
			data.mouthBuffer.add(Reagent.R_FOOD, (food + saturation) * TICKS_PER_FOOD_POINT * Guts.FOOD_USE_RATE_IDLE);
		}
		@Override
		public void onUpdate(EntityPlayer par1EntityPlayer) {
			prevFoodLevel = foodLevel;
			foodLevel = Math.min(20, (int)(data.fbar * 21));
		}
		@Override
		public int getFoodLevel() {
			return foodLevel;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public int getPrevFoodLevel() {
			return prevFoodLevel;
		}
		@Override
		public boolean needFood() {
			return true;
		}
	}
	
	public void drink(int millibuckets) {
		data.mouthBuffer.add(Reagent.R_WATER, millibuckets);
	}
	
	
	public static PlayerExtData get(EntityPlayerMP pl) {
		return (PlayerExtData)pl.getExtendedProperties(EXT_PROP_ID);
	}
	
	public void tick() {
		data.leg_energy_level = 1;
		data.is_sprinting = player.isSprinting();
		data.tick();
		
		if(player.isSprinting() && data.leg_energy_level < 0.8f) {
			player.setSprinting(false);
			PacketDispatcher.sendPacketToPlayer(FluffyJam1Mod.TinyPacketHandler.getActionPacket(100), (Player)player);
		}
		
		player.playerNetServerHandler.sendPacketToPlayer(FluffyJam1Mod.TinyPacketHandler.getBrainFunctionPacket(data.brain_function, data.ex1bar, data.ex2bar, data.fbar, data.wbar));
		player.setAir((int)(300 * data.oxygen_level));
		
		data.drowning = player.isInsideOfMaterial(Material.water);
	}
	
	Reagents ebuffer = new Reagents();

	
	@Override
	public void eject(Reagents r, int where) {
		if(r.getTotal() == 0)
			return;
		
		ebuffer.add(r);
		
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.posY) - 1;
		int z = MathHelper.floor_double(player.posZ);
		if(player.worldObj.blockExists(x, y, z) && player.worldObj.getBlockId(x, y, z) == FluffyJam1Mod.block1.blockID) {
			((OneTile)player.worldObj.getBlockTileEntity(x, y, z)).accept(ebuffer);
		}
		
		while(ebuffer.getTotal() > 100) {
			Reagents dropped = ebuffer.getVolume(100);
			ebuffer.remove(dropped);
			
			float tl = 0, ts = 0;
			for(int k = 0; k < Reagent.COUNT; k++)
				if(Reagent.IS_LIQUID[k])
					tl += dropped.get(k);
				else
					ts += dropped.get(k);
			
			if(tl > ts)
				dropLiquid(FluffyJam1Mod.blockF_u);
			else
				dropSolid(dropped);
		}
	}
	
	private boolean tryDropLiquid(Block block, World w, int x, int y, int z) {
		if(!w.isAirBlock(x, y, z))
			return false;
		while(y>0 && w.isAirBlock(x, y-1, z))
			y--;
		w.setBlock(x, y, z, block.blockID);
		return true;
	}

	private void dropLiquid(Block block) {
		World w = player.worldObj;
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.posY);
		int z = MathHelper.floor_double(player.posZ);
		if(tryDropLiquid(block, w, x, y, z))
			return;
		for(int k = 0; k < 50; k++) {
			int _x = x + w.rand.nextInt(9) - 4;
			int _y = y + w.rand.nextInt(9) - 4;
			int _z = z + w.rand.nextInt(9) - 4;
			if(tryDropLiquid(block, w, _x, _y, _z))
				return;
		}
		
		for(int dx = -4; dx <= 4; dx++)
			for(int dy = -4; dy <= 4; dy++)
				for(int dz = -4; dz <= 4; dz++)
					if(tryDropLiquid(block, w, x+dx, y+dy, z+dz))
						return;
		
		// overwrite a random block
		w.setBlock(x + w.rand.nextInt(9) - 4, y + w.rand.nextInt(9) - 4, z + w.rand.nextInt(9) - 4, block.blockID);
	}


	private void dropSolid(Reagents r) {
		ItemStack is = new ItemStack(FluffyJam1Mod.itemS);
		is.stackTagCompound = new NBTTagCompound();
		is.stackTagCompound.setByteArray("reagents", IOUtils.toBytes(r));
		EntityItem ei = new EntityItem(player.worldObj, player.posX, player.posY + 0.6, player.posZ, is);
		ei.motionX = 0.4 * MathHelper.sin(-player.renderYawOffset * 0.017453292F - (float)Math.PI);
		ei.motionY = 0;
		ei.motionZ = 0.4 * MathHelper.cos(-player.renderYawOffset * 0.017453292F - (float)Math.PI);
		ei.delayBeforeCanPickup = 40;
		player.worldObj.spawnEntityInWorld(ei);
		player.worldObj.playSoundAtEntity(player, "mob.chicken.plop", 1.0F, (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
	}


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
		data.listener = this;
	}


	public void empty(int i) {
		for(Guts.Tile t : data.tiles)
			if(t instanceof Guts.ValveTile)
				if(i == ((Guts.ValveTile)t).id)
					((Guts.ValveTile)t).open = !((Guts.ValveTile)t).open;
	}

	

}
