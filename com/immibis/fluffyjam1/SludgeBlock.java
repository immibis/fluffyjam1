package com.immibis.fluffyjam1;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class SludgeBlock extends Block implements IFluidBlock {
	public SludgeBlock(int id) {
		super(id, new Material(MapColor.woodColor) {
			// this makes water wash away sludge, but it's also required to make it pumpable :(
			/*@Override
			public boolean blocksMovement() {
				return false;
			}*/
		});
		
		setTextureName("immibis_fluffyjam1:sludge");
		setUnlocalizedName("immibis.fj1.sludge");
		setHardness(30f);
		setResistance(1000000f);
	}
	
	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> rv = new ArrayList<ItemStack>();
		rv.add(new ItemStack(FluffyJam1Mod.itemSludge));
		rv.add(new ItemStack(FluffyJam1Mod.itemSludge));
		rv.add(new ItemStack(FluffyJam1Mod.itemSludge));
		rv.add(new ItemStack(FluffyJam1Mod.itemSludge));
		return rv;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		super.registerIcons(par1IconRegister);
		FluffyJam1Mod.f_sl.setIcons(blockIcon);
	}
	
	@Override
	public boolean canDrain(World world, int x, int y, int z) {
		return true;
	}
	
	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain) {
		if(doDrain)
			world.setBlockToAir(x, y, z);
		return new FluidStack(FluffyJam1Mod.f_sl, 1000);
	}
	
	@Override
	@Deprecated
	public float getFilledPercentage(World world, int x, int y, int z) {
		return 1f;
	}
	
	@Override
	public Fluid getFluid() {
		return FluffyJam1Mod.f_sl;
	}
}
