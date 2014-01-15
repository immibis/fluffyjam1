package com.immibis.fluffyjam1;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.EnumSet;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.jcraft.jorbis.Block;

import net.minecraft.block.BlockFluid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ImmibisFJ1_ProtectedAccessProxy;
import net.minecraft.item.Item;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidRegistry.FluidRegisterEvent;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid="immibis_fj1", name="Immibis Fluffy Jam 1 Untitled Mod", version="1.0")
@NetworkMod(clientSideRequired=true, serverSideRequired=false, tinyPacketHandler=FluffyJam1Mod.TinyPacketHandler.class)
public class FluffyJam1Mod implements IGuiHandler {
	public static OpTableBlock blockOT;
	public static BlockFluidBase blockF_u, blockF_d;
	public static Item itemS;
	
	public static int GUI_OP_TABLE = 1;
	
	public static Fluid f_u, f_d;
	
	public static final boolean SELF_OP_MODE = true; // if true, players operate on themselves, for SSP testing
	
	@Instance("immibis_fj1")
	public static FluffyJam1Mod INSTANCE;
	
	public static float clientBrainFunction;
	
	public static class TinyPacketHandler implements ITinyPacketHandler {
		@Override
		public void handle(NetHandler handler, Packet131MapData p) {
			if(p.uniqueID == 0) {
				Bar.bf.value = clientBrainFunction = (p.itemData[0] & 255) / 255f;
				Bar.ex1.value = (p.itemData[1] & 255) / 255f;
				Bar.ex2.value = (p.itemData[2] & 255) / 255f;
				Bar.f.value = (p.itemData[3] & 255) / 255f;
				Bar.w.value = (p.itemData[4] & 255) / 255f;
			}
			if(p.uniqueID == 1)
				PlayerExtData.get(((NetServerHandler)handler).playerEntity).empty(1);
			if(p.uniqueID == 2)
				PlayerExtData.get(((NetServerHandler)handler).playerEntity).empty(2);
		}
		
		public static Packet getBrainFunctionPacket(float bf, float bl, float p, float f, float w) {
			return PacketDispatcher.getTinyPacket(INSTANCE, (short)0, new byte[] {(byte)(bf * 255), (byte)(bl * 255), (byte)(p * 255), (byte)(f * 255), (byte)(w * 255)});
		}
		
		public static Packet getActionPacket(int a) {
			return PacketDispatcher.getTinyPacket(INSTANCE, (short)a, new byte[0]);
		}
	}
	
