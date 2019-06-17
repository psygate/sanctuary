/*
 *     Copyright (C) 2016 psygate (https://github.com/psygate)
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 */

package com.psygate.minecraft.spigot.sovereignty.sanctuary.data;

import com.psygate.minecraft.spigot.sovereignty.nucleus.util.mc.BlockKey;
import com.psygate.spatial.primitives.IntBoundable2D;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Set;
import java.util.UUID;

/**
 * Created by psygate (https://github.com/psygate) on 27.03.2016.
 */
public interface SanctuaryField extends IntBoundable2D, InventoryHolder {
    long getCreationTime();

    void setCreationTime(long creationTime);

    UUID getCreator();

    void setCreator(UUID creator);

    Long getGroupID();

    void setGroupID(Long groupID);

    BlockKey getLocation();

    void setLocation(BlockKey location);

    long getHealth();

    void setHealth(long health);

    long getMaxHealth();

    void setMaxHealth(long maxHealth);

    void delete();

    void setName(String name);

    String getName();

    Inventory getInventory();

    void dismissInventory();

    void damage(long damage, DamageType type, UUID source);

    void damage(long damage);

    boolean canReinforce(BlockKey key);

    boolean reinforce(BlockKey key);

    boolean canReinforce(Set<BlockKey> blocks);

    void reinforce(Set<BlockKey> blocks);

    boolean isFriendly(UUID player);

    long getRadius();

    void addChangeListener(SanctuaryModificationListener listener);

    void removeChangeListener(SanctuaryModificationListener listener);

    long getHealingTimeout();
}
