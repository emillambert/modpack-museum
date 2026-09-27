package net.modpackmuseum.core.container;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Minimal reusable Container implementation backed by a fixed NonNullList. */
public interface MuseumContainer extends Container {
    NonNullList<ItemStack> items();

    @Override
    default int getContainerSize() {
        return items().size();
    }

    @Override
    default boolean isEmpty() {
        for (ItemStack stack : items()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default ItemStack getItem(int slot) {
        return items().get(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items(), slot, count);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items(), slot);
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        items().set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    default void clearContent() {
        for (int i = 0; i < items().size(); i++) {
            items().set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    default boolean stillValid(Player player) {
        return true;
    }
}
