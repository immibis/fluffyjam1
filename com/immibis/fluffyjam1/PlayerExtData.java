package com.immibis.fluffyjam1;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

public class PlayerExtData implements IExtendedEntityProperties, GutsListener {

	public static final String EXT_PROP_ID = "FJ1IMBGT";
	
	public EntityPlayerMP player;
	public Guts data;
	
	public FakeFoodStats fakeFoodStats = new FakeFoodStats();
	
	
	// each food point gives enough to run idle for this many ticks (in normal configuration)
	public static final int TICKS_PER_FOOD_POINT = 300;
	
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
		
		player.playerNetServerHandler.sendPacketToPlayer(FluffyJam1Mod.TinyPacketHandler.getBrainFunctionPacket(data.brain_function, data.bladder, data.poop));
	}
	
	Reagents buffer1 = new Reagents(), buffer2 = new Reagents();

	
	@Override
	public void eject(Reagents r, int where) {
		if(where == 1) {
			buffer1.add(r);
			for(int k = 0; k < Reagent.COUNT; k++)
				while(buffer1.get(k) > 100) {
					buffer1.remove(k, 100);
					player.sendChatToPlayer(ChatMessageComponent.createFromText("Dropped some "+Reagent.NAME[k]));
				}
		
		} else if(where == 2) {
			buffer2.add(r);
			while(buffer2.getTotal() >= 100) {
				Reagents _this = buffer2.getVolume(100);
				buffer2.remove(_this);
				
				ItemStack is = new ItemStack(FluffyJam1Mod.itemS);
				is.stackTagCompound = new NBTTagCompound();
				is.stackTagCompound.setByteArray("reagents", IOUtils.toBytes(_this));
				EntityItem ei = new EntityItem(player.worldObj, player.posX, player.posY + 0.6, player.posZ, is);
				ei.motionX = 0.4 * MathHelper.sin(-player.renderYawOffset * 0.017453292F - (float)Math.PI);
				ei.motionY = 0;
				ei.motionZ = 0.4 * MathHelper.cos(-player.renderYawOffset * 0.017453292F - (float)Math.PI);
				ei.delayBeforeCanPickup = 40;
				player.worldObj.spawnEntityInWorld(ei);
				player.worldObj.playSoundAtEntity(player, "mob.chicken.plop", 1.0F, (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
			}
			
		} else
			throw new RuntimeException("unknown eject-location "+where);
		
		
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
				if(r.get(Reagent.R_URINE) > 0 && i == 1) ((Guts.ValveTile)t).open = true;
			}
	}

	

}
