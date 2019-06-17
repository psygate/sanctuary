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

package com.psygate.minecraft.spigot.sovereignty.sanctuary.listeners;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.psygate.collections.Pair;
import com.psygate.minecraft.spigot.sovereignty.amethyst.Amethyst;
import com.psygate.minecraft.spigot.sovereignty.amethyst.events.PlayerCreateReinforcementEvent;
import com.psygate.minecraft.spigot.sovereignty.amethyst.reinforcement.Reinforcement;
import com.psygate.minecraft.spigot.sovereignty.amethyst.reinforcement.ReinforcementManager;
import com.psygate.minecraft.spigot.sovereignty.amethyst.reinforcement.proxy.ProxyManager;
import com.psygate.minecraft.spigot.sovereignty.ivory.groups.Rank;
import com.psygate.minecraft.spigot.sovereignty.nucleus.util.mc.BlockKey;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.DamageType;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryField;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.Tables;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryPlacementTimesRecord;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.events.*;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.psygate.minecraft.spigot.sovereignty.sanctuary.util.GroupHelper.hasMember;

/**
 * Created by psygate (https://github.com/psygate) on 27.03.2016.
 */
public class SanctuaryListener implements Listener {
    private final static Logger LOG = Sanctuary.getLogger(SanctuaryListener.class.getName());
    private LoadingCache<UUID, Pair<Boolean, Long>> placementCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .initialCapacity(100)
            .maximumSize(1000)
            .removalListener((RemovalNotification<UUID, Pair<Boolean, Long>> removalNotification) -> {
                if (!removalNotification.getValue().getKey()) {
                    Sanctuary.DBI().asyncSubmit((conf) -> {
                        Timestamp timestamp = new Timestamp(removalNotification.getValue().getValue());
//                        Calendar cal = Calendar.getInstance();
//                        cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
//                        if (timestamp.before(new Timestamp(cal.getTimeInMillis()))) {
//                            System.err.println("Strange sanctuary cd date: " + timestamp);
//                        }
                        DSL.using(conf).insertInto(Tables.SANCTUARY_PLACEMENT_TIMES)
                                .set(new SanctuaryPlacementTimesRecord(removalNotification.getKey(), timestamp))
                                .execute();
                    });
                }
            })
            .build(new CacheLoader<UUID, Pair<Boolean, Long>>() {
                @Override
                public Pair<Boolean, Long> load(UUID uuid) throws Exception {
                    return Sanctuary.DBI().submit((conf) -> {
                        return DSL.using(conf).selectFrom(Tables.SANCTUARY_PLACEMENT_TIMES)
                                .where(Tables.SANCTUARY_PLACEMENT_TIMES.CREATOR.eq(uuid))
                                .orderBy(Tables.SANCTUARY_PLACEMENT_TIMES.CREATION.desc())
                                .limit(1)
                                .fetchOptional()
                                .map(rec -> new Pair<>(true, rec.getCreation().getTime()))
                                .orElseGet(() -> new Pair<>(false, System.currentTimeMillis() - Sanctuary.getConf().getSanctuaries().getPlacementCooldown()));
                    });
                }
            });

    public void flush() {
        placementCache.invalidateAll();
    }

