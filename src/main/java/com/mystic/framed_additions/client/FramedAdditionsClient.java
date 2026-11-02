package com.mystic.framed_additions.client;

import com.mystic.framed_additions.CopiedFramedBlock;
import com.mystic.framed_additions.FramedAdditions;
import com.mystic.framed_additions.FramedRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import xfacthd.framedblocks.api.block.FramedBlockEntity;

import java.util.Map;

@Mod.EventBusSubscriber(modid = FramedAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FramedAdditionsClient {
    private FramedAdditionsClient() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(net.minecraftforge.event.AddPackFindersEvent event) {
        DynamicFramedResources.add(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
        for (CopiedFramedBlock block : FramedRegistry.BLOCKS) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id == null) {
                continue;
            }

            BlockState sourceState = block.source().defaultBlockState();
            BakedModel sourceModel = models.get(BlockModelShaper.stateToModelLocation(sourceState));
            if (sourceModel == null) {
                sourceModel = missing;
            }

            ModelResourceLocation framedLocation = BlockModelShaper.stateToModelLocation(block.defaultBlockState());
            BakedModel frameModel = models.get(framedLocation);
            if (frameModel == null) {
                frameModel = missing;
            }

            BakedModel wrapped = new CopiedFramedModel(block, sourceModel, frameModel);
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                models.put(BlockModelShaper.stateToModelLocation(state), wrapped);
            }
            models.put(new ModelResourceLocation(id, "inventory"), wrapped);
        }
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        if (FramedRegistry.BLOCKS.isEmpty()) {
            return;
        }
        event.register((state, level, pos, tint) -> {
            if (level == null || pos == null) {
                return 0xFFFFFF;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FramedBlockEntity framed) {
                BlockState camo = framed.getCamo().getState();
                if (!camo.isAir()) {
                    return Minecraft.getInstance().getBlockColors().getColor(camo, level, pos, tint);
                }
            }
            return 0xFFFFFF;
        }, FramedRegistry.BLOCKS.toArray(Block[]::new));
    }
}
