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

import com.psygate.collections.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by psygate on 26.04.2016.
 */
public class ItemMap {
    private final HashMap<Material, Integer> values = new HashMap<>();
    private final HashMap<Material, Integer> limits = new HashMap<>();

    public ItemMap(Map<Material, Integer> limits) {
        this.limits.putAll(limits);
    }

    public Optional<ItemStack> add(ItemStack stack) {
        if (!limits.containsKey(stack.getType())) {
            return Optional.of(stack);
        } else {
            int amount = values.getOrDefault(stack.getType(), 0);
            int total = amount + stack.getAmount();
            if (total > limits.get(stack.getType())) {
                values.put(stack.getType(), limits.get(stack.getType()));

                return Optional.of(new ItemStack(stack.getType(), total - limits.get(stack.getType())));
            } else if (total == limits.get(stack.getType())) {
                values.put(stack.getType(), limits.get(stack.getType()));
                return Optional.empty();
            } else {
                values.put(stack.getType(), total);
                return Optional.empty();
            }
        }
    }

    public Collection<ItemStack> addAll(Collection<ItemStack> stacks) {
        LinkedList<ItemStack> drop = new LinkedList<>();

        for (ItemStack stack : stacks) {
            add(stack).ifPresent(drop::add);
        }

        return drop;
    }

    public void clear(Map<Material, Integer> newlimits) {
        values.clear();
        limits.clear();
        limits.putAll(newlimits);
    }

    public boolean isSatisfied() {
        for (Map.Entry<Material, Integer> en : limits.entrySet()) {
            if (values.getOrDefault(en.getKey(), 0) < en.getValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a list of have & require items.
     *
     * @return List of have and require items. Key of the pair is the have, value of the pair is the require.
     */
    public List<Pair<ItemStack, ItemStack>> getItems() {
        ArrayList<Pair<ItemStack, ItemStack>> list = new ArrayList<>(limits.size());
        list.addAll(limits.entrySet().stream().map(en -> new Pair<>(
                new ItemStack(en.getKey(), values.getOrDefault(en.getKey(), 0)),
                new ItemStack(en.getKey(), en.getValue())
        )).collect(Collectors.toList()));

        return list;
    }

    public HashMap<Material, Integer> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "Values: " + values + " Required: " + limits + " Satisfied: " + isSatisfied();
    }
}
