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

package com.psygate.minecraft.spigot.sovereignty.sanctuary.configuration;

import com.psygate.collections.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by psygate (https://github.com/psygate) on 27.03.2016.
 */
public class Growth {
    private List<Pair<Material, Double>> costPerBlock;

    public Growth(ConfigurationSection growth) {
        costPerBlock = growth.getMapList("cost_per_block")
                .stream()
                .map(m -> new Pair<>(asMaterial((String) m.get("type")), (Double) m.get("amount")))
                .collect(Collectors.toList());
    }

    private Material asMaterial(String type) {
        return Material.valueOf(type.trim().toUpperCase());
    }

    public List<Pair<Material, Double>> getCostPerBlock() {
        return Collections.unmodifiableList(costPerBlock);
    }

    public boolean isUpgradeMaterial(Material type) {
        return costPerBlock.stream().map(Pair::getKey).anyMatch(t -> t == type);
    }

    public boolean isUpgradeMaterial(ItemStack stack) {
        if (stack == null) {
            return false;
        } else if (stack.hasItemMeta()) {
            return false;
        } else if (stack.getData().getData() != 0) {
            return false;
        } else if (!isUpgradeMaterial(stack.getType())) {
            return false;
        } else {
            return true;
        }
    }
}
