package com.mystic.framed_additions.client;

import com.mystic.framed_additions.CopiedFramedBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.api.model.BakedModelProxy;
import xfacthd.framedblocks.api.model.data.FramedBlockData;
import xfacthd.framedblocks.api.model.util.ModelCache;

import java.util.ArrayList;
import java.util.List;

public class CopiedFramedModel extends BakedModelProxy {
    private static final RandomSource STABLE_RANDOM = RandomSource.create(0L);
    private final CopiedFramedBlock block;
    private final BakedModel sourceModel;
    private final TextureAtlasSprite frameSprite;

    public CopiedFramedModel(CopiedFramedBlock block, BakedModel sourceModel, BakedModel frameModel) {
        super(frameModel);
        this.block = block;
        this.sourceModel = sourceModel;
        this.frameSprite = frameModel.getParticleIcon();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        FramedBlockData framedData = data.get(FramedBlockData.PROPERTY);
        if (side != null && framedData != null && framedData.isSideHidden(side)) {
            return List.of();
        }

        BlockState camo = framedData == null ? null : framedData.getCamoState();
        boolean bare = camo == null || camo.isAir();
        BlockState sourceState = block.source().defaultBlockState();
        if (bare) {
            if (renderType != null && !sourceLayers(sourceState, rand).contains(renderType)) {
                return List.of();
            }
            return visibleQuads(sourceModel.getQuads(sourceState, side, rand, ModelData.EMPTY, renderType), framedData);
        }

        RenderType layer = primaryLayer(camo);
        if (renderType != null && renderType != layer) {
            return List.of();
        }

        List<BakedQuad> sourceQuads = sourceModel.getQuads(sourceState, side, rand);
        if (sourceQuads.isEmpty()) {
            return baseModel.getQuads(state, side, rand, data, renderType);
        }

        BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camo);
        List<BakedQuad> quads = new ArrayList<>(sourceQuads.size());
        for (BakedQuad quad : sourceQuads) {
            if (quad.getDirection() != null && framedData != null && framedData.isSideHidden(quad.getDirection())) {
                continue;
            }
            quads.add(retexture(quad, spriteFor(quad, camo, camoModel, layer), tintFor(quad, camo, camoModel, layer)));
        }
        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        FramedBlockData framedData = data.get(FramedBlockData.PROPERTY);
        BlockState camo = framedData == null ? null : framedData.getCamoState();
        if (camo == null || camo.isAir()) {
            return sourceLayers(block.source().defaultBlockState(), rand);
        }
        return ChunkRenderTypeSet.of(primaryLayer(camo));
    }

    @Override
    public boolean isGui3d() {
        return sourceModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return sourceModel.usesBlockLight();
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean applyLeftHandTransform) {
        sourceModel.applyTransform(context, poseStack, applyLeftHandTransform);
        return this;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return sourceModel.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        FramedBlockData framedData = data.get(FramedBlockData.PROPERTY);
        BlockState camo = framedData == null ? null : framedData.getCamoState();
        if (camo != null && !camo.isAir()) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(camo).getParticleIcon();
        }
        return sourceModel.getParticleIcon();
    }

    private ChunkRenderTypeSet sourceLayers(BlockState sourceState, RandomSource rand) {
        ChunkRenderTypeSet layers = sourceModel.getRenderTypes(sourceState, rand, ModelData.EMPTY);
        return layers.isEmpty() ? ChunkRenderTypeSet.of(RenderType.solid()) : layers;
    }

    private static List<BakedQuad> visibleQuads(List<BakedQuad> quads, @Nullable FramedBlockData framedData) {
        if (framedData == null) {
            return quads;
        }
        List<BakedQuad> visible = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            if (quad.getDirection() == null || !framedData.isSideHidden(quad.getDirection())) {
                visible.add(quad);
            }
        }
        return visible;
    }

    private static RenderType primaryLayer(BlockState camo) {
        ChunkRenderTypeSet layers = ModelCache.getRenderTypes(camo, STABLE_RANDOM, ModelData.EMPTY);
        if (layers.isEmpty()) {
            return RenderType.solid();
        }
        return layers.iterator().next();
    }

    private TextureAtlasSprite spriteFor(BakedQuad sourceQuad, @Nullable BlockState camo, @Nullable BakedModel camoModel, RenderType layer) {
        if (camoModel == null) {
            return frameSprite;
        }
        BakedQuad sample = sampleQuad(camoModel, camo, sourceQuad.getDirection(), layer);
        return sample == null ? camoModel.getParticleIcon() : sample.getSprite();
    }

    private static int tintFor(BakedQuad sourceQuad, @Nullable BlockState camo, @Nullable BakedModel camoModel, RenderType layer) {
        if (camoModel == null) {
            return -1;
        }
        BakedQuad sample = sampleQuad(camoModel, camo, sourceQuad.getDirection(), layer);
        return sample == null ? -1 : sample.getTintIndex();
    }

    @Nullable
    private static BakedQuad sampleQuad(BakedModel model, BlockState state, @Nullable Direction side, RenderType layer) {
        List<BakedQuad> quads = model.getQuads(state, side, STABLE_RANDOM, ModelData.EMPTY, layer);
        if (quads.isEmpty()) {
            quads = model.getQuads(state, side, STABLE_RANDOM);
        }
        if (quads.isEmpty() && side != null) {
            quads = model.getQuads(state, null, STABLE_RANDOM, ModelData.EMPTY, layer);
            for (BakedQuad quad : quads) {
                if (quad.getDirection() == side) {
                    return quad;
                }
            }
            return quads.isEmpty() ? null : quads.get(0);
        }
        return quads.isEmpty() ? null : quads.get(0);
    }

    private static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite sprite, int tintIndex) {
        TextureAtlasSprite old = quad.getSprite();
        if (old == sprite) {
            return quad;
        }

        int[] vertices = quad.getVertices().clone();
        int stride = vertices.length / 4;
        for (int vertex = 0; vertex < 4; vertex++) {
            int index = vertex * stride;
            float u = Float.intBitsToFloat(vertices[index + 4]);
            float v = Float.intBitsToFloat(vertices[index + 5]);
            vertices[index + 4] = Float.floatToRawIntBits(sprite.getU(old.getUOffset(u)));
            vertices[index + 5] = Float.floatToRawIntBits(sprite.getV(old.getVOffset(v)));
        }
        return new BakedQuad(vertices, tintIndex, quad.getDirection(), sprite, quad.isShade());
    }
}
