package com.immibis.fluffyjam1;

import java.util.Iterator;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockContainer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumEntitySize;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EnumStatus;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet17Sleep;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class OpTableBlock extends BlockBed {
	public OpTableBlock(int id) {
		super(id);
		setCreativeTab(CreativeTabs.tabDecorations);
		setHardness(0.2F);
		setUnlocalizedName("immibis.fj1.optable");
		disableStats();
		setTextureName("bed");
		
		GameRegistry.registerBlock(this, OpTableItem.class, "optable");
	}
	
	public static EntityPlayer getPlayerInBed(World w, int x, int y, int z) {
		if(w.getBlockId(x, y, z) != FluffyJam1Mod.blockOT.blockID)
			return null;
		
		int i1 = w.getBlockMetadata(x, y, z);
		if (!isBlockHeadOfBed(i1))
        {
            int j1 = getDirection(i1);
            x += footBlockToHeadBlockMap[j1][0];
            z += footBlockToHeadBlockMap[j1][1];

            if (w.getBlockId(x, y, z) != FluffyJam1Mod.blockOT.blockID)
                return null;

            i1 = w.getBlockMetadata(x, y, z);
        }
		
		if (isBedOccupied(i1))
        {
            Iterator iterator = w.playerEntities.iterator();

            while (iterator.hasNext())
            {
                EntityPlayer entityplayer2 = (EntityPlayer)iterator.next();

                if (entityplayer2.isPlayerSleeping())
                {
                    ChunkCoordinates chunkcoordinates = entityplayer2.playerLocation;

                    if (chunkcoordinates.posX == x && chunkcoordinates.posY == y && chunkcoordinates.posZ == z)
                        return entityplayer2;
                }
            }

            setBedOccupied(w, x, y, z, false);
        }
        
		return null;
	}
	
	/**
     * Called upon block activation (right click on the block.)
     */
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        if (par1World.isRemote)
        {
            return true;
        }
        else
        {
            int i1 = par1World.getBlockMetadata(par2, par3, par4);

            if (!isBlockHeadOfBed(i1))
            {
                int j1 = getDirection(i1);
                par2 += footBlockToHeadBlockMap[j1][0];
                par4 += footBlockToHeadBlockMap[j1][1];

                if (par1World.getBlockId(par2, par3, par4) != this.blockID)
                {
                    return true;
                }

                i1 = par1World.getBlockMetadata(par2, par3, par4);
            }

            if (par1World.provider.canRespawnHere() && par1World.getBiomeGenForCoords(par2, par4) != BiomeGenBase.hell)
            {
                if (isBedOccupied(i1))
                {
                    EntityPlayer entityplayer1 = null;
                    Iterator iterator = par1World.playerEntities.iterator();

                    while (iterator.hasNext())
                    {
                        EntityPlayer entityplayer2 = (EntityPlayer)iterator.next();

                        if (entityplayer2.isPlayerSleeping())
                        {
                            ChunkCoordinates chunkcoordinates = entityplayer2.playerLocation;

                            if (chunkcoordinates.posX == par2 && chunkcoordinates.posY == par3 && chunkcoordinates.posZ == par4)
                            {
                                entityplayer1 = entityplayer2;
                            }
                        }
                    }

                    if (entityplayer1 != null)
                    {
                        par5EntityPlayer.addChatMessage("tile.immibis.fj1.optable.occupied");
                        return true;
                    }

                    setBedOccupied(par1World, par2, par3, par4, false);
                }

                EnumStatus enumstatus = sleepInBedAt(par5EntityPlayer, par2, par3, par4);

                if (enumstatus == EnumStatus.OK)
                {
                    setBedOccupied(par1World, par2, par3, par4, true);
                    
                    if(FluffyJam1Mod.SELF_OP_MODE)
                    	par5EntityPlayer.openGui(FluffyJam1Mod.INSTANCE, FluffyJam1Mod.GUI_OP_TABLE, par1World, par2, par3, par4);
                    
                    return true;
                }
                else
                {
                    if (enumstatus == EnumStatus.NOT_POSSIBLE_NOW)
                    {
                        par5EntityPlayer.addChatMessage("tile.bed.noSleep");
                    }
                    else if (enumstatus == EnumStatus.NOT_SAFE)
                    {
                        par5EntityPlayer.addChatMessage("tile.bed.notSafe");
                    }

                    return true;
                }
            }
            else
            {
                double d0 = (double)par2 + 0.5D;
                double d1 = (double)par3 + 0.5D;
                double d2 = (double)par4 + 0.5D;
                par1World.setBlockToAir(par2, par3, par4);
                int k1 = getDirection(i1);
                par2 += footBlockToHeadBlockMap[k1][0];
                par4 += footBlockToHeadBlockMap[k1][1];

                if (par1World.getBlockId(par2, par3, par4) == this.blockID)
                {
                    par1World.setBlockToAir(par2, par3, par4);
                    d0 = (d0 + (double)par2 + 0.5D) / 2.0D;
                    d1 = (d1 + (double)par3 + 0.5D) / 2.0D;
                    d2 = (d2 + (double)par4 + 0.5D) / 2.0D;
                }

                par1World.newExplosion((Entity)null, (double)((float)par2 + 0.5F), (double)((float)par3 + 0.5F), (double)((float)par4 + 0.5F), 5.0F, true, true);
                return true;
            }
        }
    }
	

    private EnumStatus sleepInBedAt(EntityPlayer ply, int par1, int par2, int par3) {
    	
    	if(true) return ply.sleepInBedAt(par1, par2, par3);
    	
    	if (ply.isRiding())
        {
    		ply.mountEntity((Entity)null);
        }

    	setSize(ply, 0.2F, 0.2F);
    	ply.yOffset = 0.2F;

        if (ply.worldObj.blockExists(par1, par2, par3))
        {
            int l = ply.worldObj.getBlockMetadata(par1, par2, par3);
            int i1 = BlockBed.getDirection(l);
            Block block = Block.blocksList[ply.worldObj.getBlockId(par1, par2, par3)];
            if (block != null)
            {
                i1 = block.getBedDirection(ply.worldObj, par1, par2, par3);
            }
            float f = 0.5F;
            float f1 = 0.5F;

            switch (i1)
            {
                case 0:
                    f1 = 0.9F;
                    break;
                case 1:
                    f = 0.1F;
                    break;
                case 2:
                    f1 = 0.1F;
                    break;
                case 3:
                    f = 0.9F;
            }

            func_71013_b(ply, i1);
            ply.setPosition((double)((float)par1 + f), (double)((float)par2 + 0.9375F), (double)((float)par3 + f1));
        }
        else
        {
        	ply.setPosition((double)((float)par1 + 0.5F), (double)((float)par2 + 0.9375F), (double)((float)par3 + 0.5F));
        }

        ply.playerLocation = new ChunkCoordinates(par1, par2, par3);
        ply.motionX = ply.motionZ = ply.motionY = 0.0D;
        
        if(ply instanceof EntityPlayerMP) {
        	 Packet17Sleep packet17sleep = new Packet17Sleep(ply, 0, par1, par2, par3);
             ((EntityPlayerMP)ply).getServerForPlayer().getEntityTracker().sendPacketToAllPlayersTrackingEntity(ply, packet17sleep);
             //((EntityPlayerMP)ply).playerNetServerHandler.setPlayerLocation(ply.posX, ply.posY, ply.posZ, ply.rotationYaw, ply.rotationPitch);
             ((EntityPlayerMP)ply).playerNetServerHandler.sendPacketToPlayer(packet17sleep);
        }

        return EnumStatus.OK;
	}
    
    private void setSize(EntityPlayer ply, float par1, float par2) {
	    float f2;

        if (par1 != ply.width || par2 != ply.height)
        {
            f2 = ply.width;
            ply.width = par1;
            ply.height = par2;
            ply.boundingBox.maxX = ply.boundingBox.minX + (double)ply.width;
            ply.boundingBox.maxZ = ply.boundingBox.minZ + (double)ply.width;
            ply.boundingBox.maxY = ply.boundingBox.minY + (double)ply.height;

            if (ply.width > f2 && !ply.worldObj.isRemote)
            {
            	ply.moveEntity((double)(f2 - ply.width), 0.0D, (double)(f2 - ply.width));
            }
        }

        f2 = par1 % 2.0F;

        if ((double)f2 < 0.375D)
        {
        	ply.myEntitySize = EnumEntitySize.SIZE_1;
        }
        else if ((double)f2 < 0.75D)
        {
        	ply.myEntitySize = EnumEntitySize.SIZE_2;
        }
        else if ((double)f2 < 1.0D)
        {
        	ply.myEntitySize = EnumEntitySize.SIZE_3;
        }
        else if ((double)f2 < 1.375D)
        {
        	ply.myEntitySize = EnumEntitySize.SIZE_4;
        }
        else if ((double)f2 < 1.75D)
        {
        	ply.myEntitySize = EnumEntitySize.SIZE_5;
        }
        else
        {
            ply.myEntitySize = EnumEntitySize.SIZE_6;
        }
    }
    
    private void func_71013_b(EntityPlayer ply, int par1)
    {
        ply.field_71079_bU = 0.0F;
        ply.field_71089_bV = 0.0F;

        switch (par1)
        {
            case 0:
            	ply.field_71089_bV = -1.8F;
                break;
            case 1:
            	ply.field_71079_bU = 1.8F;
                break;
            case 2:
            	ply.field_71089_bV = 1.8F;
                break;
            case 3:
            	ply.field_71079_bU = -1.8F;
        }
    }

	@SideOnly(Side.CLIENT)
    @Override
    public int idPicked(World par1World, int par2, int par3, int par4)
    {
        return blockID;
    }
    
    @Override
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return isBlockHeadOfBed(par1) ? 0 : blockID;
    }
    
    @Override
    public boolean isBed(World world, int x, int y, int z, EntityLivingBase player) {
    	return true;
    }
}
