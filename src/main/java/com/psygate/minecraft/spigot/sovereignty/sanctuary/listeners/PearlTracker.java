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

import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by psygate (https://github.com/psygate) on 28.03.2016.
 */
class PearlTracker implements Runnable {
    private static PearlTracker instance;

    public static PearlTracker getInstance() {
        if (instance == null) {
            instance = new PearlTracker();
        }
        return instance;
    }

    private Set<EnderPearl> pearls = new HashSet<>();
    private int jobID;
    private boolean scheduled = false;
    private int idleCycles = 0;

    public void addPearl(EnderPearl pearl) {
        pearls.add(pearl);
        checkSchedule();
    }

    private void checkSchedule() {
        if (!scheduled) {
            jobID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sanctuary.getInstance(), this, 1, 1);
        }
    }

    @Override
    public void run() {
        if (pearls.isEmpty()) {
            idleCycles++;
        }

        if (idleCycles > 200) {
            Bukkit.getScheduler().cancelTask(jobID);
            scheduled = false;
        }

        Iterator<EnderPearl> it = pearls.iterator();
        while (it.hasNext()) {
            EnderPearl pearl = it.next();
            if (!SanctuaryManager.getInstance().getContaining(pearl.getLocation().getBlockX(), pearl.getLocation().getBlockZ(), pearl.getWorld().getUID()).isEmpty()) {
                if (pearl.getShooter() instanceof Player) {
                    ((Player) pearl.getShooter()).sendMessage(ChatColor.RED + "Pearl destroyed by sanctuary.");
                }
                pearl.remove();
                it.remove();
            }
        }
    }

    public void removePearl(EnderPearl pearl) {
        pearls.remove(pearl);
    }
}
