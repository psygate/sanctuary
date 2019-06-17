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

import com.psygate.collections.Pair;
import com.psygate.minecraft.spigot.sovereignty.nucleus.util.mc.BlockKey;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryField;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryModificationListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by psygate on 26.05.2016.
 */
public class ScoreBoardManager {
    private static ScoreBoardManager instance;
    private Map<BlockKey, Pair<Scoreboard, SanctuaryModificationListener>> scoreboardMap = new HashMap<>();

    private ScoreBoardManager() {

    }

    public static ScoreBoardManager getInstance() {
        if (instance == null) {
            instance = new ScoreBoardManager();
        }

        return instance;
    }

    public void add(Player player, SanctuaryField sanctuary, boolean setScoreBoard) {
        scoreboardMap.computeIfAbsent(sanctuary.getLocation(), blockKey -> {
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

            Objective objective = board.registerNewObjective("sanctuary", sanctuary.getName());
            objective.setDisplayName(sanctuary.getName());
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            Team hostile = board.registerNewTeam("Hostile");
            Team friendly = board.registerNewTeam("Friendly");

            hostile.setDisplayName(ChatColor.RED + "[Non-Member]");
            hostile.setNameTagVisibility(NameTagVisibility.ALWAYS);
            hostile.setAllowFriendlyFire(true);
            hostile.setSuffix(hostile.getDisplayName());
//            hostile.setPrefix(hostile.getDisplayName());

            friendly.setDisplayName(ChatColor.GREEN + "[Member]");
            friendly.setNameTagVisibility(NameTagVisibility.ALWAYS);
            friendly.setAllowFriendlyFire(true);
            friendly.setSuffix(friendly.getDisplayName());
//            friendly.setPrefix(friendly.getDisplayName());

            objective.getScore("Max. Health").setScore((int) sanctuary.getMaxHealth());
            objective.getScore("Health").setScore((int) sanctuary.getHealth());
            objective.getScore("Radius").setScore((int) sanctuary.getRadius());

            SanctuaryModificationListener ml = new SanctuaryModificationListener() {
                @Override
                public void onNameChange(String name) {
                    objective.setDisplayName(name);
                }

                @Override
                public void onMaxHealthChange(long health) {
                    objective.getScore("Max. Health").setScore((int) sanctuary.getMaxHealth());
                }

                @Override
                public void onHealthChange(long health) {
                    objective.getScore("Health").setScore((int) sanctuary.getHealth());
                    objective.getScore("Radius").setScore((int) sanctuary.getRadius());
                }
            };
            sanctuary.addChangeListener(ml);
            return new Pair<>(board, ml);
        });

        Scoreboard board = scoreboardMap.get(sanctuary.getLocation()).getKey();

        if (sanctuary.isFriendly(player.getUniqueId())) {
            Team friendly = board.getTeam("Friendly");
            friendly.addPlayer(player);
        } else {
            Team hostile = board.getTeam("Hostile");
            hostile.addPlayer(player);
        }

        if (setScoreBoard) {
            player.setScoreboard(board);
        }
    }

    public void remove(Player player, SanctuaryField sanctuary) {
        if (scoreboardMap.containsKey(sanctuary.getLocation())) {
            Scoreboard board = scoreboardMap.get(sanctuary.getLocation()).getKey();

            if (sanctuary.isFriendly(player.getUniqueId())) {
                Team friendly = board.getTeam("Friendly");
                friendly.removePlayer(player);
            } else {
                Team hostile = board.getTeam("Hostile");
                hostile.removePlayer(player);
            }

            if (board.getTeams().stream().allMatch(v -> v.getPlayers().isEmpty())) {
                sanctuary.removeChangeListener(scoreboardMap.get(sanctuary.getLocation()).getValue());
                scoreboardMap.remove(sanctuary.getLocation());
            }
        }
    }
}
