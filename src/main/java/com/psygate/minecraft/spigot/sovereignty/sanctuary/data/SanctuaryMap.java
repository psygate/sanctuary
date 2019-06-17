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
import com.psygate.minecraft.spigot.sovereignty.nucleus.util.mc.BlockKey;
import com.psygate.spatial.primitives.IntAABB2D;
import com.psygate.spatial.trees.AdaptiveIntAreaQuadTree;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;

/**
 * Created by psygate (https://github.com/psygate) on 29.03.2016.
 */
class SanctuaryMap {
    private final static AdaptiveIntAreaQuadTree<LSanctuaryField> EMPTY_TREE = new AdaptiveIntAreaQuadTree<>(IntAABB2D.fromCenter(0, 0, 1), 8);
    private final Map<UUID, AdaptiveIntAreaQuadTree<LSanctuaryField>> sanctuaryTrees = new HashMap<>();
    private final Map<BlockKey, LSanctuaryField> exactSanctuaries = new HashMap<>();

    SanctuaryMap() {

    }

    public Optional<SanctuaryField> get(BlockKey key) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }

        if (exactSanctuaries.containsKey(key)) {
            return Optional.of(exactSanctuaries.get(key));
        } else {
            return Optional.empty();
        }
    }

    public List<LSanctuaryField> getAll() {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        return new LinkedList<>(exactSanctuaries.values());
    }

    public Collection<LSanctuaryField> getValuesContaining(int x, int z, UUID world) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        if (sanctuaryTrees.containsKey(world)) {
            return sanctuaryTrees.get(world).getValuesContaining(x, z);
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<LSanctuaryField> getValuesIntersecting(IntAABB2D bounds, UUID world) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        if (sanctuaryTrees.containsKey(world)) {
            Collection<LSanctuaryField> vals = sanctuaryTrees.get(world).getValuesIntersecting(bounds);
//            assert sanctuaryTrees.get(world).getValues().stream().filter(s -> s.getBounds().intersects(bounds)).findAny().isPresent() == !vals.isEmpty();
            return vals;
        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasValuesContaining(int x, int z, UUID world) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        if (sanctuaryTrees.containsKey(world)) {
            return sanctuaryTrees.get(world).hasValuesContaining(x, z);
        } else {
            return false;
        }
    }

    public boolean hasValuesIntersecting(IntAABB2D bounds, UUID world) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        if (sanctuaryTrees.containsKey(world)) {
//            Collection<LSanctuaryField> vals = sanctuaryTrees.get(world).getValuesIntersecting(bounds);
//            assert sanctuaryTrees.get(world).getValues().stream().filter(s -> s.getBounds().intersects(bounds)).findAny().isPresent() == !vals.isEmpty();
            return sanctuaryTrees.get(world).hasValuesIntersecting(bounds);
        } else {
            return false;
        }
    }

    public List<LSanctuaryField> purgeNonIntersecting(List<Pair<UUID, IntAABB2D>> bounds) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        List<LSanctuaryField> all = getAll();
        Set<LSanctuaryField> intersect = new HashSet<>();

        for (Pair<UUID, IntAABB2D> bound : bounds) {
            intersect.addAll(getValuesIntersecting(bound.getValue(), bound.getKey()));
        }

        Iterator<LSanctuaryField> it = all.iterator();

        while (it.hasNext()) {
            if (intersect.contains(it.next())) {
                it.remove();
            }
        }

        for (LSanctuaryField field : all) {
            sanctuaryTrees.get(field.getLocation().getUuid()).remove(field);
            exactSanctuaries.remove(field.getLocation());
        }

        return all;
    }

    public void add(LSanctuaryField field) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        mapAdd(field);
        treeAdd(field);

