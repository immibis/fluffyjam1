package com.immibis.fluffyjam1;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import com.immibis.fluffyjam1.FluffyJam1Mod.TinyPacketHandler;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ProxyClient extends ProxyBase {
	
	@Override
	public void stopSprinting() {
		Minecraft.getMinecraft().thePlayer.setSprinting(false);
	}
	
	@Override
	public void init() {
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
				
				if(Minecraft.getMinecraft().currentScreen != null)
					return;
				
				if(kb == kb_p)
					PacketDispatcher.sendPacketToServer(TinyPacketHandler.getActionPacket(1));
				if(kb == kb_s)
					PacketDispatcher.sendPacketToServer(TinyPacketHandler.getActionPacket(2));
			}
		});
	}
}
