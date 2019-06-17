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

import org.bukkit.configuration.ConfigurationSection;
import org.stringtemplate.v4.ST;

/**
 * Created by psygate on 25.05.2016.
 */
public class Messages {
    private String enterFriendly;
    private String enterHostile;
    private String exitFriendly;
    private String exitHostile;

    public Messages(ConfigurationSection messages) {
        enterFriendly = messages.getString("enter.friendly");
        enterHostile = messages.getString("enter.hostile");
        exitFriendly = messages.getString("exit.friendly");
        exitHostile = messages.getString("exit.hostile");
    }

    public String getEnterFriendly() {
        return enterFriendly;
    }

    public String getEnterHostile() {
        return enterHostile;
    }

    public String getExitFriendly() {
        return exitFriendly;
    }

    public String getExitHostile() {
        return exitHostile;
    }
}
