package com.mystic.framed_additions.client;

import com.mystic.framed_additions.CopiedFramedBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.api.model.BakedModelProxy;

import java.util.List;

public final class LazyCopiedModel extends BakedModelProxy {
    private static final ThreadLocal<Boolean> RESOLVING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final CopiedFramedBlock block;
    private volatile CopiedFramedModel resolved;

    public LazyCopiedModel(CopiedFramedBlock block) {
        super(Minecraft.getInstance().getModelManager().getMissingModel());
        this.block = block;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        CopiedFramedModel model = model();
        return model == null ? List.of() : model.getQuads(state, side, rand);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        CopiedFramedModel model = model();
        return model == null ? List.of() : model.getQuads(state, side, rand, data, renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        CopiedFramedModel model = model();
        return model == null ? super.getRenderTypes(state, rand, data) : model.getRenderTypes(state, rand, data);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        CopiedFramedModel model = model();
        return model == null ? super.getRenderTypes(stack, fabulous) : model.getRenderTypes(stack, fabulous);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        CopiedFramedModel model = model();
        return model == null ? super.getRenderPasses(stack, fabulous) : model.getRenderPasses(stack, fabulous);
    }

    @Override
    public boolean useAmbientOcclusion() {
        CopiedFramedModel model = model();
        return model == null ? super.useAmbientOcclusion() : model.useAmbientOcclusion();
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        CopiedFramedModel model = model();
        return model == null ? super.useAmbientOcclusion(state) : model.useAmbientOcclusion(state);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
        CopiedFramedModel model = model();
        return model == null ? super.useAmbientOcclusion(state, renderType) : model.useAmbientOcclusion(state, renderType);
    }

    @Override
    public boolean isGui3d() {
        CopiedFramedModel model = model();
        return model == null ? super.isGui3d() : model.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        CopiedFramedModel model = model();
        return model == null ? super.usesBlockLight() : model.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        CopiedFramedModel model = model();
        return model == null ? super.isCustomRenderer() : model.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        CopiedFramedModel model = model();
        return model == null ? super.getParticleIcon() : model.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        CopiedFramedModel model = model();
        return model == null ? super.getParticleIcon(data) : model.getParticleIcon(data);
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        CopiedFramedModel model = model();
        return model == null ? super.getTransforms() : model.getTransforms();
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean applyLeftHandTransform) {
        CopiedFramedModel model = model();
        if (model == null) {
            return super.applyTransform(context, poseStack, applyLeftHandTransform);
        }
        model.applyTransform(context, poseStack, applyLeftHandTransform);
        return this;
    }

    @Nullable
    private CopiedFramedModel model() {
        CopiedFramedModel current = resolved;
        if (current != null || RESOLVING.get()) {
            return current;
        }
        RESOLVING.set(Boolean.TRUE);
        try {
            current = create();
        } finally {
            RESOLVING.set(Boolean.FALSE);
        }
        resolved = current;
        return current;
    }

    private CopiedFramedModel create() {
        return new CopiedFramedModel(block);
    }
}
