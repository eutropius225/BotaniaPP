/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [May 20, 2014, 10:56:14 PM (GMT)]
 */
package eutros.botaniapp.common.item;

import com.google.common.math.BigIntegerMath;
import eutros.botaniapp.common.core.handler.TerraPickMiningHandler;
import eutros.botaniapp.common.core.helper.ItemNBTHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.ISequentialBreaker;
import vazkii.botania.api.mana.IManaGivingItem;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.helper.PlayerHelper;
import vazkii.botania.common.item.ItemTemperanceStone;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelPick;
import vazkii.botania.common.item.relic.ItemThorRing;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class ItemTerraPickPP extends ItemManasteelPick implements IManaItem, ISequentialBreaker {

    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    private static final String TAG_ENABLED = "enabled";
    private static final String TAG_MANA = "mana";
    private static final String TAG_TIPPED = "tipped";
    public static int OLD_MAX = 5;
    public static final List<Material> MATERIALS = Arrays.asList(Material.ROCK, Material.IRON, Material.ICE,
            Material.GLASS, Material.PISTON, Material.ANVIL, Material.ORGANIC, Material.EARTH, Material.SAND,
            Material.SNOW, Material.SNOW_BLOCK, Material.CLAY);

    private static final int[] CREATIVE_MANA = new int[] {
            10000 - 1, 1000000 - 1, 10000000 - 1, 100000000 - 1, 1000000000 - 1, Integer.MAX_VALUE - 1
    };
    private static final int MANA_PER_DAMAGE = 100;

    public ItemTerraPickPP(Properties props) {
        super(BotaniaAPI.TERRASTEEL_ITEM_TIER, props, -2.8F);
        addPropertyOverride(new ResourceLocation("botania", TAG_TIPPED), (itemStack, world, entityLivingBase) ->
                isTipped(itemStack) ?
                1 :
                0);
        addPropertyOverride(new ResourceLocation("botania", TAG_ENABLED), (itemStack, world, entityLivingBase) ->
                isEnabled(itemStack) ?
                1 :
                0);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup tab, @Nonnull NonNullList<ItemStack> list) {
        if(isInGroup(tab)) {
            for(int mana : CREATIVE_MANA) {
                ItemStack stack = new ItemStack(this);
                setTrueMana(stack, BigInteger.valueOf(mana));
                list.add(stack);
            }
            ItemStack stack = new ItemStack(this);
            setTrueMana(stack, BigInteger.valueOf(CREATIVE_MANA[1]));
            setTipped(stack);
            list.add(stack);
        }
    }

    @Override
    public void breakOtherBlock(PlayerEntity player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
        if(!isEnabled(stack))
            return;

        World world = player.world;
        Material mat = world.getBlockState(pos).getMaterial();
        if(!MATERIALS.contains(mat))
            return;

        if(world.isAirBlock(pos))
            return;

        boolean thor = !ItemThorRing.getThorRing(player).isEmpty();
        boolean doX = thor || side.getXOffset() == 0;
        boolean doY = thor || side.getYOffset() == 0;
        boolean doZ = thor || side.getZOffset() == 0;

        int origLevel = getLevel(stack);
        int level = origLevel + (thor ? 1 : 0);
        if(ItemTemperanceStone.hasTemperanceActive(player) && level > 2)
            level = 2;

        int range = level - 1;
        int rangeY = Math.max(1, range);

        if(range == 0 && level != 1)
            return;

        Vec3i beginDiff = new Vec3i(doX ? -range : 0, doY ? -1 : 0, doZ ? -range : 0);
        Vec3i endDiff = new Vec3i(doX ? range : 0, doY ? rangeY * 2 - 1 : 0, doZ ? range : 0);

        if(level > 6) {
            BlockPos truePos = pos;
            int depth = thor ? range + 1 : 1;
            switch(side) {
                case NORTH:
                case SOUTH:
                case EAST:
                case WEST:
                    pos = pos.offset(Direction.UP, range - 1);
                    break;
                case DOWN:
                    if(thor) depth = range * 2 - 1;
                    break;
                case UP:
                    if(thor) depth = 2;
                    break;
            }
            TerraPickMiningHandler.createEvent(player,
                    stack,
                    world,
                    pos,
                    range,
                    thor ?
                    depth :
                    1,
                    isTipped(stack),
                    side,
                    truePos);
            if(thor)
                TerraPickMiningHandler.createEvent(player,
                        stack,
                        world,
                        pos.offset(side),
                        range,
                        range * 2 + 1 - depth,
                        isTipped(stack),
                        side.getOpposite(),
                        truePos);
        } else {
            ToolCommons.removeBlocksInIteration(player, stack, world, pos, beginDiff, endDiff, state -> MATERIALS.contains(state.getMaterial()), isTipped(stack));
        }

        if(origLevel >= 5) {
            PlayerHelper.grantCriterion((ServerPlayerEntity) player, new ResourceLocation("botania:challenge/rank_ss_pick"), "code_triggered");
        }
    }

    @Override
    public boolean disposeOfTrashBlocks(ItemStack stack) {
        return isTipped(stack);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> stacks, ITooltipFlag flags) {
        int level = getLevel(stack);
        ITextComponent rank = new TranslationTextComponent("botania.rank" + Math.min(5, level));

        int pluses = 0;
        if(level >= OLD_MAX)
            pluses = (level - OLD_MAX) + 1;
        rank.appendText(new String(new char[pluses]).replace("\0", "+"));

        ITextComponent rankFormat = new TranslationTextComponent("botaniamisc.toolRank", rank);
        stacks.add(rankFormat);
        if(getTrueMana(stack).compareTo(MAX_INT) >= 0)
            stacks.add(new TranslationTextComponent("botaniamisc.getALife").applyTextStyle(TextFormatting.RED));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);

        getMana(stack);
        int level = getLevel(stack);

        if(level != 0) {
            setEnabled(stack, !isEnabled(stack));
            if(!world.isRemote)
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.terraPickMode, SoundCategory.PLAYERS, 0.5F, 0.4F);
        }

        return ActionResult.success(stack);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        return ctx.getPlayer() == null || ctx.getPlayer().isSneaking() ? super.onItemUse(ctx)
                                                                       : ActionResultType.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if(isEnabled(stack)) {
            int level = getLevel(stack);

            if(level == 0)
                setEnabled(stack, false);
            else if(entity instanceof PlayerEntity && !((PlayerEntity) entity).isSwingInProgress)
                addMana(stack, -level);
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, PlayerEntity player) {
        BlockRayTraceResult raycast = ToolCommons.raytraceFromEntity(player, 10, false);
        if(!player.world.isRemote && raycast.getType() == RayTraceResult.Type.BLOCK) {
            Direction face = raycast.getFace();
            breakOtherBlock(player, stack, pos, pos, face);
            BotaniaAPI.internalHandler.breakOnAllCursors(player, this, stack, pos, face);
        }

        return false;
    }

    public int getMana(ItemStack stack) {
        return 0;
    }

    @Override
    public int getMaxMana(ItemStack itemStack) {
        return Integer.MAX_VALUE;
    }

    public static void setTrueMana(ItemStack stack, BigInteger mana) {
        ItemNBTHelper.setString(stack, TAG_MANA, mana.toString());
    }

    public static BigInteger getTrueMana(ItemStack stack) {
        String s = ItemNBTHelper.getString(stack, TAG_MANA, "0");
        return new BigInteger(s.length() == 0 ? "0" : s);
    }

    public void setEnabled(ItemStack stack, boolean enabled) {
        ItemNBTHelper.setBoolean(stack, TAG_ENABLED, enabled);
    }

    public static int getLevel(ItemStack stack) {
        BigInteger mana = getTrueMana(stack);

        int level = BigIntegerMath.log10(mana.max(BigInteger.ONE), RoundingMode.FLOOR);

        return Math.max(0, level - 4);
    }

    @Override
    public void addMana(ItemStack stack, int mana) {
        setTrueMana(stack, getTrueMana(stack).add(BigInteger.valueOf(mana)));
    }

    @Override
    public boolean canReceiveManaFromPool(ItemStack stack, TileEntity pool) {
        return true;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack stack, ItemStack otherStack) {
        return !(otherStack.getItem() instanceof IManaGivingItem);
    }

    @Override
    public boolean canExportManaToPool(ItemStack stack, TileEntity tileEntity) {
        return false;
    }

    @Override
    public boolean canExportManaToItem(ItemStack stack, ItemStack otherStack) {
        return false;
    }

    @Override
    public boolean isNoExport(ItemStack stack) {
        return true;
    }

    public static boolean isTipped(ItemStack stack) {
        return ItemNBTHelper.getBoolean(stack, TAG_TIPPED, false);
    }

    public static void setTipped(ItemStack stack) {
        ItemNBTHelper.setBoolean(stack, TAG_TIPPED, true);
    }

    public static boolean isEnabled(ItemStack stack) {
        return ItemNBTHelper.getBoolean(stack, TAG_ENABLED, false);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack before, @Nonnull ItemStack after, boolean slotChanged) {
        return after.getItem() != this || isEnabled(before) != isEnabled(after);
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getManaPerDamage() {
        return MANA_PER_DAMAGE;
    }

}