	@ForgeSubscribe(priority = EventPriority.LOW)
	@SideOnly(Side.CLIENT)
	public void renderOverlay(RenderGameOverlayEvent.Post evt) {
		if(evt.type != RenderGameOverlayEvent.ElementType.ALL)
			return;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		
		// darken screen depending on brain function level
		{
			if(clientBrainFunction > 0.5)
				GL11.glColor4f(0, 0, 0, (1 - clientBrainFunction) * 0.95f/0.5f);
			else
				GL11.glColor4f(0, 0, 0, 0.95f);
			
			GL11.glBegin(GL11.GL_QUADS);
			
			GL11.glVertex2f(0, 0);
			GL11.glVertex2f(0, evt.resolution.getScaledHeight());
			GL11.glVertex2f(evt.resolution.getScaledWidth(), evt.resolution.getScaledHeight());
			GL11.glVertex2f(evt.resolution.getScaledWidth(), 0);
			
			GL11.glEnd();
		}
		
		GL11.glColor3f(1, 1, 1);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		{
			boolean flashTimer = (System.currentTimeMillis() % 1000) > 500;
			
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
			
			int x = evt.resolution.getScaledWidth() - 100;
			int w = 60;
			int y = evt.resolution.getScaledHeight()*3/4 - Bar.bars.length*4;
			int h = 8;
			int is = 8;
			
			for(int k = 0; k < Bar.bars.length; k++) {
				Bar b = Bar.bars[k];
				GL11.glColor3f(1, 1, 1);
				if(b.icon != null)
					drawIcon(x, y, is, is, b.icon);
				int xb = x + is + 1;
				
				int wb = w - is - 1;
				
				boolean flashThisBar = b.value < b.normalMin || b.value > b.normalMax;
				
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glBegin(GL11.GL_QUADS);
				
				// background
				if(flashThisBar && flashTimer)
					GL11.glColor3f(1, 0, 0);
				else
					GL11.glColor3f(0, 0, 0);
				GL11.glVertex2f(xb, y);
				GL11.glVertex2f(xb, y+h);
				GL11.glVertex2f(xb+wb, y+h);
				GL11.glVertex2f(xb+wb, y);
				
				float wb2 = wb * b.value;
				GL11.glColor3ub((byte)(b.colour >> 16), (byte)(b.colour >> 8), (byte)b.colour);
				GL11.glVertex2f(xb+1, y+1);
				GL11.glVertex2f(xb+1, y+h-1);
				GL11.glVertex2f(xb+wb2-1, y+h-1);
				GL11.glVertex2f(xb+wb2-1, y+1);
				
				GL11.glEnd();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				
				y += h;
			}
		}
		
		GL11.glColor4f(1, 1, 1, 1);
		
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	@SideOnly(Side.CLIENT)
	private void drawIcon(int x, int y, int w, int h, Icon i) {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(i.getMinU(), i.getMinV()); GL11.glVertex2i(x, y);
		GL11.glTexCoord2f(i.getMinU(), i.getMaxV()); GL11.glVertex2i(x, y+h);
		GL11.glTexCoord2f(i.getMaxU(), i.getMaxV()); GL11.glVertex2i(x+w, y+h);
		GL11.glTexCoord2f(i.getMaxU(), i.getMinV()); GL11.glVertex2i(x+w, y);
		GL11.glEnd();
	}

	@SideOnly(Side.CLIENT)
	private void drawBar(int x, int y, int w, int h, float f) {
		float right = x + w * f;
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x, y+h);
		GL11.glVertex2f(right, y+h);
		GL11.glVertex2f(right, y);
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onIconRegister(TextureStitchEvent.Pre evt) {
		if(evt.map.textureType == 0) {
			f_u.setIcons(evt.map.registerIcon("immibis_fluffyjam1:f_u_s"), evt.map.registerIcon("immibis_fluffyjam1:f_u_f"));
			f_d.setIcons(evt.map.registerIcon("immibis_fluffyjam1:f_d"));
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		blockOT = new OpTableBlock(2200);
		itemS = new Item(22000).setUnlocalizedName("immibis.fj1.itemS").setMaxStackSize(1).setTextureName("immibis_fluffyjam1:itemS");
		
		f_u = new Fluid("FJ1IMBU");
		f_d = new Fluid("FJ1IMBD");
		
		if(!FluidRegistry.registerFluid(f_u)) throw new RuntimeException(f_u.getName()+" already registered");
		if(!FluidRegistry.registerFluid(f_d)) throw new RuntimeException(f_d.getName()+" already registered");
		
		blockF_u = new BlockFluidClassic(2201, f_u, Material.water) {
			@Override
			@SideOnly(Side.CLIENT)
			public Icon getIcon(int par1, int par2) {
				if(par1 == 1)
					return f_u.getStillIcon();
				else
					return f_u.getFlowingIcon();
			}
			
			{setQuantaPerBlock(3);}
		};
		blockF_d = new BlockFluidClassic(2202, f_d, Material.water);
		
		Bar.initEventHandler();
		
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
				
				PlayerExtData.get((EntityPlayerMP)pl).tick();
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
		
		final KeyBinding kb_p = new KeyBinding("P", Keyboard.KEY_P);
		final KeyBinding kb_s = new KeyBinding("S", Keyboard.KEY_O);
		KeyBindingRegistry.registerKeyBinding(new KeyHandler(new KeyBinding[] {kb_p, kb_s}, new boolean[] {false, false}) {
			
			@Override
			public String getLabel() {
				return "key handler";
			}
			
			@Override
			public EnumSet<TickType> ticks() {
				return EnumSet.of(TickType.CLIENT);
			}
			
			@Override
			public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
				
			}
			
			@Override
			public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
				if(!tickEnd) return;
				
				if(kb == kb_p)
					PacketDispatcher.sendPacketToServer(TinyPacketHandler.getActionPacket(1));
				if(kb == kb_s)
					PacketDispatcher.sendPacketToServer(TinyPacketHandler.getActionPacket(2));
			}
		});
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@ForgeSubscribe
	public void onEntityConstruct(EntityEvent.EntityConstructing evt) {
		if(evt.entity instanceof EntityPlayerMP) {
			PlayerExtData link = new PlayerExtData();
			evt.entity.registerExtendedProperties(PlayerExtData.EXT_PROP_ID, link);
		}
	}
	
	@ForgeSubscribe
	public void onEntityJoinWorld(EntityJoinWorldEvent evt) {
		if(evt.entity instanceof EntityPlayerMP) {
			PlayerExtData link = PlayerExtData.get((EntityPlayerMP)evt.entity);
			ImmibisFJ1_ProtectedAccessProxy.setFoodStats((EntityPlayerMP)evt.entity, link.fakeFoodStats);
		} else if(evt.entity instanceof EntityPlayer && evt.world.isRemote)
			ImmibisFJ1_ProtectedAccessProxy.setFoodStats((EntityPlayer)evt.entity, new PlayerExtData.ClientFakeFoodStats());
	}
	
	@ForgeSubscribe
	public void onEmptyBucket(FillBucketEvent evt) {
		if(evt.current.getItem() == Item.bucketWater) {
			if(evt.entity instanceof EntityPlayerMP) {
				PlayerExtData.get((EntityPlayerMP)evt.entity).drink(1000);
				evt.setCanceled(true);
				evt.current.itemID = Item.bucketEmpty.itemID;
				evt.setResult(Event.Result.ALLOW);
			}
		}
		
		if(evt.current.getItem() == Item.bucketEmpty)
			// this block is not water
			if(evt.target.typeOfHit == EnumMovingObjectType.TILE && evt.world.getBlockId(evt.target.blockX, evt.target.blockY, evt.target.blockZ) == blockF_u.blockID)
				evt.setCanceled(true);
		
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
