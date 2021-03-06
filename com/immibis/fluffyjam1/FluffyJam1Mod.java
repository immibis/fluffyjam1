package com.immibis.fluffyjam1;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.EnumSet;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ImmibisFJ1_ProtectedAccessProxy;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
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
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid="immibis_fj1", name="Totally Random Surgery Mod", version="1.0")
@NetworkMod(clientSideRequired=true, serverSideRequired=false, tinyPacketHandler=FluffyJam1Mod.TinyPacketHandler.class,
	serverPacketHandlerSpec=@NetworkMod.SidedPacketHandler(channels={"FJ1IMB"}, packetHandler=FluffyJam1Mod.ServerPacketHandler.class)
)
public class FluffyJam1Mod implements IGuiHandler {
	public static OpTableBlock blockOT;
	public static BlockFluidBase blockF_u, blockF_d;
	public static OneBlock block1;
	public static Item itemSludge;
	public static Block blockSludge;
	public static CreativeTabs tab = new CreativeTabs("fj1imb") {public ItemStack getIconItemStack() {return new ItemStack(Item.bed);}};
	
	public static final int REAGENT_PER_BLOCK = 200;
	public static final float REAGENT_PER_MILLIBUCKET_DRANK = 0.5f;
	
	public static final long MIDNIGHT = 18000;
	
	public static final int GUI_OP_TABLE = 1;
	
	public static Fluid f_u, f_d, f_sl;
	
	public static boolean SELF_OP_MODE = true; // if true, players operate on themselves, for SSP testing
	
	@Instance("immibis_fj1")
	public static FluffyJam1Mod INSTANCE;
	
	public static float clientBrainFunction;
	public static float clientArmFunction;
	
	public static class TinyPacketHandler implements ITinyPacketHandler {
		@Override
		public void handle(NetHandler handler, Packet131MapData p) {
			if(p.uniqueID == 0) {
				Bar.bf.value = clientBrainFunction = (p.itemData[0] & 255) / 255f;
				Bar.ex1.value = (p.itemData[1] & 255) / 255f;
				Bar.ex2.value = (p.itemData[2] & 255) / 255f;
				Bar.f.value = (p.itemData[3] & 255) / 255f;
				Bar.w.value = (p.itemData[4] & 255) / 255f;
				clientArmFunction = (p.itemData[5] & 255) / 255f;
				Bar.a.value = (p.itemData[6] & 255) / 255f;
			}
			if(p.uniqueID == 1)
				PlayerExtData.get(((NetServerHandler)handler).playerEntity).empty(1);
			if(p.uniqueID == 2)
				PlayerExtData.get(((NetServerHandler)handler).playerEntity).empty(2);
			if(p.uniqueID == 10)
				drink(((NetServerHandler)handler).playerEntity);
			if(p.uniqueID == 100)
				proxy.stopSprinting();
		}
		
		public static Packet getBrainFunctionPacket(float bf, float bl, float p, float f, float w, float af, float air) {
			return PacketDispatcher.getTinyPacket(INSTANCE, (short)0, new byte[] {(byte)(bf * 255), (byte)(bl * 255), (byte)(p * 255), (byte)(f * 255), (byte)(w * 255), (byte)(af * 255), (byte)(air * 255)});
		}
		
		public static Packet getActionPacket(int a) {
			return PacketDispatcher.getTinyPacket(INSTANCE, (short)a, new byte[0]);
		}
	}
	
	public static class ServerPacketHandler implements IPacketHandler {

