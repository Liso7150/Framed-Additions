package com.mystic.framed_additions;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import xfacthd.framedblocks.api.predicate.contex.ConTexMode;
import xfacthd.framedblocks.api.predicate.contex.ConnectionPredicate;
import xfacthd.framedblocks.api.predicate.cull.SideSkipPredicate;
import xfacthd.framedblocks.api.predicate.fullface.FullFacePredicate;
import xfacthd.framedblocks.api.shapes.ShapeProvider;
import xfacthd.framedblocks.api.shapes.SingleShapeProvider;
import xfacthd.framedblocks.api.type.IBlockType;

public final class CopiedBlockType implements IBlockType {
    private final String name;
    private final VoxelShape shape;
    private final boolean fullBlock;
    private final FullFacePredicate fullFace;

    private CopiedBlockType(Block source) {
        this.name = String.valueOf(ForgeRegistries.BLOCKS.getKey(source));
        this.shape = shapeOf(source);
        this.fullBlock = isFullBlock(this.shape);
        this.fullFace = (state, side) -> Block.isFaceFull(shape, side);
    }

    public static CopiedBlockType of(Block source) {
        return new CopiedBlockType(source);
    }

    public VoxelShape shape() {
        return shape;
    }

    private static VoxelShape shapeOf(Block source) {
        BlockState state = source.defaultBlockState();
        try {
            VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
            return shape == null ? Shapes.block() : shape;
        } catch (RuntimeException exception) {
            FramedAdditions.LOGGER.warn("Using a full cube shape for {}: {}", ForgeRegistries.BLOCKS.getKey(source), exception.toString());
            return Shapes.block();
        }
    }

    private static boolean isFullBlock(VoxelShape shape) {
        try {
            return Block.isShapeFullBlock(shape);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Override
    public boolean canOccludeWithSolidCamo() {
        return fullBlock;
    }

    @Override
    public boolean hasSpecialHitbox() {
        return false;
    }

    @Override
    public FullFacePredicate getFullFacePredicate() {
        return fullFace;
    }

    @Override
    public SideSkipPredicate getSideSkipPredicate() {
        return SideSkipPredicate.FALSE;
    }

    @Override
    public ConnectionPredicate getConnectionPredicate() {
        return ConnectionPredicate.FALSE;
    }

    @Override
    public ShapeProvider generateShapes(ImmutableList<BlockState> states) {
        return new SingleShapeProvider(states, shape);
    }

    @Override
    public boolean hasSpecialTile() {
        return false;
    }

    @Override
    public boolean hasBlockItem() {
        return true;
    }

    @Override
    public boolean supportsWaterLogging() {
        return false;
    }

    @Override
    public boolean supportsConnectedTextures() {
        return false;
    }

    @Override
    public ConTexMode getMinimumConTexMode() {
        return ConTexMode.NONE;
    }

    @Override
    public boolean allowMakingIntangible() {
        return false;
    }

    @Override
    public boolean canLockState() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(IBlockType other) {
        return getName().compareTo(other.getName());
    }
}
