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

import com.psygate.minecraft.spigot.sovereignty.nucleus.sql.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by psygate (https://github.com/psygate) on 27.03.2016.
 */
public class Sanctuaries {
    private final long placementCooldown;
    private Material sanctuaryType;
    private long initialHealth;
    private long healthPerRadiusUnit;
    private Set<String> enabledWorlds = new HashSet<>();
    private boolean disallowPortals;

    public Sanctuaries(ConfigurationSection sec) {
        sanctuaryType = Material.valueOf(sec.getString("sanctuary_type").trim().toUpperCase());
        initialHealth = sec.getLong("sanctuary_initial_health");
        healthPerRadiusUnit = sec.getLong("sanctuary_health_per_radius_unit");
        enabledWorlds.addAll(sec.getStringList("enabled_worlds"));
        disallowPortals = sec.getBoolean("disallow_portals");
        placementCooldown = TimeUtil.parseTimeStringToMillis(sec.getString("placement_cooldown"));
    }

    public Material getSanctuaryType() {
        return sanctuaryType;
    }

    public long getInitialHealth() {
        return initialHealth;
    }

    public long getHealthPerRadiusUnit() {
        return healthPerRadiusUnit;
    }

    public boolean isEnabledWorld(World world) {
        return enabledWorlds.contains(world.getName());
    }

    public boolean isDisallowPortals() {
        return disallowPortals;
    }

    public long getPlacementCooldown() {
        return placementCooldown;
    }
}