    public void uncooldown(UUID uuid) {
        placementCache.invalidate(uuid);
        Sanctuary.DBI().asyncSubmit((conf) -> {
            DSL.using(conf).deleteFrom(Tables.SANCTUARY_PLACEMENT_TIMES)
                    .where(Tables.SANCTUARY_PLACEMENT_TIMES.CREATOR.eq(uuid))
                    .execute();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReinforcementEvent(PlayerCreateReinforcementEvent ev) {
        if (ev.getTarget().getType() == Sanctuary.getConf().getSanctuaries().getSanctuaryType()) {
            long lastPlacement = placementCache.getUnchecked(ev.getPlayer().getUniqueId()).getValue();
            long placementCoolDown = Sanctuary.getConf().getSanctuaries().getPlacementCooldown();

            if (!ev.getPlayer().isOp() &&
                    System.currentTimeMillis() - lastPlacement < placementCoolDown) {
                ev.getPlayer().sendMessage(
                        new String[]{
                                ChatColor.RED + "Sanctuary placement is on cooldown. You cannot create a sanctuary right now.",
                                "Cooldown: " + TimeUnit.MILLISECONDS.toMinutes((lastPlacement + placementCoolDown) - System.currentTimeMillis()) + " Minutes."
                        }
                );
                if (ev.getPlayer().isOp()) {
                    ev.getPlayer().sendMessage("Last placement: " + new Date(lastPlacement));
                    ev.getPlayer().sendMessage("Cooldown Time: " + TimeUnit.MILLISECONDS.toMinutes(placementCoolDown) + " Minutes.");
                }
            } else if (Sanctuary.getConf().getSanctuaries().isEnabledWorld(ev.getTarget().getWorld())) {
                Optional<SanctuaryField> field = SanctuaryManager.getInstance().createSanctuary(ev.getPlayer().getUniqueId(), new BlockKey(ev.getTarget()), ev.getGroupID());
                if (field.isPresent()) {
                    placementCache.put(ev.getPlayer().getUniqueId(), new Pair<>(false, System.currentTimeMillis()));
                    ev.getPlayer().sendMessage(ChatColor.GREEN + "Sanctuary created. " + ChatColor.GRAY + "Sanctuary placement now on cooldown.");
                } else {
                    ev.getPlayer().sendMessage(ChatColor.RED + "Sanctuary cannot be created here.");
                }
            } else {
                ev.getPlayer().sendMessage(ChatColor.RED + "Sanctuary cannot be created here. World doesn't support sanctuaries.");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ev) {
        UUID tow = ev.getPlayer().getLocation().getWorld().getUID();
        int tox = ev.getPlayer().getLocation().getBlockX(), toz = ev.getPlayer().getLocation().getBlockZ();

        if (SanctuaryManager.getInstance().hasContaining(tox, toz, tow)) {

            Collection<? extends SanctuaryField> tofields = SanctuaryManager.getInstance().getContaining(tox, toz, tow);

            for (SanctuaryField field : tofields) {
                new SanctuaryEnterEvent(ev.getPlayer(), field).call();
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent ev) {
        UUID tow = ev.getPlayer().getLocation().getWorld().getUID();
        int tox = ev.getPlayer().getLocation().getBlockX(), toz = ev.getPlayer().getLocation().getBlockZ();

        if (SanctuaryManager.getInstance().hasContaining(tox, toz, tow)) {

            Collection<? extends SanctuaryField> tofields = SanctuaryManager.getInstance().getContaining(tox, toz, tow);

            for (SanctuaryField field : tofields) {
                new SanctuaryExitEvent(ev.getPlayer(), field).call();
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerTeleportEvent ev) throws ExecutionException, InterruptedException {
        int tox = ev.getTo().getBlockX(), toz = ev.getTo().getBlockZ(),
                fromx = ev.getFrom().getBlockX(), fromz = ev.getFrom().getBlockZ();
        UUID tow = ev.getTo().getWorld().getUID(), fromw = ev.getFrom().getWorld().getUID();

        if (SanctuaryManager.getInstance().hasContaining(tox, toz, tow)) {

            Collection<? extends SanctuaryField> tofields = SanctuaryManager.getInstance().getContaining(tox, toz, tow);

            for (SanctuaryField field : tofields) {
                boolean entered = !field.getBounds().contains(fromx, fromz) || !field.getLocation().getUuid().equals(fromw);

                if (entered) {
                    new SanctuaryEnterEvent(ev.getPlayer(), field).call();
                    return;
                }
            }
        }

        if (SanctuaryManager.getInstance().hasContaining(fromx, fromz, fromw)) {
            Collection<? extends SanctuaryField> fromfields = SanctuaryManager.getInstance().getContaining(fromx, fromz, fromw);

            for (SanctuaryField field : fromfields) {
                boolean exited = !field.getBounds().contains(tox, toz) || !field.getLocation().getUuid().equals(tow);

                if (exited) {
                    new SanctuaryExitEvent(ev.getPlayer(), field).call();
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent ev) throws ExecutionException, InterruptedException {
        int tox = ev.getTo().getBlockX(), toz = ev.getTo().getBlockZ(),
                fromx = ev.getFrom().getBlockX(), fromz = ev.getFrom().getBlockZ();
        UUID tow = ev.getTo().getWorld().getUID(), fromw = ev.getFrom().getWorld().getUID();

        if (SanctuaryManager.getInstance().hasContaining(tox, toz, tow)) {

            Collection<? extends SanctuaryField> tofields = SanctuaryManager.getInstance().getContaining(tox, toz, tow);

            for (SanctuaryField field : tofields) {
                boolean entered = !field.getBounds().contains(fromx, fromz) || !field.getLocation().getUuid().equals(fromw);

                if (entered) {
                    new SanctuaryEnterEvent(ev.getPlayer(), field).call();
                    return;
                }
            }
        }

        if (SanctuaryManager.getInstance().hasContaining(fromx, fromz, fromw)) {
            Collection<? extends SanctuaryField> fromfields = SanctuaryManager.getInstance().getContaining(fromx, fromz, fromw);

            for (SanctuaryField field : fromfields) {
                boolean exited = !field.getBounds().contains(tox, toz) || !field.getLocation().getUuid().equals(tow);

                if (exited) {
                    new SanctuaryExitEvent(ev.getPlayer(), field).call();
                    return;
                }
            }
        }
    }

//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void chunkLoadEvent(ChunkLoadEvent ev) {
//        if (Sanctuary.getConf().getSanctuaries().isEnabledWorld(ev.getWorld())) {
//            Chunk c = ev.getChunk();
//            SanctuaryManager.getInstance().loadInBounds(Arrays.asList(new Pair<>(c.getWorld().getUID(), new IntAABB2D(c.getX() * 16, c.getZ() * 16, c.getX() * 16 + 16, c.getZ() * 16 + 16))));
//        }
//    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void chunkLoadEvent(WorldLoadEvent ev) {
        if (Sanctuary.getConf().getSanctuaries().isEnabledWorld(ev.getWorld())) {
            SanctuaryManager.getInstance().load(ev.getWorld());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBlockBreak(BlockBreakEvent ev) {
        Optional<SanctuaryField> fieldopt = SanctuaryManager.getInstance().getSanctuary(new BlockKey(ev.getBlock()));
        fieldopt.ifPresent(field -> {
            ev.getPlayer().sendMessage(ChatColor.RED + "Sanctuary destroyed.");
            field.delete();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.getAction() == Action.RIGHT_CLICK_BLOCK && !ev.getPlayer().isSneaking() && ev.getClickedBlock().getType() == Sanctuary.getConf().getSanctuaries().getSanctuaryType()) {
            Optional<SanctuaryField> fieldopt = SanctuaryManager.getInstance().getSanctuary(new BlockKey(ev.getClickedBlock()));
            fieldopt.ifPresent(field -> {
                ev.getPlayer().openInventory(field.getInventory());
                ev.setCancelled(true);
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent ev) {
        Block block = ev.getBlockClicked().getRelative(ev.getBlockFace());
        if (SanctuaryManager.getInstance().hasContaining(block.getX(), block.getZ(), block.getWorld().getUID())) {
            Collection<? extends SanctuaryField> sancts = SanctuaryManager.getInstance().getContaining(block.getX(), block.getZ(), block.getWorld().getUID());
//            System.out.println("Sanctuary containing bucket empty event. " + sancts);
            for (SanctuaryField sanct : sancts) {
                if (!hasMember(ev.getPlayer().getUniqueId(), sanct.getGroupID())) {
                    ev.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketEmpty(BlockDispenseEvent ev) throws ExecutionException {
        BlockState state = ev.getBlock().getState();
        if (state instanceof Dispenser) {
            Dispenser disp = (Dispenser) state;
            org.bukkit.material.Dispenser data = (org.bukkit.material.Dispenser) disp.getData();
            BlockKey to = new BlockKey(disp.getBlock().getRelative(data.getFacing()));
            BlockKey block = new BlockKey(disp.getBlock());

            if (SanctuaryManager.getInstance().hasContaining(to.getX(), to.getZ(), to.getUuid())) {
                Collection<? extends SanctuaryField> sancts = SanctuaryManager.getInstance().getContaining(to.getX(), to.getZ(), to.getUuid());

                if (!ReinforcementManager.getInstance().isReinforced(block)) {
                    ev.setCancelled(true);
                } else {
                    Reinforcement reinf = ReinforcementManager.getInstance().getReinforcement(block);

                    for (SanctuaryField sanct : sancts) {
                        if (!sanct.getGroupID().equals(reinf.getGroupID())) {
                            ev.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void redstoneEvent(BlockRedstoneEvent ev) {
        Block block = ev.getBlock();
        if (SanctuaryManager.getInstance().hasContaining(block.getX(), block.getZ(), block.getWorld().getUID())) {
            Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(block.getX(), block.getZ(), block.getWorld().getUID());
            BlockKey key = new BlockKey(block);
            Set<BlockKey> blocks = new HashSet<>(ProxyManager.getInstance().map(block).stream().map(BlockKey::new).collect(Collectors.toList()));

            SanctuaryField field = fields.iterator().next();
            if (field.canReinforce(blocks) && Amethyst.getInstance().getConf().isReinforcible(block.getType())) {
                field.reinforce(blocks);
                ev.setNewCurrent(ev.getOldCurrent());
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerInteractEvent(PlayerInteractEvent ev) throws ExecutionException {
        if ((ev.getAction() == Action.LEFT_CLICK_BLOCK || ev.getAction() == Action.RIGHT_CLICK_BLOCK
                && Amethyst.getInstance().getConf().getSettings().isContainer(ev.getClickedBlock().getType()))) {
            if (SanctuaryManager.getInstance().hasContaining(ev.getClickedBlock().getX(), ev.getClickedBlock().getZ(), ev.getClickedBlock().getWorld().getUID())) {
                if (Amethyst.getInstance().getConf().isReinforcible(ev.getClickedBlock().getType())
                        && !ReinforcementManager.getInstance().isReinforced(new BlockKey(ev.getClickedBlock()))) {
                    Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(ev.getClickedBlock().getX(), ev.getClickedBlock().getZ(), ev.getClickedBlock().getWorld().getUID());

                    Set<BlockKey> blocks = new HashSet<>(
                            ProxyManager.getInstance().map(ev.getClickedBlock())
                                    .stream()
                                    .map(BlockKey::new)
                                    .collect(Collectors.toList())
                    );

                    SanctuaryField field = fields.iterator().next();
                    if (field.canReinforce(blocks)) {
                        field.reinforce(blocks);
//                    ev.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTNT(EntityExplodeEvent ev) {
        Iterator<Block> it = ev.blockList().iterator();

        while (it.hasNext()) {
            Block block = it.next();

            if (SanctuaryManager.getInstance().hasContaining(block.getX(), block.getZ(), block.getWorld().getUID())) {
                it.remove();
//                Collection<? extends SanctuaryField> sancts = SanctuaryManager.getInstance().getContaining(block.getX(), block.getZ(), block.getWorld().getUID());
//
//                for (SanctuaryField sanct : sancts) {
//
//                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInventoryClose(InventoryCloseEvent ev) {
        if (ev.getPlayer() instanceof Player && ev.getInventory().getHolder() instanceof SanctuaryField) {
            if (ev.getViewers().size() == 1) {
                ((SanctuaryField) ev.getInventory().getHolder()).dismissInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryInteract(InventoryMoveItemEvent ev) {
        if (ev.getDestination().getHolder() instanceof SanctuaryField
                && !Sanctuary.getConf().getGrowth().isUpgradeMaterial(ev.getItem())) {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonPush(BlockPistonExtendEvent ev) {
        for (Block block : ev.getBlocks()) {
            if (SanctuaryManager.getInstance().hasContaining(block.getX(), block.getZ(), block.getWorld().getUID())) {
                ev.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonPull(BlockPistonRetractEvent ev) {
        for (Block block : ev.getBlocks()) {
            if (SanctuaryManager.getInstance().hasContaining(block.getX(), block.getZ(), block.getWorld().getUID())) {
                ev.setCancelled(true);
                return;
            }
        }
    }

//    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//    public void onPlayerInventoryInteract(InventoryClickEvent ev) {
//        if (ev.getInventory().getHolder() instanceof SanctuaryField
//                && !Sanctuary.getConf().getGrowth().isUpgradeMaterial(ev.getCurrentItem())) {
//            ev.setCancelled(true);
//        }
//    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent ev) throws ExecutionException, InterruptedException {
        int x = ev.getBlockPlaced().getX(), z = ev.getBlockPlaced().getZ();
        UUID world = ev.getBlockPlaced().getWorld().getUID();

        if (SanctuaryManager.getInstance().hasContaining(x, z, world)) {
            Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(x, z, world);

            for (SanctuaryField field : fields) {
                if (!hasMember(ev.getPlayer().getUniqueId(), field.getGroupID(), Rank.MEMBER)) {
                    if (!new SanctuaryDamageEvent(ev.getPlayer(), Sanctuary.getConf().getDamage().getPlacement().getDamage(), field).call()) {
                        field.damage(Sanctuary.getConf().getDamage().getPlacement().getDamage(), DamageType.PLAYER, ev.getPlayer().getUniqueId());
                    }
                    ev.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplodeEvent(BlockExplodeEvent ev) throws ExecutionException, InterruptedException {
        int x = ev.getBlock().getX(), z = ev.getBlock().getZ();
        UUID world = ev.getBlock().getWorld().getUID();

        if (SanctuaryManager.getInstance().hasContaining(x, z, world)) {
            Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(x, z, world);

            for (SanctuaryField field : fields) {
                ev.setCancelled(true);
                field.damage(Sanctuary.getConf().getDamage().getTnt().getDamage(), DamageType.TNT, new UUID(0, 0));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplodeEvent(EntityExplodeEvent ev) throws ExecutionException, InterruptedException {
        // This patches a Cannons bug where the fired entity has no real entity and terminates in a nullpointer exception.
        try {
            ev.getEntityType();
        } catch (NullPointerException e) {
            LOG.log(Level.WARNING, "Cannons may be responsible for no entity type, here is the exception for completeness. Skipping processing", e);
            return;
        }
        if (ev.getEntityType() == EntityType.PRIMED_TNT || ev.getEntityType() == EntityType.MINECART_TNT) {
            int x = ev.getLocation().getBlockX(), z = ev.getLocation().getBlockZ();
            UUID world = ev.getLocation().getWorld().getUID();

            if (SanctuaryManager.getInstance().hasContaining(x, z, world)) {
                Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(x, z, world);

                for (SanctuaryField field : fields) {
                    ev.setCancelled(true);
                    field.damage(Sanctuary.getConf().getDamage().getTnt().getDamage(), DamageType.TNT, new UUID(0, 0));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpreadEvent(BlockSpreadEvent ev) throws ExecutionException, InterruptedException {
        int sourcex = ev.getSource().getX(), sourcez = ev.getSource().getZ(),
                tgtx = ev.getBlock().getX(), tgtz = ev.getBlock().getZ();

        UUID world = ev.getSource().getWorld().getUID();

        processFromTo(ev.getSource(), sourcex, sourcez, tgtx, tgtz, world, ev);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent ev) throws ExecutionException, InterruptedException {
        int sourcex = ev.getBlock().getX(), sourcez = ev.getBlock().getZ(),
                tgtx = ev.getToBlock().getX(), tgtz = ev.getToBlock().getZ();

        UUID world = ev.getBlock().getWorld().getUID();

        processFromTo(ev.getBlock(), sourcex, sourcez, tgtx, tgtz, world, ev);
    }

    private void processFromTo(Block block, int sourcex, int sourcez, int tgtx, int tgtz, UUID world, Cancellable ev) {
        if (SanctuaryManager.getInstance().hasContaining(tgtx, tgtz, world)) {
            Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(tgtx, tgtz, world);

            for (SanctuaryField field : fields) {
                if (!field.getBounds().contains(sourcex, sourcez)) {
                    ev.setCancelled(true);
//                    System.out.println("Cancelling liquid event.");
//                    if (block.getType() == Material.WATER) {
//                        block.setType(Material.STATIONARY_WATER);
//                    } else if (block.getType() == Material.LAVA) {
//                        block.setType(Material.STATIONARY_LAVA);
//                    }
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPVP(EntityDamageByEntityEvent ev) {
        Entity eattacker = ev.getDamager();
        Entity evictim = ev.getEntity();

        if (eattacker instanceof Player && evictim instanceof Player) {
            Player attacker = (Player) (eattacker), victim = (Player) evictim;
            int x = victim.getLocation().getBlockX(), z = victim.getLocation().getBlockZ();
            UUID world = victim.getLocation().getWorld().getUID();

            if (SanctuaryManager.getInstance().hasContaining(x, z, world)) {
                Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(x, z, world);

                for (SanctuaryField field : fields) {
                    SanctuaryPVPEvent pvp = new SanctuaryPVPEvent(
                            field,
                            attacker,
                            victim,
                            hasMember(attacker.getUniqueId(), field.getGroupID()),
                            hasMember(victim.getUniqueId(), field.getGroupID())
                    );

                    if (pvp.call()) {
                        ev.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionThrow(ProjectileLaunchEvent ev) {
        if (ev.getEntity().getShooter() instanceof Player && ev.getEntity().getType() == EntityType.SPLASH_POTION) {
            Player source = (Player) ev.getEntity().getShooter();
            int x = source.getLocation().getBlockX(), z = source.getLocation().getBlockZ();
            UUID world = source.getLocation().getWorld().getUID();

            if (SanctuaryManager.getInstance().hasContaining(x, z, world)) {
                Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(x, z, world);

                for (SanctuaryField field : fields) {
                    SanctuaryPotionThrowEvent pot = new SanctuaryPotionThrowEvent(
                            field,
                            source,
                            ev.getEntity(),
                            hasMember(source.getUniqueId(), field.getGroupID())
                    );

                    if (pot.call()) {
                        ev.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLaunchProjectile(ProjectileLaunchEvent ev) {
        if (ev.getEntityType() == EntityType.ENDER_PEARL) {
            int x = ev.getEntity().getLocation().getBlockX(), y = ev.getEntity().getLocation().getBlockZ();
            UUID world = ev.getEntity().getWorld().getUID();

            EnderPearl pearl = (EnderPearl) ev.getEntity();

            if (!SanctuaryManager.getInstance().hasContaining(x, y, world)) {
                PearlTracker.getInstance().addPearl(pearl);
            } else {
                ((Player) pearl.getShooter()).sendMessage(ChatColor.RED + "Pearl destroyed by sanctuary.");
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMoveEntity(ProjectileHitEvent ev) {
        if (ev.getEntityType() == EntityType.ENDER_PEARL) {
            PearlTracker.getInstance().removePearl((EnderPearl) ev.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent ev) throws ExecutionException, InterruptedException {
        if (ev.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL || ev.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (SanctuaryManager.getInstance().hasContaining(ev.getTo().getBlockX(), ev.getTo().getBlockZ(), ev.getTo().getWorld().getUID())) {
                Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(ev.getTo().getBlockX(), ev.getTo().getBlockZ(), ev.getTo().getWorld().getUID());
                for (SanctuaryField field : fields) {
                    if (!hasMember(ev.getPlayer().getUniqueId(), field.getGroupID())) {
                        ev.setCancelled(true);
                        ev.getPlayer().sendMessage(ChatColor.RED + "Teleport blocked by sanctuary.");
                        return;
                    }
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent ev) {
        int x = ev.getTo().getBlockX(), z = ev.getTo().getBlockZ();
        UUID world = ev.getTo().getWorld().getUID();

        if (SanctuaryManager.getInstance().hasContaining(x, z, world)) {
            Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(x, z, world);

            for (SanctuaryField field : fields) {
                if (!hasMember(ev.getPlayer().getUniqueId(), field.getGroupID())) {
                    ev.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent ev) throws ExecutionException, InterruptedException {
        if (SanctuaryManager.getInstance().hasContaining(ev.getBlock().getX(), ev.getBlock().getZ(), ev.getBlock().getWorld().getUID())) {
            Collection<? extends SanctuaryField> fields = SanctuaryManager.getInstance().getContaining(ev.getBlock().getX(), ev.getBlock().getZ(), ev.getBlock().getWorld().getUID());
            BlockKey key = new BlockKey(ev.getBlock());
            for (SanctuaryField field : fields) {

                if (!ReinforcementManager.getInstance().isReinforced(key)
                        && !hasMember(ev.getPlayer().getUniqueId(), field.getGroupID(), Rank.MEMBER)
                        && field.canReinforce(key)
                        && Amethyst.getInstance().getConf().isReinforcible(ev.getBlock().getType())) {
                    if (ev.getPlayer().isOp()) {
                        ev.getPlayer().sendMessage("Reinforcing: " + new BlockKey(ev.getBlock()));
                    }
                    field.reinforce(key);
                    ev.setCancelled(true);
                    break;
                }
            }
        }
    }
}
