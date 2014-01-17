package com.immibis.fluffyjam1;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.immibis.fluffyjam1.Guts.PipeTile;

import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class OpTableGUI extends GuiContainer {
	private ResourceLocation BG_TEX = new ResourceLocation("immibis_fluffyjam1", "textures/gui/optable.png");
	
	private int scrollx, scrolly;
	private boolean[][] drawnPipeLayer;
	private Mode curMode = Mode.EXAMINE;
	
	enum Mode {
		EXAMINE(224, 89, "Mode: Examine"),
		PLACE_PIPE(224, 113, "Mode: Place pipes"),
		REMOVE_PIPE(224, 137, "Mode: Remove pipes"),
		MOVE_ORGANS(224, 161, "Mode: Move organs"),
		;
		private Mode(int u, int v, String title) {
			this.u = u;
			this.v = v;
			this.title = title;
		}
		int u, v; // position of icon in texture sheet
		String title;
		
		static Mode[] VALUES = values();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mousex, int mousey) {
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		
		mc.renderEngine.bindTexture(BG_TEX);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		Guts guts = cont.guts;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glColor3f(1, 1, 1);
		GL11.glVertex2i(guiLeft+8, guiTop+6);
		GL11.glVertex2i(guiLeft+8, guiTop+6+15*12);
		GL11.glVertex2i(guiLeft+8+20*12, guiTop+6+15*12);
		GL11.glVertex2i(guiLeft+8+20*12, guiTop+6);
		
		for(int y = 0; y < 15; y++)
			for(int x = 0; x < 20; x++) {
				Guts.Tile t = guts.getTile(x+scrollx, y+scrolly);
				int px = 8+12*x, py = 6+12*y;
				if(t instanceof Guts.IPipeTile) {
					switch(((Guts.IPipeTile)t).getMask()) {
					case Guts.DM_U | Guts.DM_D: drawReagents(t.nets[Guts.D_U].new_contents, px+3, py, 6, 12); break;
					case Guts.DM_L | Guts.DM_R: drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 12, 6); break;
					case Guts.DM_U | Guts.DM_D | Guts.DM_R:
						drawReagents(t.nets[Guts.D_U].new_contents, px+3, py, 6, 12);
						drawReagents(t.nets[Guts.D_U].new_contents, px+9, py+3, 3, 6);
						break;
					case Guts.DM_U | Guts.DM_D | Guts.DM_L:
						drawReagents(t.nets[Guts.D_U].new_contents, px+3, py, 6, 12);
						drawReagents(t.nets[Guts.D_U].new_contents, px, py+3, 3, 6);
						break;
					case Guts.DM_L | Guts.DM_R | Guts.DM_U:
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 12, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py, 6, 3);
						break;
					case Guts.DM_L | Guts.DM_R | Guts.DM_D:
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 12, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py+9, 6, 3);
						break;
					case Guts.DM_R | Guts.DM_D:
						drawReagents(t.nets[Guts.D_R].new_contents, px+9, py+3, 3, 6);
						drawReagents(t.nets[Guts.D_R].new_contents, px+3, py+3, 6, 9);
						break;
					case Guts.DM_R | Guts.DM_U:
						drawReagents(t.nets[Guts.D_R].new_contents, px+9, py+3, 3, 6);
						drawReagents(t.nets[Guts.D_R].new_contents, px+3, py, 6, 9);
						break;
					case Guts.DM_L | Guts.DM_D:
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 3, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py+3, 6, 9);
						break;
					case Guts.DM_L | Guts.DM_U:
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 3, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py, 6, 9);
						break;
					case Guts.DM_L | Guts.DM_U | Guts.DM_R | Guts.DM_D:
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py, 6, 12);
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 12, 6);
						break;
					}
				} else if(t instanceof Guts.PipeCrossTile) {
					switch(((Guts.PipeCrossTile)t).getMask1()) {
					case Guts.DM_L | Guts.DM_D: case Guts.DM_U | Guts.DM_R:
						drawReagents(t.nets[Guts.D_R].new_contents, px+10, py+3, 2, 6);
						drawReagents(t.nets[Guts.D_R].new_contents, px+3, py, 6, 2);
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 9, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py+9, 6, 3);
						break;
					case Guts.DM_L | Guts.DM_U: case Guts.DM_D | Guts.DM_R:
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 2, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+3, py, 6, 2);
						drawReagents(t.nets[Guts.D_R].new_contents, px+3, py+3, 9, 6);
						drawReagents(t.nets[Guts.D_R].new_contents, px+3, py+9, 6, 3);
						break;
					case Guts.DM_L | Guts.DM_R: case Guts.DM_D | Guts.DM_U:
						drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 2, 6);
						drawReagents(t.nets[Guts.D_L].new_contents, px+10, py+3, 2, 6);
						drawReagents(t.nets[Guts.D_U].new_contents, px+3, py, 6, 12);
						break;
					}
				} else if(t instanceof Guts.TankTile)
					drawReagents(t.nets[Guts.D_U].new_contents, px, py, 12, 12);
				else if(t instanceof Guts.ValveTile) {
					drawReagents(t.nets[Guts.D_U].new_contents, px+3, py, 6, 1);
					drawReagents(t.nets[Guts.D_U].new_contents, px+3, py+1, 2, 1);
					drawReagents(t.nets[Guts.D_U].new_contents, px+7, py+1, 2, 1);
					drawReagents(t.nets[Guts.D_D].new_contents, px+3, py+11, 6, 1);
					drawReagents(t.nets[Guts.D_D].new_contents, px+3, py+10, 2, 1);
					drawReagents(t.nets[Guts.D_D].new_contents, px+7, py+10, 2, 1);
				} else if(t instanceof Guts.KidneyTile)
					drawReagents(t.nets[Guts.D_L].new_contents, px, py+3, 12, 6);
				else if(t instanceof Guts.MouthTile)
					drawReagents(t.nets[Guts.D_R].new_contents, px, py+3, 12, 6);
			}
		
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		
		GL11.glColor3f(1, 1, 1);
		
		for(int y = 0; y < 15; y++)
			for(int x = 0; x < 20; x++) {
				Guts.Tile t = guts.getTile(x+scrollx, y+scrolly);
				if(t instanceof Guts.EmptyTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 204, 12, 12);
				else if(t instanceof Guts.MouthTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 132, 204, 12, 12);
				else if(t instanceof Guts.PipeTile) {
					switch(((Guts.PipeTile)t).getMask()) {
					case Guts.DM_D | Guts.DM_U: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 192, 12, 12); break;
					case Guts.DM_L | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 0, 192, 12, 12); break;
					case Guts.DM_D | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 24, 192, 12, 12); break;
					case Guts.DM_D | Guts.DM_L: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 48, 192, 12, 12); break;
					case Guts.DM_U | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 24, 216, 12, 12); break;
					case Guts.DM_U | Guts.DM_L: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 48, 216, 12, 12); break;
					case Guts.DM_U | Guts.DM_D | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 24, 204, 12, 12); break;
					case Guts.DM_U | Guts.DM_D | Guts.DM_L: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 48, 204, 12, 12); break;
					case Guts.DM_U | Guts.DM_L | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 36, 216, 12, 12); break;
					case Guts.DM_D | Guts.DM_L | Guts.DM_R: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 36, 192, 12, 12); break;
					case Guts.DM_U | Guts.DM_D | Guts.DM_R | Guts.DM_L: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 36, 204, 12, 12); break;
					}
				} else if(t instanceof Guts.PipeCrossTile) {
					switch(((Guts.PipeCrossTile)t).getMask1()) {
					case Guts.DM_L | Guts.DM_D: case Guts.DM_R | Guts.DM_U: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 72, 192, 12, 12); break;
					case Guts.DM_L | Guts.DM_U: case Guts.DM_R | Guts.DM_D: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 60, 192, 12, 12); break;
					case Guts.DM_L | Guts.DM_R: case Guts.DM_U | Guts.DM_D: drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 84, 192, 12, 12); break;
					}
				} else if(t instanceof Guts.NoseTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 156, 216, 12, 12);
				else if(t instanceof Guts.LungTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 216, 12, 12);
				else if(t instanceof Guts.IntestineTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 12, 228 + (((Guts.IntestineTile)t).horiz ? 12 : 0), 12, 12);
				else if(t instanceof Guts.HeartTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 0, 228, 12, 12);
				else if(t instanceof Guts.OrificeTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 132, 228, 12, 12);
				else if(t instanceof Guts.BrainTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 36, 228, 12, 12);
				else if(t instanceof Guts.ObstacleTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, ((Guts.ObstacleTile)t).u, ((Guts.ObstacleTile)t).v, 12, 12);
				else if(t instanceof Guts.SensorTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 156, 228, 12, 12);
				else if(t instanceof Guts.ValveTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, ((Guts.ValveTile)t).open ? 168 : 156, 240, 12, 12);
				else if(t instanceof Guts.LegTile)
					drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 60, 240, 12, 12);
				
				switch(curMode) {
				case REMOVE_PIPE:
					if(t != null && drawnPipeLayer != null && drawnPipeLayer[x+scrollx][y+scrolly])
						drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 144, 240, 12, 12);
					break;
				case PLACE_PIPE:
					if(t != null && drawnPipeLayer != null && drawnPipeLayer[x+scrollx][y+scrolly])
						drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 132, 240, 12, 12);
					break;
				case MOVE_ORGANS:
					if(t != null && t instanceof Guts.IMovable && ((moveFromX == -1 && moveFromY == -1) || (moveFromX == x+scrollx && moveFromY == y+scrolly)))
						drawTexturedModalRect(guiLeft + 8 + 12*x, guiTop + 6 + 12*y, 168, 228, 12, 12);
					break;
				}					
			}
		
		GL11.glDisable(GL11.GL_BLEND);
		
		drawModeSelector();
		
		mousex -= guiLeft;
		mousey -= guiTop;
		
		if(curMode == Mode.EXAMINE)
		{
			int hoverx = (mousex - 8) / 12, hovery = (mousey - 6) / 12;
			
			if(hoverx >= 0 && hovery >= 0 && hoverx < 20 && hovery < 15) {
				Guts.Tile tile = guts.getTile(hoverx + scrollx, hovery + scrolly);
				if(tile != null) {
					List<String> desc = tile.describe();
					drawHoveringText(desc, guiLeft+mousex, guiTop+mousey, fontRenderer);
				}
			}
		}
		
		Mode modeUnderMouse = getModeSelectorUnderMouse(mousex, mousey);
		if(modeUnderMouse != null)
			drawHoveringText(Arrays.asList(modeUnderMouse.title), guiLeft+mousex, guiTop+mousey, fontRenderer);
	}
	
	private Mode getModeSelectorUnderMouse(int x, int y) {
		final int size = 24;
		x -= xSize + 2;
		y -= 6;
		if(x < 0) return null;
		if(x >= size+2) return null;
		if(y < 0) return null;
		
		int num = y / (size+4);
		int rely = y % (size+4);
		if(rely >= size+2) return null;
		if(num >= Mode.VALUES.length) return null;
		return Mode.VALUES[num];
	}
	
	private void drawModeSelector() {
		final int size = 24;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_QUADS);
		for(Mode m : Mode.VALUES) {
			if(m == curMode)
				GL11.glColor3f(0, 1, 0);
			else
				GL11.glColor3f(0.75f, 0, 0);
			int x = guiLeft + xSize + 2;
			int y = guiTop + m.ordinal()*(size+4) + 6;
			GL11.glVertex2f(x, y);
			GL11.glVertex2f(x, y+size+2);
			GL11.glVertex2f(x+size+2, y+size+2);
			GL11.glVertex2f(x+size+2, y);
		}
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);
		
		for(Mode m : Mode.VALUES) {
			int x = guiLeft + xSize + 2;
			int y = guiTop + m.ordinal()*(size+4) + 6;
			drawTexturedModalRect(x+1, y+1, m.u, m.v, size, size);
		}
	}

	private boolean validOnScreenTileCoords(int x, int y) {
		return x >= 0 && y >= 0 && x < 20 && y < 15;
	}
	
	@Override
	protected void mouseClicked(int x, int y, int btn) {
		//super.mouseClicked(x, y, btn);
		
		Mode mode = getModeSelectorUnderMouse(x-guiLeft, y-guiTop);
		if(mode != null) {
			curMode = mode;
			return;
		}
		
		int tx = (x - guiLeft - 8) / 12, ty = (y - guiTop - 6) / 12;
		if(!validOnScreenTileCoords(tx, ty))
			return;
		
		tx += scrollx; ty += scrolly;
		if(!cont.guts.validCoords(tx, ty))
			return;
		
		if(btn == 0 && (curMode == Mode.REMOVE_PIPE || curMode == Mode.PLACE_PIPE)) {
			drawnPipeLayer = new boolean[cont.guts.w][cont.guts.h];
			drawnPipeLayer[tx][ty] = true;
		} else if(btn == 0 && curMode == Mode.MOVE_ORGANS) {
			moveFromX = tx;
			moveFromY = ty;
		}
	}
	
	private int moveFromX = -1, moveFromY = -1;
	
	@Override
	protected void mouseClickMove(int x, int y, int btn, long par4) {
		//super.mouseClickMove(x, y, btn, par4);
		mouseMovedOrUp(x, y, -1);
	}
	
	@Override
	protected void mouseMovedOrUp(int x, int y, int btn) {
		//super.mouseMovedOrUp(x, y, btn);
		
		x -= guiLeft;
		y -= guiTop;
		
		boolean mouseOnValidTile = true;
		int tx = (x - 8) / 12, ty = (y - 6) / 12;
		if(!validOnScreenTileCoords(tx, ty))
			mouseOnValidTile = false;
		
		tx += scrollx; ty += scrolly;
		if(!cont.guts.validCoords(tx, ty))
			mouseOnValidTile = false;
		
		if(btn == 0) {
			
			if(curMode == Mode.REMOVE_PIPE || curMode == Mode.PLACE_PIPE) {
				if(drawnPipeLayer != null) {
					OpTableContainer.DrawData dd = new OpTableContainer.DrawData();
					dd.removeMode = curMode == Mode.REMOVE_PIPE;
					dd.map = drawnPipeLayer;
					cont.sendToServer(dd);
					
					cont.guts.finishDrawingPipes(drawnPipeLayer, curMode == Mode.REMOVE_PIPE);
				}
			
			} else if(curMode == Mode.MOVE_ORGANS) {
				if(mouseOnValidTile && cont.guts.validCoords(moveFromX, moveFromY)) {
					OpTableContainer.MoveData md = new OpTableContainer.MoveData();
					md.fx = moveFromX;
					md.fy = moveFromY;
					md.tx = tx;
					md.ty = ty;
					cont.sendToServer(md);
					
					cont.guts.moveTile(moveFromX, moveFromY, tx, ty);
				}
			}
			
			drawnPipeLayer = null;
			moveFromX = moveFromY = -1;
			
		} else if(mouseOnValidTile && Mouse.isButtonDown(0) && (curMode == Mode.REMOVE_PIPE || curMode == Mode.PLACE_PIPE)) {
			drawnPipeLayer[tx][ty] = true;
		}
	}
	
	

	@Override
	protected void keyTyped(char par1, int par2) {
		switch(par2) {
		case Keyboard.KEY_UP: scrolly--; break;
		case Keyboard.KEY_DOWN: scrolly++; break;
		case Keyboard.KEY_LEFT: scrollx--; break;
		case Keyboard.KEY_RIGHT: scrollx++; break;
		}
		if(par1 >= '0' && par1 <= '9' && par1 < '0' + Mode.VALUES.length)
			curMode = Mode.VALUES[par1 - '0'];
		super.keyTyped(par1, par2);
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
