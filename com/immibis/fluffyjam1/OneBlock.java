package com.immibis.fluffyjam1;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class OneBlock extends BlockContainer {
	public OneBlock(int id) {
		super(id, Material.iron);
		setHardness(0.5f);
		setUnlocalizedName("immibis_fj1.BlockOne");
		setTextureName("immibis_fj1:one");
		setCreativeTab(CreativeTabs.tabDecorations);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new OneTile();
	}
}