//        sanctuaryTrees.putIfAbsent(field.getLocation().getUuid(), new AdaptiveIntAreaQuadTree<>(field.getBounds(), 8));
//        int size = sanctuaryTrees.get(field.getLocation().getUuid()).size();
//        sanctuaryTrees.get(field.getLocation().getUuid()).add(field);
//        assert size + 1 == sanctuaryTrees.get(field.getLocation().getUuid()).size()
//                : "Unexpected Size:" + (size + 1) + "/" + sanctuaryTrees.get(field.getLocation().getUuid()).size();
//        exactSanctuaries.put(field.getLocation(), field);
//        assert exactSanctuaries.size() == sanctuaryTrees.values().stream().mapToInt(v -> v.size()).sum()
//                : "Size discrepancy: " + exactSanctuaries.size() + "/" + sanctuaryTrees.values().stream().mapToInt(v -> v.size()).sum()
//                + "\n"
//                + exactSanctuaries + "\n" + sanctuaryTrees;
    }

    private void treeAdd(LSanctuaryField field) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        UUID world = field.getLocation().getUuid();
        createTreeIfMissing(world, IntAABB2D.fromCenter(field.getLocation().getX(), field.getLocation().getZ(), 100));
        int size = sanctuaryTrees.get(world).size();
        sanctuaryTrees.get(world).add(field);
//        assert sanctuaryTrees.get(world).contains(field) : "Sanctuary is not in map.";
//        assert size + 1 == sanctuaryTrees.get(world).size() : "Expected: " + (size + 1) + " Actual: " + sanctuaryTrees.get(world).size();
    }

    private void createTreeIfMissing(UUID uuid, IntAABB2D initialBounds) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        if (!sanctuaryTrees.containsKey(uuid)) {
            sanctuaryTrees.put(uuid, new AdaptiveIntAreaQuadTree<>(initialBounds, 8));
        }
    }

    private boolean mapAdd(LSanctuaryField field) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        if (!exactSanctuaries.containsKey(field.getLocation())) {
            exactSanctuaries.put(field.getLocation(), field);
            return true;
        } else {
            return false;
        }
    }

    public void remove(LSanctuaryField field) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        if (exactSanctuaries.containsKey(field.getLocation())) {
            exactSanctuaries.remove(field.getLocation());
            sanctuaryTrees.get(field.getLocation().getUuid()).remove(field);
//            boolean assertions = false;
//            assert assertions = true;


//            if (assertions) {
//                System.out.println(field + " removed.");
//            }

//            assert !sanctuaryTrees.values().stream().anyMatch(v -> v.contains(field));
//            assert !sanctuaryTrees.get(field.getLocation().getUuid()).contains(field) : "Field (" + field + ") removed but still in tree.";
//            assert !exactSanctuaries.containsKey(field.getLocation()) : "Field (" + field + ") removed but still in map.";
//            assert exactSanctuaries.size() == sanctuaryTrees.values().stream().mapToInt(AdaptiveIntAreaQuadTree::size).sum()
//                    : "Size discrepancy: Sanctuary Map Size: " + exactSanctuaries.size()
//                    + " / Trees size:" + sanctuaryTrees.values().stream().mapToInt(AdaptiveIntAreaQuadTree::size).sum();
        } else {
            throw new IllegalStateException("Sanctuary field passed that doesn't show up in the sanctuary map. (" + field + ")");
        }
    }

    public void addAll(List<LSanctuaryField> sancts) {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        for (LSanctuaryField field : sancts) {
            add(field);
        }
    }

    public Collection<LSanctuaryField> getMap() {
        if (!bukkitThread()) {
            System.err.println("Thread access is not bukkit thread, this may lead to issues.");
        }
        return exactSanctuaries.values();
    }

    private boolean bukkitThread() {
        return Bukkit.isPrimaryThread();
    }

    public boolean hasWorld(World world) {
        return sanctuaryTrees.containsKey(world.getUID());
    }

    public void clear() {
        sanctuaryTrees.clear();
        exactSanctuaries.clear();
    }
}
