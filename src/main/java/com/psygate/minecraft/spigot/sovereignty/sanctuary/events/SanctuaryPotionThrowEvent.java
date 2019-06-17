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
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.concurrent.Callable;

/**
 * Created by psygate on 29.04.2016.
 */
public class SanctuaryPotionThrowEvent extends Event implements Callable<Boolean>, Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Boolean call() {
        Bukkit.getServer().getPluginManager().callEvent(this);
        return cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    private final SanctuaryField sanctuary;
    private final Player source;
    private final Projectile projectile;
    private final boolean sourceIsMember;

    public SanctuaryPotionThrowEvent(SanctuaryField sanctuary, Player source, Projectile projectile, boolean sourceIsMember) {
        this.sanctuary = sanctuary;
        this.source = source;
        this.projectile = projectile;
        this.sourceIsMember = sourceIsMember;
    }

    public SanctuaryField getSanctuary() {
        return sanctuary;
    }

    public Player getSource() {
        return source;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public boolean isSourceIsMember() {
        return sourceIsMember;
    }
}
