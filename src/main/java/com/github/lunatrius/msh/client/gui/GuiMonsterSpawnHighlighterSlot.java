package com.github.lunatrius.msh.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.github.lunatrius.msh.entity.SpawnCondition;
import com.github.lunatrius.msh.handler.ConfigurationHandler;
import com.github.lunatrius.msh.proxy.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLiving;

public class GuiMonsterSpawnHighlighterSlot extends GuiSlot {
    private final GuiMonsterSpawnHighlighter guiMonsterSpawnHighlighter;
    private final TextureManager renderEngine;
    private final FontRenderer fontRenderer;
    private final List<SpawnCondition> spawnConditions;

    public GuiMonsterSpawnHighlighterSlot(Minecraft minecraft, GuiMonsterSpawnHighlighter guiMonsterSpawnHighlighter) {
        super(minecraft, guiMonsterSpawnHighlighter.width, guiMonsterSpawnHighlighter.height, 16, guiMonsterSpawnHighlighter.height - 30, 24);
        this.guiMonsterSpawnHighlighter = guiMonsterSpawnHighlighter;
        this.renderEngine = minecraft.renderEngine;
        this.fontRenderer = minecraft.fontRendererObj;
        this.spawnConditions = SpawnCondition.SPAWN_CONDITIONS;
    }

    @Override
    protected int getSize() {
        return this.spawnConditions.size();
    }

    @Override
    protected void elementClicked(int index, boolean isDoubleClick, int a, int b) {
        if (index < 0 || index >= this.spawnConditions.size()) {
            return;
        }

        SpawnCondition spawnCondition = this.spawnConditions.get(index);
        spawnCondition.enabled = !spawnCondition.enabled;

        ConfigurationHandler.setEntityEnabled(spawnCondition.name, spawnCondition.enabled);
    }

    @Override
    protected boolean isSelected(int index) {
        return !(index < 0 || index >= this.spawnConditions.size()) && this.spawnConditions.get(index).enabled;

    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {
    }

    @Override
    protected void drawSlot(int index, int x, int y, int insideSlotHeight, int mouseXIn, int mouseYIn) {
        if (index < 0 || index >= this.spawnConditions.size()) {
            return;
        }

        drawEntity(x, y, this.spawnConditions.get(index).entity);
        this.guiMonsterSpawnHighlighter.drawString(
        	this.fontRenderer, this.spawnConditions.get(index).entity.getName(), x + 24, y + 6, 0x00FFFFFF);
    }

    private void drawEntity(int x, int y, EntityLiving entityLiving) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator tess = Tessellator.getInstance();

        this.renderEngine.bindTexture(Gui.STAT_ICONS);
        drawEntitySlot(tess, x, y);

        TextureInformation ti = ClientProxy.ENTITY_ICONS.get(entityLiving.getClass());
        if (ti != null) {
            this.renderEngine.bindTexture(ti.resourceLocation);
            drawTextureParts(tess, x, y, ti);

            if (ti.resourceSpecial != null) {
                this.renderEngine.bindTexture(ti.resourceSpecial);
                drawTextureParts(tess, x, y, ti);
            }
        }
    }

    private void drawEntitySlot(Tessellator tess, int x, int y) {
    	final VertexBuffer buffer = tess.getBuffer();
    	buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    	buffer.pos(x + 1, y + 19, 0).tex(0 * 0.0078125, 18 * 0.0078125).endVertex();
    	buffer.pos(x + 19, y + 19, 0).tex(18 * 0.0078125, 18 * 0.0078125).endVertex();
    	buffer.pos(x + 19, y + 1, 0).tex(18 * 0.0078125, 0 * 0.0078125).endVertex();
    	buffer.pos(x + 1, y + 1, 0).tex(0 * 0.0078125, 0 * 0.0078125).endVertex();
        tess.draw();
    }

    private void drawTextureParts(Tessellator tess, int x, int y, TextureInformation ti) {
    	final VertexBuffer buffer = tess.getBuffer();
    	buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (TextureInformation.TexturePart tp : ti.textureParts) {
        	buffer.pos(x + 2 + tp.x, y + 2 + tp.y + tp.height, 0).tex(tp.srcX / ti.width, (tp.srcY + tp.srcHeight) / ti.height).endVertex();
        	buffer.pos(x + 2 + tp.x + tp.width, y + 2 + tp.y + tp.height, 0).tex((tp.srcX + tp.srcWidth) / ti.width, (tp.srcY + tp.srcHeight) / ti.height).endVertex();
        	buffer.pos(x + 2 + tp.x + tp.width, y + 2 + tp.y, 0).tex((tp.srcX + tp.srcWidth) / ti.width, tp.srcY / ti.height).endVertex();
        	buffer.pos(x + 2 + tp.x, y + 2 + tp.y, 0).tex(tp.srcX / ti.width, tp.srcY / ti.height).endVertex();
        }
        tess.draw();
    }
}
