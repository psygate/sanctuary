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
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by psygate (https://github.com/psygate) on 27.03.2016.
 */
public class Regeneration {
    private long ticktime;
    private long heal;

    public Regeneration(ConfigurationSection regeneration) {
        ticktime = TimeUtil.parseTimeStringToMillis(regeneration.getString("ticktime"));
        heal = regeneration.getLong("heal");
    }

    public long getTicktime() {
        return ticktime;
    }

    public long getHeal() {
        return heal;
    }
}
