package com.github.lunatrius.msh.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class SpawnConditionMob extends SpawnConditionCreature {
    public SpawnConditionMob(String name, EntityLiving entity, boolean enabled) {
        super(name, entity, enabled);
    }

    @Override
    public SpawnType canSpawnAt(World world, int x, int y, int z) {
        return world.getDifficulty() != EnumDifficulty.PEACEFUL && this.isValidLightLevel(world, x, y, z) ? SpawnType.NIGHT.and(super.canSpawnAt(world, x, y, z)) : SpawnType.NONE;
    }

    protected boolean isValidLightLevel(World world, int x, int y, int z) {
        y = MathHelper.floor_double(this.entity.getEntityBoundingBox().minY);
        return getBlockLightLevel(world, x, y, z, LIGHT_NIGHT) <= 7;
    }
}
