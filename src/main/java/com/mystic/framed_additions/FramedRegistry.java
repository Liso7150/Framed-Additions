package com.mystic.framed_additions;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import xfacthd.framedblocks.api.block.FramedBlockEntity;

import java.util.ArrayList;
import java.util.List;

public final class FramedRegistry {
    public static final List<CopiedFramedBlock> BLOCKS = new ArrayList<>();
    public static final List<Item> ITEMS = new ArrayList<>();
    public static Item ICON;
    public static BlockEntityType<FramedBlockEntity> BLOCK_ENTITY;

    private FramedRegistry() {
    }
}
