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
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusCommand;
import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusPlayerCommand;
import com.psygate.minecraft.spigot.sovereignty.nucleus.util.mc.BlockKey;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by psygate (https://github.com/psygate) on 09.04.2016.
 */
public class SpawnHostileCommand extends NucleusPlayerCommand {

    public SpawnHostileCommand() {
        super(0, 0);
    }

    @Override
    protected void subOnCommand(Player player, Command command, String s, String[] strings) throws Exception {
        Optional<? extends Group> groupopt = GroupManager.getInstance().getGroup("SANCTUARY_HOSTILE");
        long gid;
        if (!groupopt.isPresent()) {
            gid = GroupManager.getInstance().createGroup("SANCTUARY_HOSTILE", new UUID(0, 0)).get().getGroupID();
        } else {
            gid = groupopt.get().getGroupID();
        }

        player.getLocation().getBlock().setType(Sanctuary.getConf().getSanctuaries().getSanctuaryType());
        ReinforcementManager.getInstance().createReinforcement(
                new LinkedList<>(),
                new HashSet<>(Arrays.asList(new BlockKey(player.getLocation().getBlock()))),
                gid,
                true,
                false,
                25,
                25,
                System.currentTimeMillis() + 1000,
                System.currentTimeMillis(),
                new UUID(0, 0)
        );

        SanctuaryManager.getInstance().createSanctuary(new UUID(0, 0), new BlockKey(player.getLocation().getBlock()), gid);
    }

    @Override
    protected String[] getName() {
        return new String[]{"spawnhostile"};
    }
}
