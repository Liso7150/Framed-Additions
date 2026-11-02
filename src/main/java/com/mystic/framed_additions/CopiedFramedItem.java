package com.mystic.framed_additions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class CopiedFramedItem extends BlockItem {
    public CopiedFramedItem(CopiedFramedBlock block) {
        super(block, new Properties());
    }

    @Override
    public Component getName(ItemStack stack) {
        return ((CopiedFramedBlock) getBlock()).getName();
    }
}
