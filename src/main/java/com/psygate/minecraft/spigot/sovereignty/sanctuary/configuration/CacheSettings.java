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

import com.google.common.cache.CacheBuilder;
import com.psygate.minecraft.spigot.sovereignty.nucleus.sql.util.TimeUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.TimeUnit;

/**
 * Created by psygate on 03.05.2016.
 */
public class CacheSettings {
    private long expireAfterWrite;
    private long expireAfterAccess;
    private int initialCapacity;
    private int maximumSize;

    public CacheSettings(ConfigurationSection section) {
        expireAfterWrite = TimeUtil.parseTimeStringToMillis(section.getString("expire_after_write"));
        expireAfterAccess = TimeUtil.parseTimeStringToMillis(section.getString("expire_after_access"));
        initialCapacity = section.getInt("expire_after_access");
        maximumSize = section.getInt("maximum_size");
    }

    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public int getMaximumSize() {
        return maximumSize;
    }


    public <K, V> CacheBuilder getCacheBuilder() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(expireAfterAccess, TimeUnit.MILLISECONDS)
                .expireAfterWrite(expireAfterWrite, TimeUnit.MILLISECONDS)
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize);
    }
}
