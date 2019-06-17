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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.psygate.minecraft.spigot.sovereignty.ivory.groups.Rank;
import com.psygate.minecraft.spigot.sovereignty.ivory.managment.GroupManager;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryField;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.Tables;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryPlayerSettingsRecord;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.events.SanctuaryEnterEvent;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.events.SanctuaryExitEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.jooq.impl.DSL;
import org.stringtemplate.v4.ST;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.psygate.minecraft.spigot.sovereignty.sanctuary.util.GroupHelper.hasMember;

/**
 * Created by psygate on 25.05.2016.
 */
public class NotificationListener implements Listener {
    private static NotificationListener instance;

    private Scoreboard EMPTY_SCORE_BOARD;
    private final LoadingCache<UUID, PlayerSettings> settingsCache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .initialCapacity(100)
            .maximumSize(1000)
            .build(new CacheLoader<UUID, PlayerSettings>() {
                @Override
                public PlayerSettings load(UUID uuid) throws Exception {
                    Optional<SanctuaryPlayerSettingsRecord> opt = Sanctuary.DBI().submit((conf) -> {
                        return DSL.using(conf).selectFrom(Tables.SANCTUARY_PLAYER_SETTINGS)
                                .where(Tables.SANCTUARY_PLAYER_SETTINGS.PUUID.eq(uuid))
                                .fetchOptional();
                    });

                    return opt.map(PlayerSettings::new).orElseGet(() -> new PlayerSettings(uuid));
                }
            });

    private NotificationListener() {

    }

    public static NotificationListener getInstance() {
        if (instance == null) {
            instance = new NotificationListener();
        }
        return instance;
    }

    public PlayerSettings getPlayerSettings(UUID uuid) {
        return settingsCache.getUnchecked(uuid);
    }

    public void persistPlayerSettings(PlayerSettings settings) {
        settingsCache.put(settings.getPlayer(), settings);
        Sanctuary.DBI().asyncSubmit((conf) -> {
            SanctuaryPlayerSettingsRecord rec = new SanctuaryPlayerSettingsRecord();
            rec.setPuuid(settings.getPlayer());
            rec.setBossbarBool(settings.isBossBar());
            rec.setEnterExitNotifyBool(settings.isNotifyOnEnterExit());
            rec.setScoreboardBool(settings.isScoreboard());

            DSL.using(conf).insertInto(Tables.SANCTUARY_PLAYER_SETTINGS)
                    .set(rec)
                    .onDuplicateKeyUpdate()
                    .set(rec)
                    .execute();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnter(SanctuaryEnterEvent ev) {
        PlayerSettings set = settingsCache.getUnchecked(ev.getPlayer().getUniqueId());
        if (set.isNotifyOnEnterExit()) {
            if (hasMember(ev.getPlayer().getUniqueId(), ev.getSanctuary().getGroupID())) {
                ST st = new ST(Sanctuary.getConf().getMessages().getEnterFriendly());
                ev.getPlayer().sendMessage(bind(st, ev.getPlayer(), ev.getSanctuary()).render());
            } else {
                ST st = new ST(Sanctuary.getConf().getMessages().getEnterHostile());
                ev.getPlayer().sendMessage(bind(st, ev.getPlayer(), ev.getSanctuary()).render());
            }
        }

        if (GroupManager.getInstance().getGroup(ev.getSanctuary().getGroupID()).get().hasMemberWithRankGE(ev.getPlayer().getUniqueId(), Rank.MEMBER)) {
            ScoreBoardManager.getInstance().add(ev.getPlayer(), ev.getSanctuary(), set.isScoreboard());
        }
    }

    private ST bind(ST st, Player player, SanctuaryField sanctuary) {
        ChatColor[] vals = ChatColor.values();

        for (int i = 0; i < vals.length; i++) {
            st.add(vals[i].name().toLowerCase(), vals[i].toString());
        }

        st.add("name", sanctuary.getName());

        return st;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExit(SanctuaryExitEvent ev) {
        PlayerSettings set = settingsCache.getUnchecked(ev.getPlayer().getUniqueId());
        if (set.isNotifyOnEnterExit()) {
            if (hasMember(ev.getPlayer().getUniqueId(), ev.getSanctuary().getGroupID())) {
                ST st = new ST(Sanctuary.getConf().getMessages().getExitFriendly());
                ev.getPlayer().sendMessage(bind(st, ev.getPlayer(), ev.getSanctuary()).render());
            } else {
                ST st = new ST(Sanctuary.getConf().getMessages().getExitHostile());
                ev.getPlayer().sendMessage(bind(st, ev.getPlayer(), ev.getSanctuary()).render());
            }
        }

        ScoreBoardManager.getInstance().remove(ev.getPlayer(), ev.getSanctuary());
        ev.getPlayer().setScoreboard(getEmptyScoreBoard());
    }

    public Scoreboard getEmptyScoreBoard() {
        if (EMPTY_SCORE_BOARD == null) {
            this.EMPTY_SCORE_BOARD = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        return EMPTY_SCORE_BOARD;
    }
}
