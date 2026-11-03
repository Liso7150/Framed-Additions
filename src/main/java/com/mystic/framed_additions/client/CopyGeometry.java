package com.mystic.framed_additions.client;

import com.mystic.framed_additions.CopiedFramedBlock;
import com.mystic.framed_additions.FramedAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class CopyGeometry implements IUnbakedGeometry<CopyGeometry> {
    static final IGeometryLoader<CopyGeometry> LOADER = (json, context) -> {
        if (!json.has("source")) {
            return new CopyGeometry(null);
        }
        ResourceLocation sourceId;
        try {
            sourceId = new ResourceLocation(json.get("source").getAsString());
        } catch (RuntimeException exception) {
            return new CopyGeometry(null);
        }
        ResourceLocation copyId = new ResourceLocation(FramedAdditions.MODID, sourceId.getNamespace() + "/" + sourceId.getPath());
        Block copy = ForgeRegistries.BLOCKS.getValue(copyId);
        return new CopyGeometry(copy instanceof CopiedFramedBlock framed ? framed : null);
    };

    @Nullable
    private final CopiedFramedBlock block;

    private CopyGeometry(@Nullable CopiedFramedBlock block) {
        this.block = block;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        // The source model is resolved on first use. Loading it here would bake every block during a dynamic-resources reload.
        if (block == null) {
            return Minecraft.getInstance().getModelManager().getMissingModel();
        }
        return new LazyCopiedModel(block);
    }
}
