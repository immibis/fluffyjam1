package com.immibis.fluffyjam1;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Icon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

/* Only used client-side */
public class Bar {
	public static final Bar f = new Bar("beef_cooked", 0xC08000);
	public static final Bar w = new Bar("bucket_water", 0x0000FF);
	public static final Bar ex1 = new Bar("x", 0xFFFF00);
	public static final Bar ex2 = new Bar("x", 0x604000);
	public static final Bar[] bars = new Bar[] {
		f, w, ex1, ex2
	};
	
	public String iconName;
	@SideOnly(Side.CLIENT) public Icon icon;
	public int colour;
	public float value;
	
	// yes, anything that relies on loading the class is dumb, and this should be an init() method that registers the
	// event handlers
	public static void initEventHandler() {
		if(FMLLaunchHandler.side().isClient()) {
			MinecraftForge.EVENT_BUS.register(new EventHandler());
		}
	}
	
	public static class EventHandler {
		@ForgeSubscribe
		@SideOnly(Side.CLIENT)
		public void onTextureStitch(TextureStitchEvent.Pre evt) {
			if(evt.map.textureType == 1)
				for(Bar b : bars)
					b.icon = evt.map.registerIcon(b.iconName);
		}
	}
	
	private Bar(String iconName, int colour) {
		this.iconName = iconName;
		this.colour = colour;
		
		
	}
}
