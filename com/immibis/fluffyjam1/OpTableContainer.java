package com.immibis.fluffyjam1;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
	
	static class DrawData implements Serializable {
		private static final long serialVersionUID = 1;
		
		boolean removeMode;
		boolean[][] map;
	}
	
	static class MoveData implements Serializable {
		private static final long serialVersionUID = 1;
		int fx, fy, tx, ty;
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
	
	public void sendToPlayer(Object o, Player p) {
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = CHANNEL;
		packet.data = IOUtils.toBytes(o);
		packet.length = packet.data.length;
		PacketDispatcher.sendPacketToPlayer(packet, p);
	}
	
	@SideOnly(Side.CLIENT)
	public void sendToServer(Object o) {
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = CHANNEL;
		packet.data = IOUtils.toBytes(o);
		packet.length = packet.data.length;
		PacketDispatcher.sendPacketToServer(packet);
	}
	
	@Override
	public void addCraftingToCrafters(ICrafting par1iCrafting) {
		super.addCraftingToCrafters(par1iCrafting);
		if(par1iCrafting instanceof EntityPlayerMP) {
			sendToPlayer(PlayerExtData.get((EntityPlayerMP)operee).data, (Player)par1iCrafting);
		}
	}
	
	public void receiveObject(Object o) {
		if(o instanceof Guts)
			guts = (Guts)o;
		else if(o instanceof DrawData) {
			DrawData dd = (DrawData)o;
			PlayerExtData.get((EntityPlayerMP)operee).data.finishDrawingPipes(dd.map, dd.removeMode);
		} else if(o instanceof MoveData) {
			MoveData md = (MoveData)o;
			PlayerExtData.get((EntityPlayerMP)operee).data.moveTile(md.fx, md.fy, md.tx, md.ty);
		}
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
