package com.mystic.framed_additions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import xfacthd.framedblocks.api.block.AbstractFramedBlock;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.api.block.FramedProperties;
import xfacthd.framedblocks.api.block.IFramedBlock;

import java.util.ArrayList;
import java.util.List;

public class CopiedFramedBlock extends AbstractFramedBlock {
    private static final ThreadLocal<CopiedBlockType> BUILDING = new ThreadLocal<>();
    private final Block source;

    public static CopiedFramedBlock create(Block source) {
        CopiedBlockType type = CopiedBlockType.of(source);
        BUILDING.set(type);
        try {
            return new CopiedFramedBlock(source, type);
        } finally {
            BUILDING.remove();
        }
    }

    private CopiedFramedBlock(Block source, CopiedBlockType type) {
        super(type, IFramedBlock.createProperties(type));
        this.source = source;
    }

    public Block source() {
        return source;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        CopiedBlockType type = BUILDING.get();
        if (type != null && type.canOccludeWithSolidCamo()) {
            builder.add(FramedProperties.SOLID);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FramedBlockEntity(FramedRegistry.BLOCK_ENTITY, pos, state);
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable("item.framed_additions.copy", source.getName());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        if (asItem() != Items.AIR) {
            drops.add(new ItemStack(this));
        }
        return getCamoDrops(drops, builder);
    }
}
