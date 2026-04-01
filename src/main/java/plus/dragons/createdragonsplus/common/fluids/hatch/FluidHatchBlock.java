/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.common.fluids.hatch;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidHelper.FluidExchange;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;

public class FluidHatchBlock extends HorizontalDirectionalBlock implements IBE<FluidHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {

    public FluidHatchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, WATERLOGGED));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        if (context.getClickedFace().getAxis().isVertical())
            return null;
        return withWater(state.setValue(FACING, context.getClickedFace().getOpposite()), context);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return state;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty())
            return super.use(state, level, pos, player, hand, hitResult);

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (player instanceof FakePlayer)
            return InteractionResult.PASS;

        BlockEntity blockEntity = level.getBlockEntity(pos.relative(state.getValue(FACING)));
        if (blockEntity == null)
            return InteractionResult.FAIL;

        // Query the adjacent block's fluid capability on the face the hatch is mounted to,
        // so sided fluid handlers (e.g. Create tanks) accept the interaction.
        Direction querySide = state.getValue(FACING).getOpposite();
        IFluidHandler tankCapability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, querySide).orElse(null);
        if (tankCapability == null)
            return InteractionResult.FAIL;

        FilteringBehaviour filter = BlockEntityBehaviour.get(level, pos, FilteringBehaviour.TYPE);
        if (filter == null)
            return InteractionResult.FAIL;

        FluidExchange exchange;
        FluidStack fluidStack;
        if (!(fluidStack = tryEmptyItem(level, player, hand, stack, blockEntity, tankCapability, filter)).isEmpty()) {
            exchange = FluidExchange.ITEM_TO_TANK;
        } else if (!(fluidStack = tryFillItem(level, player, hand, stack, blockEntity, tankCapability, filter)).isEmpty()) {
            exchange = FluidExchange.TANK_TO_ITEM;
        } else {
            if (GenericItemEmptying.canItemBeEmptied(level, stack) || GenericItemFilling.canItemBeFilled(level, stack))
                return InteractionResult.SUCCESS;
            return InteractionResult.FAIL;
        }

        SoundEvent soundevent = switch (exchange) {
            case ITEM_TO_TANK -> FluidHelper.getEmptySound(fluidStack);
            case TANK_TO_ITEM -> FluidHelper.getFillSound(fluidStack);
        };
        if (soundevent != null && !level.isClientSide) {
            float pitch = Mth.clamp(1 - (fluidStack.getAmount() / (FluidTankBlockEntity.getCapacityMultiplier() * 16f)), 0, 1);
            pitch /= 1.5f;
            pitch += .5f;
            pitch += (level.random.nextFloat() - .5f) / 4f;
            level.playSound(null, pos, soundevent, SoundSource.BLOCKS, .5f, pitch);
        }

        return InteractionResult.SUCCESS;
    }

    public FluidStack tryEmptyItem(
            Level level, Player player, InteractionHand hand, ItemStack stack,
            BlockEntity blockEntity, IFluidHandler capability, FilteringBehaviour filter) {
        if (!GenericItemEmptying.canItemBeEmptied(level, stack))
            return FluidStack.EMPTY;

        Pair<FluidStack, ItemStack> emptying = GenericItemEmptying.emptyItem(level, stack, true);
        FluidStack fluidStack = emptying.getFirst();

        if (!filter.test(fluidStack))
            return FluidStack.EMPTY;

        if (fluidStack.getAmount() != capability.fill(fluidStack, FluidAction.SIMULATE))
            return FluidStack.EMPTY;
        if (level.isClientSide)
            return fluidStack;

        ItemStack copy = stack.copy();
        emptying = GenericItemEmptying.emptyItem(level, copy, false);

        // Re-simulate fill to guard against capability state changes between the
        // first simulate (above) and now. If the tank can no longer accept the
        // fluid, abort without consuming the item (don't update player hand).
        int realFill = capability.fill(fluidStack.copy(), FluidAction.SIMULATE);
        if (realFill != fluidStack.getAmount())
            return FluidStack.EMPTY;
        capability.fill(fluidStack.copy(), FluidAction.EXECUTE);
        blockEntity.setChanged();

        if (level instanceof ServerLevel serverLevel)
            serverLevel.getChunkSource().blockChanged(blockEntity.getBlockPos());

        if (!player.isCreative() && !(blockEntity instanceof CreativeFluidTankBlockEntity)) {
            if (copy.isEmpty()) {
                player.setItemInHand(hand, emptying.getSecond());
            } else {
                player.setItemInHand(hand, copy);
                player.getInventory().placeItemBackInInventory(emptying.getSecond());
            }
        }
        return fluidStack;
    }

    public FluidStack tryFillItem(Level level, Player player, InteractionHand hand, ItemStack stack, BlockEntity blockEntity, IFluidHandler capability, FilteringBehaviour filter) {
        if (!GenericItemFilling.canItemBeFilled(level, stack))
            return FluidStack.EMPTY;

        for (int i = 0; i < capability.getTanks(); i++) {
            FluidStack fluidStack = capability.getFluidInTank(i);
            if (fluidStack.isEmpty() || !filter.test(fluidStack))
                continue;
            int requiredAmountForItem = GenericItemFilling.getRequiredAmountForItem(level, stack, fluidStack.copy());
            if (requiredAmountForItem == -1)
                continue;
            if (requiredAmountForItem > fluidStack.getAmount())
                continue;

            if (level.isClientSide)
                return fluidStack;

            FluidStack fluidCopy = fluidStack.copy();
            fluidCopy.setAmount(requiredAmountForItem);

            // Re-simulate drain to guard against capability state changes between
            // the initial check and now. If the tank can no longer provide the
            // fluid, abort without consuming the item.
            FluidStack realDraw = capability.drain(fluidCopy.copy(), FluidAction.SIMULATE);
            if (realDraw.isEmpty() || realDraw.getAmount() < requiredAmountForItem)
                return FluidStack.EMPTY;

            // Use a copy for fillItem so the original stack is untouched until
            // we confirm the drain succeeds.
            ItemStack stackForFill = stack.copy();
            ItemStack result = GenericItemFilling.fillItem(level, requiredAmountForItem, stackForFill, fluidStack.copy());

            capability.drain(fluidCopy, FluidAction.EXECUTE);

            if (!player.isCreative() && !(blockEntity instanceof CreativeFluidTankBlockEntity)) {
                stack.shrink(1);
                if (stack.isEmpty())
                    player.setItemInHand(hand, result);
                else player.getInventory().placeItemBackInInventory(result);
            }
            blockEntity.setChanged();
            if (level instanceof ServerLevel serverLevel)
                serverLevel.getChunkSource().blockChanged(blockEntity.getBlockPos());
            return fluidCopy;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.ITEM_HATCH.get(state.getValue(FACING).getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public Class<FluidHatchBlockEntity> getBlockEntityClass() {
        return FluidHatchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidHatchBlockEntity> getBlockEntityType() {
        return CDPBlockEntities.FLUID_HATCH.get();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pathComputationType) {
        return false;
    }
}
