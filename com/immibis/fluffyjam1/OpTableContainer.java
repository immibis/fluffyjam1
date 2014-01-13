package com.immibis.fluffyjam1;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
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
	
	public static final String CHANNEL = "FJ1IMBOTC";
	
	static class PacketHandler implements IPacketHandler {
		@Override
		public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
			Object o = IOUtils.fromBytes(packet.data);
			Container c_ = ((EntityPlayer)player).openContainer;
			if(c_ instanceof OpTableContainer)
				((OpTableContainer)c_).receiveObject(o);
		}
	}
	
	@Override
	public void addCraftingToCrafters(ICrafting par1iCrafting) {
		super.addCraftingToCrafters(par1iCrafting);
		if(par1iCrafting instanceof EntityPlayerMP) {
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = CHANNEL;
			packet.data = IOUtils.toBytes(PlayerGuts.get((EntityPlayerMP)operee).data);
			packet.length = packet.data.length;
			PacketDispatcher.sendPacketToPlayer(packet, (Player)par1iCrafting);
		}
	}
	
	public void receiveObject(Object o) {
		if(o instanceof Guts)
			guts = (Guts)o;
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
