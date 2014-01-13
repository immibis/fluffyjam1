package com.immibis.fluffyjam1;

import java.util.EnumSet;

import com.jcraft.jorbis.Block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid="immibis_fj1", name="Immibis Fluffy Jam 1 Untitled Mod", version="1.0")
@NetworkMod(clientSideRequired=true, serverSideRequired=false)
public class FluffyJam1Mod implements IGuiHandler {
	public static OpTableBlock blockOT;
	
	public static int GUI_OP_TABLE = 1;
	
	
	public static final boolean SELF_OP_MODE = true; // if true, players operate on themselves, for SSP testing
	
	@Instance("immibis_fj1")
	public static FluffyJam1Mod INSTANCE;
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		blockOT = new OpTableBlock(2200);
		
		NetworkRegistry.instance().registerGuiHandler(this, this);
		NetworkRegistry.instance().registerChannel(new OpTableContainer.PacketHandler(), OpTableContainer.CHANNEL);
		
		TickRegistry.registerTickHandler(new ITickHandler() {
			
			@Override
			public EnumSet<TickType> ticks() {
				return EnumSet.of(TickType.PLAYER);
			}
			
			@Override
			public void tickStart(EnumSet<TickType> type, Object... tickData) {
				EntityPlayer pl = (EntityPlayer)tickData[0];
				if(pl.sleepTimer >= 98 && pl.isPlayerSleeping()) {
					ChunkCoordinates bed = pl.playerLocation;
					if(bed != null && !pl.worldObj.blockExists(bed.posX, bed.posY, bed.posZ) || pl.worldObj.getBlockId(bed.posX, bed.posY, bed.posZ) == blockOT.blockID) {
						// players in operating tables aren't actually sleeping
						// so don't let them become "fully asleep" at which point it would otherwise become day.
						// players are fully asleep once sleepTimer reaches 100.
						pl.sleepTimer = 98;
					}
				}
				
				PlayerGuts.get((EntityPlayerMP)pl).tick();
			}
			
			@Override
			public void tickEnd(EnumSet<TickType> type, Object... tickData) {
			}
			
			@Override
			public String getLabel() {
				return "immibis_fj3";
			}
		}, Side.SERVER);
		
		TickRegistry.registerTickHandler(new ITickHandler() {
			
			@Override
			public EnumSet<TickType> ticks() {
				return EnumSet.of(TickType.PLAYER);
			}
			
			@Override
			public void tickStart(EnumSet<TickType> type, Object... tickData) {
				EntityPlayer pl = (EntityPlayer)tickData[0];
				if(pl.openContainer instanceof OpTableContainer)
					((OpTableContainer)pl.openContainer).clientTick();
			}
			
			@Override
			public void tickEnd(EnumSet<TickType> type, Object... tickData) {
			}
			
			@Override
			public String getLabel() {
				return "immibis_fj3";
			}
		}, Side.CLIENT);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@ForgeSubscribe
	public void onEntityConstruct(EntityEvent.EntityConstructing evt) {
		if(evt.entity instanceof EntityPlayerMP) {
			evt.entity.registerExtendedProperties(PlayerGuts.EXT_PROP_ID, new PlayerGuts());
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == GUI_OP_TABLE)
			return new OpTableGUI(new OpTableContainer(player, world, x, y, z));
		return null;
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == GUI_OP_TABLE)
			return new OpTableContainer(player, world, x, y, z);
		return null;
	}
}
