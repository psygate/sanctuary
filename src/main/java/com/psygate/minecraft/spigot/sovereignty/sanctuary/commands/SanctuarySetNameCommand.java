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

import com.psygate.minecraft.spigot.sovereignty.ivory.groups.Rank;
import com.psygate.minecraft.spigot.sovereignty.ivory.managment.GroupManager;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.CommandException;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusPlayerCommand;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryField;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Created by psygate on 23.05.2016.
 */
public class SanctuarySetNameCommand extends NucleusPlayerCommand {

    public SanctuarySetNameCommand() {
        super(1, 1);
    }

    @Override
    protected void subOnCommand(Player player, Command command, String s, String[] strings) throws Exception {
        Collection<? extends SanctuaryField> sancts = SanctuaryManager.getInstance().getContaining(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), player.getLocation().getWorld().getUID());
        if (strings[0].length() < 0 || strings[0].length() > 32) {
            throw new CommandException("Illegal name.");
        }
        for (SanctuaryField sanct : sancts) {
            if (player.isOp() || GroupManager.getInstance().getGroup(sanct.getGroupID()).map(g -> g.hasMemberWithRankGE(player.getUniqueId(), Rank.MODERATOR)).orElse(Boolean.FALSE)) {
                sanct.setName(strings[0]);
                player.sendMessage(ChatColor.GREEN + "Renamed sanctuary.");
                return;
            }
        }

        throw new CommandException(ChatColor.RED + "No sanctuaries found.");
    }

    @Override
    protected String[] getName() {
        return new String[]{"sanctuarysetname"};
    }
}
