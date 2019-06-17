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

import com.psygate.minecraft.spigot.sovereignty.nucleus.commands.util.NucleusCommand;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by psygate (https://github.com/psygate) on 09.04.2016.
 */
public class ListLoadedSanctuariesCommand extends NucleusCommand {
    public ListLoadedSanctuariesCommand() {
        super(0, 0);
    }

    @Override
    protected void subOnCommand(CommandSender commandSender, Command command, String s, String[] strings) throws Exception {
        commandSender.sendMessage(new String[]{"Map: ", SanctuaryManager.getInstance().getSanctuaryMap().toString()});
//        commandSender.sendMessage(new String[] {"Trees: ", SanctuaryManager.getInstance().getSanctuaryMap()});
    }

    @Override
    protected String[] getName() {
        return new String[]{"listloadedsanctuaries"};
    }
}
