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
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.util.GroupHelper;
import com.psygate.spatial.primitives.IntAABB2D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

/**
 * Created by psygate (https://github.com/psygate) on 28.03.2016.
 */
class LSanctuaryField implements SanctuaryField {
    private Long id;
    private long creationTime;
    private UUID creator;
    private Long groupID;
    private BlockKey location;
    private long health;
    private long maxHealth;
    private SanctuaryState state;
    private String name;
    private IntAABB2D bounds;
    private Inventory inventory = null;
    private final ItemMap items;
    private Map<UUID, Long> playerDamage = new HashMap<>();
    private long tntdamage = 0;
    private SanctuaryManager manager;
    private ArrayList<SanctuaryModificationListener> listeners = new ArrayList<>(10);
    private long healingTimeout = -1;

    public LSanctuaryField(long creationTime, UUID creator, Long groupID, BlockKey location, long health, long maxHealth, String name, SanctuaryManager manager) {
        this.id = null;
        this.creationTime = creationTime;
        this.creator = Objects.requireNonNull(creator);
        this.groupID = Objects.requireNonNull(groupID);
        this.location = Objects.requireNonNull(location);
        this.health = health;
        this.maxHealth = maxHealth;
        this.name = Objects.requireNonNull(name);
        bounds = calculateBounds();
        state = SanctuaryState.NEW;
        this.manager = manager;
        items = new ItemMap(getUpgradeCosts());
    }

    public LSanctuaryField(Long id, long creationTime, UUID creator, Long groupID, BlockKey location, long health, long maxHealth, String name, Map<Material, Integer> items, SanctuaryManager manager) {
        this(creationTime, creator, groupID, location, health, maxHealth, name, manager);
        this.id = id;
        state = SanctuaryState.CLEAN;
    }


    public void cleanup() {
        Iterator<Long> it = playerDamage.values().iterator();
        long time = System.currentTimeMillis();

        while (it.hasNext()) {
            if (time - it.next() > Sanctuary.getConf().getDamage().getPlacement().getTimeout()) {
                it.remove();
            }
        }
    }

    void setState(SanctuaryState newstate) {
        if (state != SanctuaryState.DELETED || state == SanctuaryState.NEW) {
            state = newstate;
        }
    }

    Long getId() {
        return id;
    }

