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

package com.psygate.minecraft.spigot.sovereignty.sanctuary.commands;

import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusPlayerCommand;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.notifications.NotificationListener;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.notifications.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * Created by psygate on 23.05.2016.
 */
public class ToggleScoreBoardCommand extends NucleusPlayerCommand {

    public ToggleScoreBoardCommand() {
        super(0, 0);
    }

    @Override
    protected void subOnCommand(Player player, Command command, String s, String[] strings) throws Exception {
        PlayerSettings state = NotificationListener.getInstance().getPlayerSettings(player.getUniqueId());

        boolean newstate = !state.isScoreboard();

        state.setScoreboard(newstate);
        NotificationListener.getInstance().persistPlayerSettings(state);
        if (newstate == false) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        player.sendMessage("Sanctuary scoreboard display now " + ((newstate) ? ChatColor.GREEN + "on" : ChatColor.RED + "off"));
    }

    @Override
    protected String[] getName() {
        return new String[]{"sanctuarytogglescoreboard"};
    }
}
