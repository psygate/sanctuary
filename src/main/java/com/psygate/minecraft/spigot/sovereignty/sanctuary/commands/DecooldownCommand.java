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

import com.psygate.minecraft.spigot.sovereignty.amethyst.reinforcement.ReinforcementManager;
import com.psygate.minecraft.spigot.sovereignty.ivory.groups.Group;
import com.psygate.minecraft.spigot.sovereignty.ivory.managment.GroupManager;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.CommandException;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusOPCommand;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusOPConsoleCommand;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusPlayerCommand;
import com.psygate.minecraft.spigot.sovereignty.nucleus.util.mc.BlockKey;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by psygate (https://github.com/psygate) on 09.04.2016.
 */
public class DecooldownCommand extends NucleusOPConsoleCommand {

    public DecooldownCommand() {
        super(1, 1);
    }

    @Override
    protected void subOnCommandOP(CommandSender commandSender, Command command, String s, String[] strings) throws Exception {
        Player p = Bukkit.getPlayer(strings[0]);

        if (p == null) {
            throw new CommandException("Player not found.");
        } else {
            Sanctuary.getInstance().getListener().uncooldown(p.getUniqueId());
            commandSender.sendMessage(ChatColor.GREEN + "Removed sanctuary cooldown from " + strings[0]);
        }
    }

    @Override
    protected String[] getName() {
        return new String[]{"decooldown"};
    }
}
