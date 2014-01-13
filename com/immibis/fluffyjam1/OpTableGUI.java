package com.immibis.fluffyjam1;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class OpTableGUI extends GuiContainer {
	private ResourceLocation BG_TEX = new ResourceLocation("immibis_fluffyjam1", "textures/gui/optable.png");
	
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mousex, int mousey) {
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		
		mc.renderEngine.bindTexture(BG_TEX);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		Guts guts = cont.guts;
		
		for(int y = 0; y < 15; y++)
			for(int x = 0; x < 20; x++) {
				Guts.Tile t = guts.getTile(x, y);
				if(t instanceof Guts.EmptyTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 204, 12, 12);
				else if(t instanceof Guts.MouthTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 0, 204, 12, 12);
				else if(t instanceof Guts.PipeTile) {
					switch(((Guts.PipeTile)t).getMask()) {
					case Guts.DM_D | Guts.DM_U: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 192, 12, 12); break;
					case Guts.DM_L | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 0, 192, 12, 12); break;
					case Guts.DM_U | Guts.DM_D | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 24, 204, 12, 12); break;
					case Guts.DM_U | Guts.DM_D | Guts.DM_L: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 48, 204, 12, 12); break;
					case Guts.DM_U | Guts.DM_L | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 36, 216, 12, 12); break;
					case Guts.DM_D | Guts.DM_L | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 36, 192, 12, 12); break;
					}
				} else if(t instanceof Guts.PipeCrossTile) {
					switch(((Guts.PipeCrossTile)t).getMask1()) {
					case Guts.DM_L | Guts.DM_D: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 72, 192, 12, 12); break;
					case Guts.DM_L | Guts.DM_U: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 60, 192, 12, 12); break;
					case Guts.DM_L | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 84, 192, 12, 12); break;
					}
				} else if(t instanceof Guts.NoseTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 0, 216, 12, 12);
				else if(t instanceof Guts.LungTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 216, 12, 12);
				else if(t instanceof Guts.IntestineTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 228, 12, 12);
				else if(t instanceof Guts.HeartTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 0, 228, 12, 12);
				//case TUBE_BSLASH: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 72, 192, 12, 12);
				//case TUBE_FSLASH: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 60, 192, 12, 12);
				//case TUBE_CROSS: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 84, 192, 12, 12);
			}
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_QUADS);
		
		for(int y = 0; y < 15; y++)
			for(int x = 0; x < 20; x++) {
				Guts.Tile t = guts.getTile(x, y);
				int px = 8+12*x, py = 6+12*y;
				if(t instanceof Guts.PipeTile) {
					switch(((Guts.PipeTile)t).getMask()) {
					case Guts.DM_U | Guts.DM_D: drawReagents(t.nets[Guts.D_U].new_contents, px+3, py, 6, 12); break;
					case Guts.DM_L | Guts.DM_R: drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 12, 6); break;
					case Guts.DM_U | Guts.DM_D | Guts.DM_R:
						drawReagents(t.nets[Guts.D_U].new_contents, px+3, py, 6, 12);
						drawReagents(t.nets[Guts.D_U].new_contents, px+9, py+3, 3, 6);
						break;
					}
				} else if(t instanceof Guts.PipeCrossTile) {
					switch(((Guts.PipeCrossTile)t).getMask1()) {
					case Guts.DM_L | Guts.DM_D:
						drawReagents(t.nets[Guts.D_R].new_contents, px+10, py+3, 2, 6);
						drawReagents(t.nets[Guts.D_R].new_contents, px+3, py, 6, 2);
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 9, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py+9, 6, 3);
						break;
					case Guts.DM_L | Guts.DM_U:
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 2, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py, 6, 2);
						drawReagents(t.nets[Guts.D_R].new_contents, px+3, py+3, 9, 6);
						drawReagents(t.nets[Guts.D_R].new_contents, px+3, py+9, 6, 3);
						break;
					case Guts.DM_L | Guts.DM_R:
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 2, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+10, py+3, 2, 6);
						drawReagents(t.nets[Guts.D_U].new_contents, px+3, py, 6, 12);
						break;
					}
				} else if(t instanceof Guts.TankTile)
					drawReagents(((Guts.TankTile)t).r, px+1, py+1, 10, 10);
			}
		
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		mousex -= guiLeft;
		mousey -= guiTop;
		
		if(Mouse.isButtonDown(0)) {
			int hoverx = (mousex - 8) / 12, hovery = (mousey - 6) / 12;
			
			if(guts.validCoords(hoverx, hovery)) {
				List<String> desc = guts.getTile(hoverx, hovery).describe();
				drawHoveringText(desc, guiLeft+mousex, guiTop+mousey, fontRenderer);
			}
		}
	}
	
	private void drawReagents(Reagents r, int x, int y, int w, int h) {
		float total = Math.max(r.getTotal(), r.capacity*0.2f); //r.getTotal(); // r.capacity
		
		float _r = 0, _g = 0, _b = 0;
		
		for(int id = 0; id < Reagent.COUNT; id++) {
			float _this = r.get(id);
			if(_this < 0.001)
				continue;
			int col = Reagent.COLOUR[id];
			_r += ((col >> 16) & 255) * _this / total / 255f;
			_g += ((col >> 8) & 255) * _this / total / 255f;
			_b += (col & 255) * _this / total / 255f;
		}
		
		GL11.glColor3f(_r, _g, _b);
		GL11.glVertex2f(guiLeft+x, guiTop+y+h);
		GL11.glVertex2f(guiLeft+x+w, guiTop+y+h);
		GL11.glVertex2f(guiLeft+x+w, guiTop+y);
		GL11.glVertex2f(guiLeft+x, guiTop+y);
	}

	private OpTableContainer cont;
	
	public OpTableGUI(OpTableContainer container) {
		super(container);
		this.cont = container;
		xSize = 256;
		ySize = 192;
	}
}