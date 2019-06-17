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
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusCommand;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusPlayerCommand;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryField;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by psygate on 23.05.2016.
 */
public class SanctuaryInfoCommand extends NucleusPlayerCommand {

    public SanctuaryInfoCommand() {
        super(0, 0);
    }

    @Override
    protected void subOnCommand(Player player, Command command, String s, String[] strings) throws Exception {
        Collection<? extends SanctuaryField> sancts = SanctuaryManager.getInstance().getContaining(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), player.getLocation().getWorld().getUID());

        for (SanctuaryField sanct : sancts) {
            if (player.isOp() || GroupManager.getInstance().getGroup(sanct.getGroupID()).map(g -> g.hasMemberWithRankGE(player.getUniqueId(), Rank.MEMBER)).orElse(Boolean.FALSE)) {
                String[] data = new String[]{
                        ChatColor.YELLOW + "----------------" + sanct.getName() + "----------------",
                        "Health:           " + sanct.getHealth(),
                        "Max. Health:      " + sanct.getMaxHealth(),
                        "Protected Blocks: " + sanct.getBounds().getArea() * 256,
                        "Radius:           " + (sanct.getHealth() / Sanctuary.getConf().getSanctuaries().getHealthPerRadiusUnit())
                };

                player.sendMessage(data);

                if (player.isOp()) {
                    player.sendMessage("Location: " + sanct.getLocation());
                }
            }
        }
    }

    @Override
    protected String[] getName() {
        return new String[]{"sanctuaryinfo"};
    }
}
