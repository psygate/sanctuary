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

import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusOPCommand;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * Created by psygate (https://github.com/psygate) on 09.04.2016.
 */
public class ReloadCommand extends NucleusOPCommand {

    public ReloadCommand() {
        super(0, 0);
    }

    @Override
    protected String[] getName() {
        return new String[]{"sanctuaryreload"};
    }

    @Override
    protected void subOnCommand(Player player, Command command, String s, String[] strings) throws Exception {
        Bukkit.getOnlinePlayers().stream().filter(Player::isOp).forEach(p -> p.sendMessage(ChatColor.YELLOW + "Sanctuary reload initiated."));
        SanctuaryManager.getInstance().reload();
        Bukkit.getOnlinePlayers().stream().filter(Player::isOp).forEach(p -> p.sendMessage(ChatColor.GREEN + "Sanctuary reload done."));
    }
}