		@Override
		public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
			if(packet.data[0] == 0 || packet.data[0] == 1) {
				/*ByteArrayInputStream is = new ByteArrayInputStream(packet.data);
				is.read();
				Container c = ((EntityPlayer)player).openContainer;
				if(c instanceof OpTableContainer)
					((OpTableContainer)c).receivePacket(c);*/
			}
		}
		
	}
	
	@ForgeSubscribe
	public void onBreakSpeed(PlayerEvent.BreakSpeed evt) {
		EntityPlayer pl = evt.entityPlayer;
		float arm_function;
		if(pl instanceof EntityPlayerMP)
			arm_function = PlayerExtData.get((EntityPlayerMP)pl).data.arm_energy_level;
		else if(pl.worldObj.isRemote)
			arm_function = clientArmFunction;
		else
			return;
		
		evt.newSpeed *= arm_function;
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
	
	@SidedProxy(clientSide="com.immibis.fluffyjam1.ProxyClient", serverSide="com.immibis.fluffyjam1.ProxyBase")
	public static ProxyBase proxy;
	
	public static DamageSource damageSource = new DamageSource("fj1imbds") {{
		setDamageAllowedInCreativeMode();
		setDamageBypassesArmor();
	}};

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		blockOT = new OpTableBlock(2200);
		block1 = new OneBlock(2203);
		itemSludge = new Item(22000).setUnlocalizedName("immibis.fj1.itemS").setMaxStackSize(1).setTextureName("immibis_fluffyjam1:itemS");
		blockSludge = new SludgeBlock(2204);
		
		f_u = new Fluid("FJ1IMBU");
		f_d = new Fluid("FJ1IMBD");
		f_sl = new Fluid("FJ1IMBSL");
		f_sl.setBlockID(blockSludge);
		
		if(!FluidRegistry.registerFluid(f_u)) throw new RuntimeException(f_u.getName()+" already registered");
		if(!FluidRegistry.registerFluid(f_d)) throw new RuntimeException(f_d.getName()+" already registered");
		if(!FluidRegistry.registerFluid(f_sl)) throw new RuntimeException(f_sl.getName()+" already registered");
		
		blockF_u = new BlockFluidClassic(2201, f_u, Material.water) {
			@Override
			@SideOnly(Side.CLIENT)
			public Icon getIcon(int par1, int par2) {
				if(par1 == 1)
					return f_u.getStillIcon();
				else
					return f_u.getFlowingIcon();
			}
		};
		blockF_d = new BlockFluidClassic(2202, f_d, Material.water);
		
		MinecraftForge.setBlockHarvestLevel(blockSludge, "shovel", 2);
		
		Bar.initEventHandler();
		
		SELF_OP_MODE = FMLLaunchHandler.side().isClient();
		
		GameRegistry.registerBlock(blockOT, OpTableItem.class, "optable");
		GameRegistry.registerBlock(blockF_u, "fu");
		GameRegistry.registerBlock(blockF_d, "fd");
		GameRegistry.registerBlock(block1, "1");
		GameRegistry.registerBlock(blockSludge, "s");
		
		GameRegistry.addRecipe(new ItemStack(block1),
			"  W",
			"SW/",
			" SS",
			'W', Item.bucketWater,
			'S', Block.stone,
			'/', Item.stick
			);
		
		GameRegistry.addRecipe(new ItemStack(blockOT),
			"PPP",
			"SBS",
			"AAA",
			'P', Item.pickaxeStone,
			'S', Item.swordStone,
			'A', Item.axeStone,
			'B', Item.bed);
		
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
				boolean inOperatingTable = pl.isPlayerSleeping();
				if(inOperatingTable) {
					ChunkCoordinates bed = pl.playerLocation;
					if(bed == null || !pl.worldObj.blockExists(bed.posX, bed.posY, bed.posZ) || pl.worldObj.getBlockId(bed.posX, bed.posY, bed.posZ) != blockOT.blockID)
						inOperatingTable = false;
				}
				
				if(pl.sleepTimer >= 98 && inOperatingTable) {
					// players in operating tables aren't actually sleeping
					// so don't let them become "fully asleep" at which point it would otherwise become day.
					// players are fully asleep once sleepTimer reaches 100.
					pl.sleepTimer = 98;
				}
				
				if(inOperatingTable)
					pl.worldObj.getWorldInfo().setWorldTime(MIDNIGHT);
				
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
		
		proxy.init();
		
		MinecraftForge.EVENT_BUS.register(this);
		
		GameRegistry.registerTileEntity(OneTile.class, "immibis_fj1.tile1");
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
	public void onInteract(PlayerInteractEvent evt) {
		if(!evt.entityPlayer.worldObj.isRemote)
			return;
		if(evt.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || evt.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
			if(evt.entityPlayer.inventory.getCurrentItem() == null) {
				MovingObjectPosition rt = OpTableItem.rayTraceFromPlayerForBuckets(evt.entityPlayer);
				if(rt != null && rt.typeOfHit == EnumMovingObjectType.TILE) {
					int hitBlockID = evt.entityPlayer.worldObj.getBlockId(rt.blockX, rt.blockY, rt.blockZ);
					if(hitBlockID == Block.waterMoving.blockID || hitBlockID == Block.waterStill.blockID) {
						if(evt.entityPlayer.worldObj.getBlockMetadata(rt.blockX, rt.blockY, rt.blockZ) == 0) {
							// calls drink(evt.entityPlayer) on the server
							PacketDispatcher.sendPacketToServer(TinyPacketHandler.getActionPacket(10));
							evt.setCanceled(true);
							evt.useBlock = Event.Result.DENY;
							evt.useItem = Event.Result.DENY;
						}
					}
				}
			}
		}
	}
	
	private static void drink(EntityPlayer ply) {
		if(!(ply instanceof EntityPlayerMP))
			return;
		
		MovingObjectPosition rt = OpTableItem.rayTraceFromPlayerForBuckets(ply);
		if(rt == null) return;
		if(rt.typeOfHit != EnumMovingObjectType.TILE) return;
		int hitBlockID = ply.worldObj.getBlockId(rt.blockX, rt.blockY, rt.blockZ);
		if(hitBlockID == Block.waterMoving.blockID || hitBlockID == Block.waterStill.blockID) {
			if(ply.worldObj.getBlockMetadata(rt.blockX, rt.blockY, rt.blockZ) == 0) {
				ply.worldObj.setBlockToAir(rt.blockX, rt.blockY, rt.blockZ);
				PlayerExtData.get((EntityPlayerMP)ply).drink(1000);
			}
		}
	}
	
	/*@ForgeSubscribe
	public void onEmptyBucket(FillBucketEvent evt) {
		if(evt.current.getItem() == Item.bucketWater) {
			if(evt.entity instanceof EntityPlayerMP) {
				PlayerExtData.get((EntityPlayerMP)evt.entity).drink(1000);
				evt.setCanceled(true);
				evt.current.itemID = Item.bucketEmpty.itemID;
				evt.setResult(Event.Result.ALLOW);
			}
		}
	}*/
	
	@ForgeSubscribe
	public void onFillBucket(FillBucketEvent evt) {
		if(evt.current.getItem() == Item.bucketEmpty)
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
