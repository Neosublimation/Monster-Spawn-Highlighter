package com.github.lunatrius.msh.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnConditionOcelot extends SpawnConditionAnimal {
    public SpawnConditionOcelot(String name, EntityLiving entity, boolean enabled) {
        super(name, entity, enabled);
    }

    @Override
    public SpawnType canSpawnAt(World world, int x, int y, int z) {
        if (hasNoCollisions(world)) {
            if (y >= 63) {
            	final BlockPos blockPos = new BlockPos(x, y - 1, z);
            	final IBlockState blockState = world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (block == Blocks.GRASS || block.isLeaves(blockState, world, blockPos)) {
                    return SpawnType.BOTH;
                }
            }
        }

        return SpawnType.NONE;
    }
}
