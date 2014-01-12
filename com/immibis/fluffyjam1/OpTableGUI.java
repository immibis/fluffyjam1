package com.immibis.fluffyjam1;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class OpTableGUI extends GuiContainer {
	private ResourceLocation BG_TEX = new ResourceLocation("immibis_fluffyjam1", "textures/gui/optable.png");
	
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		
		mc.renderEngine.bindTexture(BG_TEX);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		Guts guts = cont.guts;
		
		for(int y = 0; y < 15; y++)
			for(int x = 0; x < 20; x++) {
				switch(guts.getTile(x, y)) {
				case EMPTY: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 204, 12, 12); break;
				case MOUTH: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 0, 204, 12, 12); break;
				case TUBE_V: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 192, 12, 12); break;
				}
			}
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_QUADS);
		
		for(int y = 0; y < 15; y++)
			for(int x = 0; x < 20; x++)
				switch(guts.getTile(x, y)) {
				case TUBE_V: drawReagents(guts.getReagents(x, y), 11+12*x, 6+12*y, 6, 12); break;
				}
		
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	private void drawReagents(Reagents r, int x, int y, int w, int h) {
		float scale = h / r.capacity;
		float total = 0;
		
		for(int id = 0; id < Reagent.COUNT; id++) {
			float _this = r.get(id);
			if(_this < 0.001)
				continue;
			float bottom = total * scale;
			float top = (total + _this) * scale;
			
			int col = Reagent.COLOUR[id];
			GL11.glColor3ub((byte)(col >> 16), (byte)(col >> 8), (byte)col);
			GL11.glVertex2f(guiLeft+x, guiTop+y+h-bottom);
			GL11.glVertex2f(guiLeft+x+w, guiTop+y+h-bottom);
			GL11.glVertex2f(guiLeft+x+w, guiTop+y+h-top);
			GL11.glVertex2f(guiLeft+x, guiTop+y+h-top);
		}
	}

	private OpTableContainer cont;
	
	public OpTableGUI(OpTableContainer container) {
		super(container);
		this.cont = container;
		xSize = 256;
		ySize = 192;
	}
}