    void setId(Long id) {
        setState(SanctuaryState.DIRTY);
        this.id = id;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(long creationTime) {
        setState(SanctuaryState.DIRTY);
        this.creationTime = creationTime;
    }

    @Override
    public UUID getCreator() {
        return creator;
    }

    @Override
    public void setCreator(UUID creator) {
        setState(SanctuaryState.DIRTY);
        this.creator = creator;
    }

    @Override
    public Long getGroupID() {
        return groupID;
    }

    @Override
    public void setGroupID(Long groupID) {
        setState(SanctuaryState.DIRTY);
        this.groupID = groupID;
    }

    @Override
    public BlockKey getLocation() {
        return location;
    }

    @Override
    public void setLocation(BlockKey location) {
        setState(SanctuaryState.DIRTY);
        this.location = location;
    }

    @Override
    public long getHealth() {
        return health;
    }

    @Override
    public void setHealth(long health) {
        if (health < 0) {
            health = 0;
        }

        health = (health > maxHealth) ? maxHealth : health;
        if (health != this.health) {
            manager.getMap().remove(this);
            setState(SanctuaryState.DIRTY);

            this.health = health;
            bounds = calculateBounds();
            manager.getMap().add(this);
            listeners.forEach(sanctuaryModificationListener -> sanctuaryModificationListener.onHealthChange(this.health));
        }
    }

    public long getHealingTimeout() {
        return healingTimeout;
    }

    @Override
    public void damage(long damage, DamageType type, UUID source) {
        long time = System.currentTimeMillis();

        switch (type) {
            case PLAYER:
                if (Sanctuary.getConf().getDamage().getPlacement().isGlobal()) {
                    source = new UUID(0, 0);
                }

                if (time - playerDamage.getOrDefault(source, Long.valueOf(0)) > Sanctuary.getConf().getDamage().getPlacement().getTimeout()) {
                    damage(damage);
                    playerDamage.put(source, time);
                }
                break;
            case TNT:
                if (time - tntdamage > Sanctuary.getConf().getDamage().getPlacement().getTimeout()) {
                    damage(damage);
                    tntdamage = time;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown damage type: " + type);
        }
    }

    @Override
    public void damage(long damage) {
        long time = System.currentTimeMillis();
        healingTimeout = time + Sanctuary.getConf().getDamage().getHealingTimeout();
        setHealth(getHealth() - damage);
    }

    @Override
    public boolean canReinforce(BlockKey key) {
        if (getState() == SanctuaryState.NEW) {
            manager.getIOHandler().syncWrite(this);
        }

        return !Sanctuary.getConf().getIgnoredTypes().isIgnored(key.getLocation().getBlock().getType()) && manager.canReinforce(key, this);
    }

    @Override
    public boolean canReinforce(Set<BlockKey> blocks) {
        return blocks.stream().map(this::canReinforce).allMatch(v -> v.booleanValue() == true);
    }

    @Override
    public boolean reinforce(BlockKey key) {
        if (getState() == SanctuaryState.NEW) {
            manager.getIOHandler().syncWrite(this);
        }

        return manager.reinforce(key, this);
    }

    @Override
    public void reinforce(Set<BlockKey> blocks) {
        if (getState() == SanctuaryState.NEW) {
            manager.getIOHandler().syncWrite(this);
        }

        manager.reinforce(blocks, this);
    }

    @Override
    public boolean isFriendly(UUID player) {
        return GroupHelper.hasMember(player, getGroupID());
    }

    @Override
    public long getRadius() {
        return getHealth() / Sanctuary.getConf().getSanctuaries().getHealthPerRadiusUnit();
    }

    @Override
    public void addChangeListener(SanctuaryModificationListener listener) {
        listeners.add(Objects.requireNonNull(listener, () -> "Modification listener cannot be null."));
    }

    @Override
    public void removeChangeListener(SanctuaryModificationListener listener) {
        listeners.remove(listener);
    }

    @Override
    public long getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void setMaxHealth(long maxHealth) {
        setState(SanctuaryState.DIRTY);
        this.maxHealth = maxHealth;
        listeners.forEach(sanctuaryModificationListener -> sanctuaryModificationListener.onMaxHealthChange(maxHealth));
    }

    public void delete() {
        if (state != SanctuaryState.NEW) {
            setState(SanctuaryState.DELETED);
        }

        manager.deleteSanctuary(this);
    }

    @Override
    public IntAABB2D getBounds() {
        return bounds;
    }

    private IntAABB2D calculateBounds() {
        return IntAABB2D.fromCenter(location.getX(), location.getZ(), (int) (getHealth() / Sanctuary.getConf().getSanctuaries().getHealthPerRadiusUnit()));
    }

    public void setName(String name) {
        if (!this.name.equals(name) && name != null) {
            this.name = name;
            setState(SanctuaryState.DIRTY);
            listeners.forEach(sanctuaryModificationListener -> sanctuaryModificationListener.onNameChange(name));
        }
    }

    @Override
    public Inventory getInventory() {
        if (inventory == null) {
            Inventory inv = Bukkit.createInventory(this, 9 * 3, "Sanctuary Inventory");
            this.inventory = inv;
            inv.addItem(createInfoBook());
        }

        return inventory;
    }

    private ItemStack createInfoBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        String title = createBookTitle();

        meta.setTitle(title);
        meta.setAuthor(title);
        ArrayList<String> pages = new ArrayList<>(
                Arrays.asList(
                        ChatColor.GOLD + this.getName() + "\n"
                                + ChatColor.BLACK + "----------------" + "\n"
                                + ChatColor.BLACK + "Health: " + getHealth() + "\n"
                                + ChatColor.BLACK + "Max. Health: " + getMaxHealth()
                                + ChatColor.BLACK + "----------------" + "\n"
                                + ChatColor.BLACK + "Protected Blocks: \n" + coveredBlocks() + "\n"
                                + ChatColor.BLACK + "Next protected Blocks: \n" + nextCoveredBlocks() + "\n"
                )
        );

        StringBuilder page = new StringBuilder();
        int linecntr = 0;
        List<Pair<ItemStack, ItemStack>> items = this.items.getItems();
        for (Pair<ItemStack, ItemStack> itempair : items) {
            linecntr++;
            page.append(itempair.getKey().getType()).append(":").append("\n");
            if (itempair.getKey().getAmount() < itempair.getValue().getAmount()) {
                page.append(ChatColor.RED.toString());
            } else {
                page.append(ChatColor.GREEN.toString());
            }
            page.append(itempair.getKey().getAmount()).append("/").append(itempair.getValue().getAmount()).append("\n");
            linecntr++;

            if (linecntr > 13 || page.length() > 200) {
                pages.add(page.toString());
                page.setLength(0);
            }
        }
        pages.add(page.toString());

        meta.setPages(pages);
        book.setItemMeta(meta);
        book.setAmount(Material.WRITTEN_BOOK.getMaxStackSize());
        return book;
    }

    private Map<Material, Integer> getUpgradeCosts() {
        Map<Material, Integer> upgradeCosts = new HashMap<>();
        double x = (getMaxHealth() / Sanctuary.getConf().getSanctuaries().getHealthPerRadiusUnit()) * 2;
        double quartz = Math.max(0, Math.ceil((0.015 * ((x * 4) + 1)) * (0.0025 * (600 - x))));
        double gold = Math.max(0, Math.ceil((0.016 * ((x * 4) + 1)) * (0.0025 * (x - 200))));

        if (quartz > 0) {
            upgradeCosts.put(Material.QUARTZ_BLOCK, (int) quartz);
        }

        if (gold > 0) {
            upgradeCosts.put(Material.GOLD_INGOT, (int) gold);
        }
//        Map<Material, Integer> upgradeCosts = new HashMap<>();
//        int nextcoverage = nextUpgradeSize();
//        for (Pair<Material, Double> pair : Sanctuary.getConf().getGrowth().getCostPerBlock()) {
//            upgradeCosts.put(pair.getKey(), (int) Math.ceil(pair.getValue() * nextcoverage));
//        }

        return upgradeCosts;
    }

    private int nextUpgradeSize() {
        return nextCoveredBlocks() - coveredBlocks();
    }

    private int coveredBlocks() {
        return bounds.getArea() * 256;
    }

    private int nextCoveredBlocks() {
        IntAABB2D bounds = getBounds();
        IntAABB2D nextBounds = new IntAABB2D(bounds.getLower().getX() - 1, bounds.getLower().getY() - 1, bounds.getUpper().getX() + 1, bounds.getUpper().getY() + 1);
        return nextBounds.getArea() * 256;
    }

    private boolean isSanctuaryBook(ItemStack stack) {
        return stack != null
                && stack.hasItemMeta()
                && stack.getItemMeta() instanceof BookMeta
                && ((BookMeta) stack.getItemMeta()).getAuthor().equals(createBookTitle());
    }

    private String createBookTitle() {
        String title = (ChatColor.GOLD + getName());
        title = title.substring(0, Math.min(title.length(), 16));
        return title;
    }

    @Override
    public void dismissInventory() {
        assert this.inventory != null : "Inventory is null, bogus dismissal.";

//        this.inventory.getViewers().forEach(entity -> entity.closeInventory());
        if (getHealth() < getMaxHealth()) {
            Inventory inv = this.inventory;
            this.inventory = null;
            if (inv.getViewers().size() > 0) {
                inv.getViewers().get(0).sendMessage(ChatColor.RED + "Sanctuary not at full health, upgrading is not possible at this stage.");
            }
            Location loc = location.getLocation();
            for (ItemStack stack : inv.getContents()) {
                if (stack != null && stack.getType() != Material.AIR && !isSanctuaryBook(stack)) {
                    location.getWorld().dropItemNaturally(loc, stack);
                }
            }
            inv.clear();
            return;
        } else {
            Inventory inv = this.inventory;
            this.inventory = null;
            LinkedList<ItemStack> stacks = new LinkedList<>();

            for (ItemStack stack : inv.getContents()) {
                if (stack != null && stack.getType() != Material.AIR && !isSanctuaryBook(stack)) {
                    stacks.add(stack);
                }
            }

            inv.clear();

            items.addAll(stacks).forEach(item -> location.getWorld().dropItemNaturally(location.getLocation(), item));
            setState(SanctuaryState.DIRTY);
            checkUpgrade();
//            if (!upgrade.isEmpty()) {
//                setState(SanctuaryState.DIRTY);
//                for (ItemStack stack : upgrade) {
//                    items.computeIfPresent(stack.getType(), (material, integer) -> integer + stack.getAmount());
//                    items.computeIfAbsent(stack.getType(), material -> stack.getAmount());
//                }
//                checkUpgrade();
//            }
        }
    }

    private Pair<ItemStack, ItemStack> splitStack(int newsum, Integer required, ItemStack stack) {
        int diff = newsum - required;
        return new Pair<>(new ItemStack(stack.getType(), stack.getAmount() - diff), new ItemStack(stack.getType(), diff));
    }

    private void checkUpgrade() {
        if (items.isSatisfied()) {
            setMaxHealth(getMaxHealth() + Sanctuary.getConf().getSanctuaries().getHealthPerRadiusUnit());
            setState(SanctuaryState.DIRTY);
            items.clear(getUpgradeCosts());
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LSanctuaryField that = (LSanctuaryField) o;

        return location != null ? location.equals(that.location) : that.location == null;
    }

    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }

    public Map<Material, Integer> getItems() {
        return items.getValues();
    }

    public SanctuaryState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "[" + getName() + ", (" + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName() + "]";
    }
}
