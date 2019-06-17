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

import com.codahale.metrics.Timer;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.psygate.collections.Pair;
import com.psygate.minecraft.spigot.sovereignty.amethyst.reinforcement.ReinforcementManager;
import com.psygate.minecraft.spigot.sovereignty.nucleus.Nucleus;
import com.psygate.minecraft.spigot.sovereignty.nucleus.util.mc.BlockKey;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryReinforcedRecord;
import com.psygate.spatial.primitives.IntAABB2D;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by psygate (https://github.com/psygate) on 19.03.2016.
 */
public class SanctuaryManager {
    private static SanctuaryManager instance;
    private SanctuaryMap sanctuaries = new SanctuaryMap();
    private SanctuaryIO iohandler = new SanctuaryIO(this);

    private final LoadingCache<Pair<BlockKey, Long>, Optional<SanctuaryReinforcedRecord>> reinforcementCache =
            Sanctuary.getConf().getCacheSettings().getCacheBuilder()
                    .removalListener(new RemovalListener<Pair<BlockKey, Long>, Optional<SanctuaryReinforcedRecord>>() {
                        @Override
                        public void onRemoval(RemovalNotification<Pair<BlockKey, Long>, Optional<SanctuaryReinforcedRecord>> removalNotification) {
                            removalNotification.getValue().ifPresent(iohandler::writeReinforcementRecords);
                        }
                    })
                    .build(new CacheLoader<Pair<BlockKey, Long>, Optional<SanctuaryReinforcedRecord>>() {
                        @Override
                        public Optional<SanctuaryReinforcedRecord> load(Pair<BlockKey, Long> blockKey) throws Exception {
                            return iohandler.getReinforced(blockKey.getKey(), blockKey.getValue());
                        }
                    });

    private SanctuaryManager() {

    }

    public static SanctuaryManager getInstance() {
        if (instance == null) {
            instance = new SanctuaryManager();
        }

        return instance;
    }

    boolean reinforce(BlockKey key, LSanctuaryField lSanctuaryField) {

        // If the field is new, store the field now.
        if (lSanctuaryField.getState() == SanctuaryState.NEW) {
            iohandler.syncWrite(lSanctuaryField);
            lSanctuaryField.setState(SanctuaryState.CLEAN);
        }

        SanctuaryReinforcedRecord rec = new SanctuaryReinforcedRecord(
                lSanctuaryField.getId(),
                key.getX(),
                key.getY(),
                key.getZ(),
                key.getUuid()
        );

        try {
            reinforcementCache.put(new Pair<>(key, lSanctuaryField.getId()), Optional.of(rec));
            iohandler.writeReinforcementRecords(rec);

            long now = System.currentTimeMillis();

            return ReinforcementManager.getInstance().createReinforcement(
                    Collections.emptyList(),
                    new HashSet<>(Arrays.asList(key)),
                    lSanctuaryField.getGroupID(),
                    false,
                    false,
                    Sanctuary.getConf().getInteractions().getStrengthReinforcementOnBreak(),
                    Sanctuary.getConf().getInteractions().getStrengthReinforcementOnBreak(),
                    now,
                    now,
                    lSanctuaryField.getCreator()
            ) != null;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    boolean reinforce(Set<BlockKey> keys, LSanctuaryField lSanctuaryField) {
        // If the field is new, store the field now.
        if (lSanctuaryField.getState() == SanctuaryState.NEW) {
            iohandler.syncWrite(lSanctuaryField);
            lSanctuaryField.setState(SanctuaryState.CLEAN);
        }

        List<SanctuaryReinforcedRecord> recs = keys.stream()
                .map(key ->
                        new SanctuaryReinforcedRecord(
                                lSanctuaryField.getId(),
                                key.getX(),
                                key.getY(),
                                key.getZ(),
                                key.getUuid()
                        )
                ).collect(Collectors.toList());

        try {
            Map<Pair<BlockKey, Long>, Optional<SanctuaryReinforcedRecord>> map = new HashMap<>();
            for (SanctuaryReinforcedRecord rec : recs) {
                map.put(
                        new Pair<>(new BlockKey(rec.getX(), rec.getY(), rec.getZ(), rec.getWorldUuid()), rec.getSanctuaryId()),
                        Optional.of(rec)
                );
            }
            reinforcementCache.putAll(map);
            iohandler.writeReinforcementRecords(recs);

            return ReinforcementManager.getInstance().createReinforcement(
                    Collections.emptyList(),
                    keys,
                    lSanctuaryField.getGroupID(),
                    false,
                    false,
                    Sanctuary.getConf().getInteractions().getStrengthReinforcementOnBreak(),
                    Sanctuary.getConf().getInteractions().getStrengthReinforcementOnBreak(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    lSanctuaryField.getCreator()
            ) != null;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void flush() {
        iohandler.forceSynced();
        iohandler.write(sanctuaries.getAll());
        reinforcementCache.invalidateAll();
        iohandler.forceAsync();
    }

//    public void loadInBounds(Collection<Pair<UUID, IntAABB2D>> bounds) {
//        Timer.Context time = Nucleus.getMetricRegistry().timer("sanctuary-manager-loadinbounds").time();
//        List<LSanctuaryField> sanctuaries = iohandler.loadInBounds(bounds);
//        time.stop();
//
//        Bukkit.getScheduler().runTask(Sanctuary.getInstance(), () -> {
//            Iterator<LSanctuaryField> it = sanctuaries.iterator();
//            while (it.hasNext()) {
//                LSanctuaryField field = it.next();
//                if (field.getLocation().getLocation().getBlock().getType() != Sanctuary.getConf().getSanctuaries().getSanctuaryType()) {
//                    System.err.println("Dead sanctuary found, block type mismatch. Culling. " + field);
//                    it.remove();
//                    iohandler.delete(field);
//                }
//            }
//            this.sanctuaries.addAll(sanctuaries);
//        });
//    }

    public void load(World world) {
        if (!sanctuaries.hasWorld(world)) {
            List<LSanctuaryField> sanctuaries = iohandler.load(world.getUID());
//            Bukkit.getScheduler().runTask(Sanctuary.getInstance(), () -> {
            Iterator<LSanctuaryField> it = sanctuaries.iterator();
            while (it.hasNext()) {
                LSanctuaryField field = it.next();
                if (field.getLocation().getLocation().getBlock().getType() != Sanctuary.getConf().getSanctuaries().getSanctuaryType()) {
                    System.err.println("Dead sanctuary found, block type mismatch. Culling. " + field);
                    it.remove();
                    iohandler.delete(field);
                }
            }
            this.sanctuaries.addAll(sanctuaries);
            System.out.println("Loaded " + sanctuaries.size() + " for " + world.getName());
//            });
        }
    }


    public void cleanUp() {
        Timer.Context timer = Nucleus.getMetricRegistry().timer("sanctuarymanager-cleanup").time();

        List<Pair<UUID, IntAABB2D>> bounds = Sanctuary.getInstance().getServer().getWorlds().stream()
                .map(World::getLoadedChunks)
                .flatMap(Arrays::stream)
                .map(c -> new Pair<>(c.getWorld().getUID(), new IntAABB2D(c.getX() * 16, c.getZ() * 16, c.getX() * 16 + 16, c.getZ() * 16 + 16)))
                .collect(Collectors.toList());

        List<LSanctuaryField> purged = sanctuaries.purgeNonIntersecting(bounds);

        iohandler.write(purged);
        timer.stop();
    }

    void deleteSanctuary(LSanctuaryField sanctuary) {
        sanctuaries.remove(sanctuary);
        iohandler.delete(sanctuary);
    }

    SanctuaryMap getMap() {
        return sanctuaries;
    }

    public Optional<SanctuaryField> createSanctuary(UUID creator, BlockKey position, Long groupID) {
        int radius = (int) (Sanctuary.getConf().getSanctuaries().getInitialHealth() / Sanctuary.getConf().getSanctuaries().getHealthPerRadiusUnit());
        if (hasIntersecting(IntAABB2D.fromCenter(position.getX(), position.getZ(), radius), position.getUuid())) {
            return Optional.empty();
        } else {
            long health = Sanctuary.getConf().getSanctuaries().getInitialHealth();
            LSanctuaryField field = new LSanctuaryField(System.currentTimeMillis(), creator, groupID, position, health, health, "Sanctuary", this);
            iohandler.syncWrite(field);
            field.setState(SanctuaryState.CLEAN);
            sanctuaries.add(field);
            return Optional.of(field);
        }
    }

    public Optional<SanctuaryField> getSanctuary(BlockKey key) {
        return sanctuaries.get(key);
    }

    public Collection<? extends SanctuaryField> getContaining(int x, int z, UUID world) {
        Collection<LSanctuaryField> fields = sanctuaries.getValuesContaining(x, z, world);
        return fields;
    }

    boolean hasIntersecting(IntAABB2D box, UUID world) {
        return !sanctuaries.getValuesIntersecting(box, world).isEmpty();
    }

    boolean canReinforce(BlockKey key, LSanctuaryField lSanctuaryField) {
        try {
            return !ReinforcementManager.getInstance().isReinforced(key) && !reinforcementCache.getUnchecked(new Pair<>(key, lSanctuaryField.getId())).isPresent();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    SanctuaryIO getIOHandler() {
        return iohandler;
    }

    public boolean hasContaining(int x, int y, UUID world) {
        return sanctuaries.hasValuesContaining(x, y, world);
    }

    public Collection<? extends SanctuaryField> getSanctuaryMap() {
        return sanctuaries.getMap();
    }

    public void healAll() {
        long ticktime = System.currentTimeMillis();
        for (LSanctuaryField sanct : sanctuaries.getAll()) {
            if (sanct.getHealingTimeout() <= ticktime) {
                sanct.setHealth(sanct.getHealth() + Sanctuary.getConf().getRegeneration().getHeal());
            }
        }
    }

    public void loadInitially() {
        System.out.println("Loading initially: " + Bukkit.getWorlds().stream().map(World::getName).reduce("", (a, b) -> a + ", " + b));
        Bukkit.getWorlds().stream().forEach(w -> this.load(w));
    }

    public void reload() {
        flush();
        sanctuaries.clear();
        loadInitially();
    }
}