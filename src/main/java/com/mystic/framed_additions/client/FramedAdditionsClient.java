package com.mystic.framed_additions.client;

import com.mystic.framed_additions.CopiedBlockType;
import com.mystic.framed_additions.FramedAdditions;
import com.mystic.framed_additions.FramedRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xfacthd.framedblocks.api.block.FramedBlockEntity;

@Mod.EventBusSubscriber(modid = FramedAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FramedAdditionsClient {
    private FramedAdditionsClient() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(net.minecraftforge.event.AddPackFindersEvent event) {
        DynamicFramedResources.add(event);
    }

    @SubscribeEvent
    public static void onGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("copy", CopyGeometry.LOADER);
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

    @Mod.EventBusSubscriber(modid = FramedAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    static final class SourceShapes {
        private SourceShapes() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                CopiedBlockType.enableSourceShapes();
            }
        }
    }
}
