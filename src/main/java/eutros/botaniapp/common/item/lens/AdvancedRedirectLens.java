package eutros.botaniapp.common.item.lens;

import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import vazkii.botania.api.internal.IManaBurst;

import java.util.Optional;

public class AdvancedRedirectLens extends ItemLens {

    public AdvancedRedirectLens(Properties properties) {
        super(properties);
    }

    @Override
    public boolean collideBurst(IManaBurst burst, RayTraceResult rtr, boolean isManaBlock, boolean dead, ItemStack stack) {
        BlockPos coords = burst.getBurstSourceBlockPos();
        ThrowableEntity entity = (ThrowableEntity) burst;
        World world = entity.getEntityWorld();
        if(!entity.world.isRemote && rtr.getType() == RayTraceResult.Type.BLOCK
                && !((BlockRayTraceResult) rtr).getPos().equals(coords)) {
            if(!burst.isFake()) {
                Direction face = ((BlockRayTraceResult) rtr).getFace();
                BlockPos pos = ((BlockRayTraceResult) rtr).getPos();

                BlockState oldState = world.getBlockState(pos);
                BlockState newState = rotate(oldState, face);
                if(newState == oldState)
                    newState = mirror(oldState);

                world.setBlockState(pos, newState);
            }
        }

        return dead;
    }

    @SuppressWarnings("unchecked")
    private static Optional<IProperty<Direction>> getDirection(BlockState state) {
        for (IProperty<?> prop : state.getProperties()) {
            if (prop.getName().equals("facing") && prop.getValueClass() == Direction.class) {
                return Optional.of((IProperty<Direction>) prop);
            }
        }
        return Optional.empty();
    }

    private static BlockState rotate(BlockState state, Direction direction) {
        Optional<IProperty<Direction>> facing = getDirection(state);

        if(!facing.isPresent())
            return state;

        IProperty<Direction> prop = facing.get();
        if (state.get(prop) != direction &&
                prop.getAllowedValues().contains(direction))
            return state.with(prop, direction);

        return state;
    }

    private static BlockState mirror(BlockState state) {
        Optional<IProperty<Direction>> dir = getDirection(state);

        if(!dir.isPresent())
            return state;

        IProperty<Direction> prop = dir.get();
        Direction opposite = state.get(prop).getOpposite();
        if(prop.getAllowedValues().contains(opposite))
            return state.with(prop, opposite);

        return state;
    }

}
