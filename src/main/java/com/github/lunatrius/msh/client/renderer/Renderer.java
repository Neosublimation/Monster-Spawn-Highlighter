package com.github.lunatrius.msh.client.renderer;

import org.lwjgl.opengl.GL11;

import com.github.lunatrius.core.util.vector.Vector4i;
import com.github.lunatrius.msh.handler.ConfigurationHandler;
import com.github.lunatrius.msh.handler.client.Events;
import com.github.lunatrius.msh.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Renderer {
    private Minecraft minecraft = null;
    private final int[] list = new int[] {
            -1, -1, -1, -1, -1, -1
    };

    public Renderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        compileList();
    }

    private void compileList() {
        for (int i = 0; i < 3; i++) {
            this.list[i * 2 + 0] = GL11.glGenLists(1);
            GL11.glNewList(this.list[i * 2 + 0], GL11.GL_COMPILE);
            GL11.glBegin(GL11.GL_QUADS);
            createQuad(0.1f + i * 0.1f, 0.9f - i * 0.1f);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINE_LOOP);
            createQuad(0.1f + i * 0.1f, 0.9f - i * 0.1f);
            GL11.glEnd();
            GL11.glEndList();

            this.list[i * 2 + 1] = GL11.glGenLists(1);
            GL11.glNewList(this.list[i * 2 + 1], GL11.GL_COMPILE);
            GL11.glBegin(GL11.GL_QUADS);
            createQuad(0.1f + i * 0.1f, 0.9f - i * 0.1f);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINE_LOOP);
            createQuad(0.1f + i * 0.1f, 0.9f - i * 0.1f);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex3f(0.5f, 0.0f, 0.5f);
            GL11.glVertex3f(0.5f, ConfigurationHandler.guideLength, 0.5f);
            GL11.glEnd();
            GL11.glEndList();
        }
    }

    private void createQuad(float min, float max) {
        GL11.glVertex3f(min, 0.03f, min);
        GL11.glVertex3f(min, 0.03f, max);
        GL11.glVertex3f(max, 0.03f, max);
        GL11.glVertex3f(max, 0.03f, min);
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (this.minecraft != null && ConfigurationHandler.renderSpawns != 0) {
            EntityPlayerSP player = this.minecraft.thePlayer;
            if (player != null) {
                Reference.PLAYER_POSITION.x = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks());
                Reference.PLAYER_POSITION.y = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks());
                Reference.PLAYER_POSITION.z = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks());

                render();
            }
        }
    }

    private void render() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);

        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glLineWidth(2.0f);

        GL11.glTranslatef(-Reference.PLAYER_POSITION.x, -Reference.PLAYER_POSITION.y, -Reference.PLAYER_POSITION.z);

        float delta;
        Block block;
        for (Vector4i blockPos : Events.SPAWN_LIST) {
            int type = blockPos.w;
            switch (type) {
            case 1:
                ConfigurationHandler.glColorDay();
                break;

            case 2:
                ConfigurationHandler.glColorNight();
                break;

            case 3:
                ConfigurationHandler.glColorBoth();
                break;
            }

            delta = 0.0f;
            final BlockPos blockPosObj = new BlockPos(blockPos.x, blockPos.y, blockPos.z);
            final IBlockState blockState = this.minecraft.theWorld.getBlockState(blockPosObj);
            block = blockState.getBlock();
            if (block != null) {
                if (block == Blocks.SNOW|| block == Blocks.WOODEN_PRESSURE_PLATE || block == Blocks.STONE_PRESSURE_PLATE || block == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE || block == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                    final AxisAlignedBB bounds = blockState.getBoundingBox(this.minecraft.theWorld, blockPosObj);
                    delta = (float)(bounds.maxY - bounds.minY);
                }
            }

            type = 3 - type;

            GL11.glTranslatef(blockPos.x, blockPos.y + delta, blockPos.z);
            GL11.glCallList(this.list[type * 2 + (ConfigurationHandler.renderSpawns == 1 ? 0 : 1)]);
            GL11.glTranslatef(-blockPos.x, -(blockPos.y + delta), -blockPos.z);
        }

        GL11.glTranslatef(Reference.PLAYER_POSITION.x, Reference.PLAYER_POSITION.y, Reference.PLAYER_POSITION.z);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}
