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
	public static final Bar f = new Bar("beef_cooked", 0xC08000, 0.15f, Float.POSITIVE_INFINITY);
	public static final Bar w = new Bar("bucket_water", 0x0000FF, 0.15f, Float.POSITIVE_INFINITY);
	public static final Bar ex1 = new Bar("immibis_fluffyjam1:drop", 0xFFFF00, Float.NEGATIVE_INFINITY, 0.8f);
	public static final Bar ex2 = new Bar("immibis_fluffyjam1:itemS", 0x604000, Float.NEGATIVE_INFINITY, 0.8f);
	public static final Bar bf = new Bar("immibis_fluffyjam1:smiley", 0x00FF00, 0.6f, Float.POSITIVE_INFINITY);
	public static final Bar[] bars = new Bar[] {
		f, w, ex1, ex2, bf
	};
	
	public String iconName;
	@SideOnly(Side.CLIENT) public Icon icon;
	public int colour;
	public float value;
	public float normalMin, normalMax;
	
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
	
	private Bar(String iconName, int colour, float normalMin, float normalMax) {
		this.iconName = iconName;
		this.colour = colour;
		this.normalMin = normalMin;
		this.normalMax = normalMax;
	}
}
