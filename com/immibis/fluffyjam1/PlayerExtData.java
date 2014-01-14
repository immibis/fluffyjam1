package com.immibis.fluffyjam1;

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
	
	private class FakeFoodStats extends FoodStats {
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
		data.tick();
		
		player.playerNetServerHandler.sendPacketToPlayer(FluffyJam1Mod.TinyPacketHandler.getBrainFunctionPacket(data.brain_function, data.bladder, data.poop, data.food_level, data.water_level));
		player.setAir((int)(300 * data.oxygen_level));
		
		data.drowning = player.isInsideOfMaterial(Material.water);
	}
	
	Reagents buffer1 = new Reagents(), buffer2 = new Reagents();

	
	@Override
	public void eject(Reagents r, int where) {
		if(where == 1) {
			buffer1.add(r);
			
			while(buffer1.getTotal() > 100) {
				Reagents dropped = buffer1.getVolume(100);
				buffer1.remove(dropped);
				dropLiquid(dropped);
			}
		
		} else if(where == 2) {
			buffer2.add(r);
			while(buffer2.getTotal() >= 100) {
				Reagents dropped = buffer2.getVolume(100);
				buffer2.remove(dropped);
				dropSolid(dropped);
			}
			
		} else
			throw new RuntimeException("unknown eject-location "+where);
		
		
	}
	
	private void dropLiquid(Reagents dropped) {
		if(dropped.get(Reagent.R_MWASTE) == 0)
			dropLiquid(Block.waterStill);
		else
			dropLiquid(FluffyJam1Mod.blockF_u);
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
			if(t instanceof Guts.ValveTile) {
				Reagents r = t.nets[Guts.D_U].contents;
				if(r.get(Reagent.R_STOOL) > 0 && i == 2) ((Guts.ValveTile)t).open = true;
				if(r.get(Reagent.R_MWASTE) > 0 && i == 1) ((Guts.ValveTile)t).open = true;
			}
	}

	

}
