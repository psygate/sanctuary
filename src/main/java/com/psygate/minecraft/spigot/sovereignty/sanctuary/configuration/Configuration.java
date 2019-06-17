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

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by psygate (https://github.com/psygate) on 15.03.2016.
 */
public class Configuration {
    private final IgnoredTypesConfiguration ignoredTypes;
    private Sanctuaries sanctuaries;
    private Damage damage;
    private Regeneration regeneration;
    private Growth growth;
    private Interactions interactions;
    private CacheSettings cacheSettings;
    private Messages messages;

    public Configuration(FileConfiguration conf) {
        sanctuaries = new Sanctuaries(conf.getConfigurationSection("sanctuaries"));
        damage = new Damage(conf.getConfigurationSection("damage"));
        regeneration = new Regeneration(conf.getConfigurationSection("regeneration"));
        growth = new Growth(conf.getConfigurationSection("growth"));
        interactions = new Interactions(conf.getConfigurationSection("interactions"));
        cacheSettings = new CacheSettings(conf.getConfigurationSection("cache"));
        messages = new Messages(conf.getConfigurationSection("messages"));
        ignoredTypes = new IgnoredTypesConfiguration(conf.getStringList("ignored_block_types"));
    }

    public Sanctuaries getSanctuaries() {
        return sanctuaries;
    }

    public Damage getDamage() {
        return damage;
    }

    public Regeneration getRegeneration() {
        return regeneration;
    }

    public Growth getGrowth() {
        return growth;
    }

    public Interactions getInteractions() {
        return interactions;
    }

    public CacheSettings getCacheSettings() {
        return cacheSettings;
    }

    public IgnoredTypesConfiguration getIgnoredTypes() {
        return ignoredTypes;
    }

    public Messages getMessages() {
        return messages;
    }
}
