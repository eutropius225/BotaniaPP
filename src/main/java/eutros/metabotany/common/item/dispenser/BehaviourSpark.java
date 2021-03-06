package eutros.metabotany.common.item.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;

public class BehaviourSpark extends DefaultDispenseItemBehavior {

    @NotNull
    @Override
    protected ItemStack dispenseStack(IBlockSource source, @NotNull ItemStack stack) {
        World world = source.getWorld();
        Direction facing = world.getBlockState(source.getBlockPos()).get(DispenserBlock.FACING);
        BlockPos pos = source.getBlockPos().offset(facing);
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof ISparkAttachable) {
            ISparkAttachable attach = (ISparkAttachable) tile;
            if(attach.canAttachSpark(stack) && attach.getAttachedSpark() == null) {
                if(!world.isRemote) {
                    stack.shrink(1);
                    EntityType<?> sparkType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("botania", "spark"));
                    if(sparkType == null) return super.dispenseStack(source, stack);

                    Entity spark = sparkType.create(world);

                    if(spark == null) return super.dispenseStack(source, stack);

                    spark.setPosition(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
                    world.addEntity(spark);
                    attach.attachSpark((ISparkEntity) spark);
                    VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
                }
                return stack;
            }
        }

        return super.dispenseStack(source, stack);
    }

}
