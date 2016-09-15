package com.github.lunatrius.msh.handler.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.github.lunatrius.core.util.vector.Vector4i;
import com.github.lunatrius.msh.client.gui.GuiMonsterSpawnHighlighter;
import com.github.lunatrius.msh.entity.SpawnCondition;
import com.github.lunatrius.msh.handler.ConfigurationHandler;
import com.github.lunatrius.msh.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class Events {
    public static final KeyBinding TOGGLE_KEY = new KeyBinding("key.monsterspawnhighlighter.toggle", Keyboard.KEY_L, "key.category.monsterspawnhighlighter");
    public static final List<Vector4i> SPAWN_LIST = new ArrayList<Vector4i>();

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Frustum frustrum = new Frustum();
    private AxisAlignedBB boundingBox = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    private Map<Integer, Map<Class, SpawnPlacementType>> biomeCreatureSpawnMapping = new HashMap<Integer, Map<Class, SpawnPlacementType>>();
    private int ticks = -1;

    @SubscribeEvent
    public void tick(ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            onTick();
        }
    }

    @SubscribeEvent
    public void keyInput(KeyInputEvent event) {
        if (TOGGLE_KEY.isPressed()) {
            if (this.minecraft.currentScreen == null) {
                this.minecraft.displayGuiScreen(new GuiMonsterSpawnHighlighter(null));
            }
        }
    }

    public boolean onTick() {
        this.minecraft.mcProfiler.startSection("msh");

        if (--this.ticks < 0) {
            this.ticks = ConfigurationHandler.updateRate;

            if (this.minecraft.theWorld != null && ConfigurationHandler.renderSpawns != 0) {
                SPAWN_LIST.clear();

                this.frustrum.setPosition(Reference.PLAYER_POSITION.x, Reference.PLAYER_POSITION.y, Reference.PLAYER_POSITION.z);

                World world = this.minecraft.theWorld;

                int lowX, lowY, lowZ, highX, highY, highZ, x, y, z;
                SpawnCondition.SpawnType type;

                lowX = (int) (Math.floor(Reference.PLAYER_POSITION.x) - ConfigurationHandler.renderRangeXZ);
                highX = (int) (Math.floor(Reference.PLAYER_POSITION.x) + ConfigurationHandler.renderRangeXZ);
                lowY = (int) (Math.floor(Reference.PLAYER_POSITION.y) - ConfigurationHandler.renderRangeYBellow);
                highY = (int) (Math.floor(Reference.PLAYER_POSITION.y) + ConfigurationHandler.renderRangeYAbove);
                lowZ = (int) (Math.floor(Reference.PLAYER_POSITION.z) - ConfigurationHandler.renderRangeXZ);
                highZ = (int) (Math.floor(Reference.PLAYER_POSITION.z) + ConfigurationHandler.renderRangeXZ);

                for (y = lowY; y <= highY; y++) {
                    for (x = lowX; x <= highX; x++) {
                        for (z = lowZ; z <= highZ; z++) {
                            if (!this.frustrum.isBoundingBoxInFrustum(this.boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1))) {
                                continue;
                            }

                            setEntityLivingLocation(x, y, z);

                            if ((type = getCanSpawnHere(world, x, y, z)) != SpawnCondition.SpawnType.NONE) {
                                SPAWN_LIST.add(new Vector4i(x, y, z, type.ordinal()));
                            }
                        }
                    }
                }
            }
        }

        this.minecraft.mcProfiler.endSection();

        return true;
    }

    private void setEntityLivingLocation(int x, int y, int z) {
        for (SpawnCondition entityLivingEntry : SpawnCondition.SPAWN_CONDITIONS) {
            entityLivingEntry.entity.setLocationAndAngles(x + 0.5f, y, z + 0.5f, 0.0f, 0.0f);
        }
    }

    @SuppressWarnings("null")
    private SpawnCondition.SpawnType getCanSpawnHere(World world, int x, int y, int z) {
    	final IBlockState blockState = world.getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if (block == null || block == Blocks.AIR || blockState.getMaterial().isLiquid()) {
            return SpawnCondition.SpawnType.NONE;
        }

        Biome biome = world.getBiomeGenForCoords(new BlockPos(x, 0, z));

        Map<Class, SpawnPlacementType> classCreatureType = getClassCreatureTypeMapFromBiome(biome);
        if (classCreatureType == null) {
            return SpawnCondition.SpawnType.NONE;
        }

        Class clazz;
        SpawnPlacementType creatureType;

        SpawnCondition.SpawnType type = SpawnCondition.SpawnType.NONE;

        for (Map.Entry<Class, SpawnPlacementType> entry : classCreatureType.entrySet()) {
            clazz = entry.getKey();
            creatureType = entry.getValue();

            if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(creatureType, world, new BlockPos(x, y, z))) {
                continue;
            }

            SpawnCondition spawnCondition = SpawnCondition.CLASS_SPAWN_CONDITION_MAP.get(clazz);
            if (spawnCondition != null) {
                if (spawnCondition.enabled) {
                    type = type.or(spawnCondition.canSpawnAt(world, x, y, z));

                    if (type == SpawnCondition.SpawnType.BOTH) {
                        return SpawnCondition.SpawnType.BOTH;
                    }
                }
            }
        }

        return type;
    }

    private Map<Class, SpawnPlacementType> getClassCreatureTypeMapFromBiome(Biome biome) {
    	final int biomeId = Biome.getIdForBiome(biome);
        Map<Class, SpawnPlacementType> classCreatureTypeMap = this.biomeCreatureSpawnMapping.get(biomeId);
        if (classCreatureTypeMap != null) {
            return classCreatureTypeMap;
        }

        classCreatureTypeMap = new HashMap<Class, SpawnPlacementType>();

        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            List<Biome.SpawnListEntry> spawnableList = biome.getSpawnableList(creatureType);
            if (spawnableList != null) {
                for (Biome.SpawnListEntry entry : spawnableList) {
                    classCreatureTypeMap.put(entry.entityClass, EntitySpawnPlacementRegistry.getPlacementForEntity(entry.entityClass));
                }
            }
        }

        return this.biomeCreatureSpawnMapping.put(biomeId, classCreatureTypeMap);
    }
}
