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

package com.psygate.minecraft.spigot.sovereignty.sanctuary.notifications;

import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryPlayerSettingsRecord;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by psygate on 25.05.2016.
 */
public class PlayerSettings {
    private final UUID player;
    private boolean bossBar = true;
    private boolean notifyOnEnterExit = true;
    private boolean scoreboard = true;

    public PlayerSettings(UUID player) {
        this.player = Objects.requireNonNull(player, () -> "Player UUID cannot be null.");
    }

    public PlayerSettings(SanctuaryPlayerSettingsRecord rec) {
        player = rec.getPuuid();
        bossBar = rec.getBossbarBool();
        notifyOnEnterExit = rec.getEnterExitNotifyBool();
        scoreboard = rec.getScoreboardBool();
    }

    public UUID getPlayer() {
        return player;
    }

    public boolean isBossBar() {
        return bossBar;
    }

    public boolean isNotifyOnEnterExit() {
        return notifyOnEnterExit;
    }

    public void setBossBar(boolean bossBar) {
        this.bossBar = bossBar;
    }

    public void setNotifyOnEnterExit(boolean notifyOnEnterExit) {
        this.notifyOnEnterExit = notifyOnEnterExit;
    }

    public boolean isScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(boolean scoreboard) {
        this.scoreboard = scoreboard;
    }
}
