/*
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets
 * Copyright (C) dmulloy2 <http://dmulloy2.net>
 * Copyright (C) Kristian S. Strangeland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.lukel.silhouetteserver.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WrapperPlayServerEntityEquipment extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_EQUIPMENT;

    public WrapperPlayServerEntityEquipment() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerEntityEquipment(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Retrieve Entity ID.
     * <p>
     * Notes: entity's ID
     *
     * @return The current Entity ID
     */
    public int getEntityID() {
        return handle.getIntegers().read(0);
    }

    /**
     * Set Entity ID.
     *
     * @param value - new value.
     */
    public void setEntityID(int value) {
        handle.getIntegers().write(0, value);
    }

    /**
     * Retrieve the entity whose equipment will be changed.
     *
     * @param world - the current world of the entity.
     * @return The affected entity.
     */
    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    /**
     * Retrieve the entity whose equipment will be changed.
     *
     * @param event - the packet event.
     * @return The affected entity.
     */
    public Entity getEntity(PacketEvent event) {
        return getEntity(event.getPlayer().getWorld());
    }

    /**
     * Retrieve list of ItemSlot - ItemStack pairs.
     *
     * @return The current list of ItemSlot - ItemStack pairs.
     */
    public List<Pair<ItemSlot, ItemStack>> getSlotStackPairs() {
        return handle.getSlotStackPairLists().read(0);
    }

    /**
     * Set a ItemSlot - ItemStack pair.
     * @param slot The slot the item will be equipped in. If matches an existing pair, will overwrite the old one
     * @param item The item to equip
     *
     * @return Whether a pair was overwritten.
     */
    public boolean setSlotStackPair(ItemSlot slot, ItemStack item) {
        List<Pair<ItemSlot, ItemStack>> slotStackPairs = handle.getSlotStackPairLists().read(0);
        boolean removed = slotStackPairs.removeIf(pair -> pair.getFirst().equals(slot));
        slotStackPairs.add(new Pair<>(slot, item));
        handle.getSlotStackPairLists().write(0, slotStackPairs);
        return removed;
    }

    /**
     * Removes the ItemSlot ItemStack pair matching the provided slot. If doesn't exist does nothing
     * @param slot the slot to remove the pair from
     *
     * @return Whether a pair was removed.
     */
    public boolean removeSlotStackPair(ItemSlot slot) {
        List<Pair<ItemSlot, ItemStack>> slotStackPairs = handle.getSlotStackPairLists().read(0);
        boolean removed = slotStackPairs.removeIf(pair -> pair.getFirst().equals(slot));
        handle.getSlotStackPairLists().write(0, slotStackPairs);
        return removed;
    }

    /**
     * Check whether the provided is to be affected
     * @param slot the slot to check for
     * @return true if is set, false otherwise
     */
    public boolean isSlotSet(ItemSlot slot) {
        return handle.getSlotStackPairLists().read(0).stream().anyMatch(pair -> pair.getFirst().equals(slot));
    }

    /**
     * Get the item being equipped to the provided slot
     * @param slot the slot to retrieve the item from
     * @return the equipping item, or null if doesn't exist
     */
    public ItemStack getItem(ItemSlot slot) {
        for (Pair<ItemSlot, ItemStack> pair : handle.getSlotStackPairLists().read(0)) {
            if (pair.getFirst().equals(slot)) { return pair.getSecond(); }
        }
        return null;
    }

    /**
     * @deprecated This format is no longer supported in Minecraft 1.16+
     * For 1.16+ use the SlotStack methods
     */
    @Deprecated
    public ItemSlot getSlot() {
        return handle.getItemSlots().read(0);
    }

    /**
     * @deprecated This format is no longer supported in Minecraft 1.16+
     * For 1.16+ use the SlotStack methods
     */
    @Deprecated
    public void setSlot(ItemSlot value) {
        handle.getItemSlots().write(0, value);
    }

    /**
     * @deprecated This format is no longer supported in Minecraft 1.16+
     * For 1.16+ use the SlotStack methods
     *
     * Retrieve Item.
     * <p>
     * Notes: item in slot format
     *
     * @return The current Item
     */
    @Deprecated
    public ItemStack getItem() {
        return handle.getItemModifier().read(0);
    }

    /**
     * @deprecated This format is no longer supported in Minecraft 1.16+
     * For 1.16+ use the SlotStack methods
     *
     * Set Item.
     *
     * @param value - new value.
     */
    @Deprecated
    public void setItem(ItemStack value) {
        handle.getItemModifier().write(0, value);
    }
}