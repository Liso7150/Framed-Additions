package com.mystic.framed_additions;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.api.block.IFramedBlock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mod(FramedAdditions.MODID)
public class FramedAdditions {
    public static final String MODID = "framed_additions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FramedAdditions() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(Registration.class);
        modEventBus.addListener(FramedRecipePack::addPackFinders);
    }

    private static final class Registration {
        private static final ResourceLocation OAK_PLANKS = new ResourceLocation(MODID, "minecraft/oak_planks");

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onRegister(RegisterEvent event) {
            if (event.getRegistryKey().equals(Registries.BLOCK)) {
                registerBlocks(event);
            } else if (event.getRegistryKey().equals(Registries.ITEM)) {
                registerItems(event);
            } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
                registerBlockEntity(event);
            } else if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
                registerTab(event);
            }
        }

        private static void registerBlocks(RegisterEvent event) {
            List<Block> sources = new ArrayList<>(ForgeRegistries.BLOCKS.getValues());
            sources.sort(Comparator.comparing(block -> {
                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
                return id == null ? "" : id.toString();
            }));

            int registered = 0;
            for (Block source : sources) {
                ResourceLocation sourceId = ForgeRegistries.BLOCKS.getKey(source);
                if (sourceId == null || !shouldCopy(source, sourceId)) {
                    continue;
                }

                ResourceLocation id = new ResourceLocation(MODID, sourceId.getNamespace() + "/" + sourceId.getPath());
                try {
                    Block framed = CopiedFramedBlock.create(source);
                    event.register(Registries.BLOCK, id, () -> framed);
                    FramedRegistry.BLOCKS.add((CopiedFramedBlock) framed);
                    registered++;
                } catch (Throwable exception) {
                    LOGGER.warn("Skipped framed copy of {}", sourceId, exception);
                }
            }
            LOGGER.info("Registered {} framed block copies", registered);
        }

        private static boolean shouldCopy(Block source, ResourceLocation sourceId) {
            if (MODID.equals(sourceId.getNamespace())) {
                return false;
            }
            if (source instanceof IFramedBlock) {
                return false;
            }
            return !source.defaultBlockState().isAir();
        }

        private static void registerItems(RegisterEvent event) {
            for (CopiedFramedBlock block : FramedRegistry.BLOCKS) {
                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
                Item item = new CopiedFramedItem(block);
                event.register(Registries.ITEM, id, () -> item);
                FramedRegistry.ITEMS.add(item);
                if (OAK_PLANKS.equals(id)) {
                    FramedRegistry.ICON = item;
                }
            }
            if (FramedRegistry.ICON == null && !FramedRegistry.ITEMS.isEmpty()) {
                FramedRegistry.ICON = FramedRegistry.ITEMS.get(0);
            }
        }

        private static void registerBlockEntity(RegisterEvent event) {
            if (FramedRegistry.BLOCKS.isEmpty()) {
                return;
            }

            event.register(Registries.BLOCK_ENTITY_TYPE, new ResourceLocation(MODID, "framed_copy"), () -> {
                @SuppressWarnings("unchecked")
                BlockEntityType<FramedBlockEntity>[] holder = new BlockEntityType[1];
                holder[0] = BlockEntityType.Builder.of(
                        (pos, state) -> new FramedBlockEntity(holder[0], pos, state),
                        FramedRegistry.BLOCKS.toArray(Block[]::new)
                ).build(null);
                FramedRegistry.BLOCK_ENTITY = holder[0];
                return holder[0];
            });
        }

        private static void registerTab(RegisterEvent event) {
            event.register(Registries.CREATIVE_MODE_TAB, new ResourceLocation(MODID, "main"), () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.framed_additions"))
                            .icon(() -> FramedRegistry.ICON == null ? new ItemStack(Items.ITEM_FRAME) : new ItemStack(FramedRegistry.ICON))
                            .withSearchBar()
                            .displayItems((parameters, output) -> {
                                for (Item item : FramedRegistry.ITEMS) {
                                    output.accept(item);
                                }
                            })
                            .build()
            );
        }
    }

    @Mod.EventBusSubscriber(modid = MODID)
    private static final class SourceShapes {
        private SourceShapes() {
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                CopiedBlockType.enableSourceShapes();
            }
        }
    }
}
