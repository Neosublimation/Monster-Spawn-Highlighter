package com.github.lunatrius.msh.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class SpawnConditionPigZombie extends SpawnConditionMob {
    public SpawnConditionPigZombie(String name, EntityLiving entity, boolean enabled) {
        super(name, entity, enabled);
    }

    @Override
    public SpawnType canSpawnAt(World world, int x, int y, int z) {
        return world.getDifficulty() != EnumDifficulty.PEACEFUL && hasNoCollisions(world) ? SpawnType.BOTH : SpawnType.NONE;
    }
}
