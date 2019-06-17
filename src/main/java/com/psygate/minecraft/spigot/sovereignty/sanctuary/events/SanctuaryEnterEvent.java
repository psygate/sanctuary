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

package com.psygate.minecraft.spigot.sovereignty.sanctuary.events;

import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryField;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.concurrent.Callable;

/**
 * Created by psygate on 25.05.2016.
 */
public class SanctuaryEnterEvent extends Event implements Callable<Void> {
    private static final HandlerList handlers = new HandlerList();


    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Void call() {
        Bukkit.getServer().getPluginManager().callEvent(this);
        return null;
    }

    private final SanctuaryField sanctuary;
    private final Player player;

    public SanctuaryEnterEvent(Player player, SanctuaryField sanctuary) {
        this.player = player;
        this.sanctuary = sanctuary;
    }

    public SanctuaryField getSanctuary() {
        return sanctuary;
    }

    public Player getPlayer() {
        return player;
    }
}
