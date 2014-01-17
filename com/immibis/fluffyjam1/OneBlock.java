package com.immibis.fluffyjam1;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class OneBlock extends BlockContainer {
	public OneBlock(int id) {
		super(id, Material.iron);
		setHardness(0.5f);
		setUnlocalizedName("immibis.fj1.t");
		setTextureName("immibis_fj1:one");
		setCreativeTab(CreativeTabs.tabDecorations);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new OneTile();
	}
	
	Icon i0, ix;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		ix = par1IconRegister.registerIcon("iron_block");
		i0 = par1IconRegister.registerIcon("immibis_fluffyjam1:t");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2) {
		return par1 == 1 ? i0 : ix;
	}
}
