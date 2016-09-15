package com.github.lunatrius.msh.entity;

import com.github.lunatrius.core.world.chunk.ChunkHelper;
import com.github.lunatrius.msh.reference.Reference;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeSwamp;

public class SpawnConditionSlime extends SpawnCondition {
    public SpawnConditionSlime(String name, EntityLiving entity, boolean enabled) {
        super(name, entity, enabled);
    }

    @Override
    public SpawnType canSpawnAt(World world, int x, int y, int z) {
        if (world.getDifficulty() != EnumDifficulty.PEACEFUL) {
            Biome biomegenbase = world.getBiome(new BlockPos(x, 0, z));

            if (biomegenbase instanceof BiomeSwamp && y > 50 && y < 70 && getBlockLightLevel(world, x, y, z, LIGHT_NIGHT) <= 7) {
                return super.canSpawnAt(world, x, y, z);
            }

            if (Reference.hasSeed && ChunkHelper.isSlimeChunk(Reference.seed, new BlockPos(x >> 4, 0, z >> 4)) && y < 40.0D) {
                return super.canSpawnAt(world, x, y, z);
            }
        }

        return SpawnType.NONE;
    }
}
