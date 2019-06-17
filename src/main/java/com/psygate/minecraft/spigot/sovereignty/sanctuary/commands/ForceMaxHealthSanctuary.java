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

import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.CommandException;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusOPCommand;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * Created by psygate (https://github.com/psygate) on 09.04.2016.
 */
public class ForceMaxHealthSanctuary extends NucleusOPCommand {

    public ForceMaxHealthSanctuary() {
        super(1, 1);
    }

    @Override
    protected String[] getName() {
        return new String[]{"forcemaxhealth"};
    }

    @Override
    protected void subOnCommand(Player player, Command command, String s, String[] strings) throws Exception {
        try {
            Long maxhealth = Long.parseLong(strings[0]);

            if (maxhealth <= 0) {
                throw new NumberFormatException();
            }

            SanctuaryManager.getInstance().getContaining(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), player.getWorld().getUID())
                    .forEach(sanctuary -> {
                        sanctuary.setMaxHealth(maxhealth);
                        player.sendMessage("Force set max health on sanctuary: " + sanctuary);
                    });
        } catch (NumberFormatException e) {
            throw new CommandException("First argument must be a positive integer.");
        }
    }
}
