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

package com.psygate.minecraft.spigot.sovereignty.sanctuary;

import com.codahale.metrics.Timer;
import com.psygate.minecraft.spigot.sovereignty.nucleus.Nucleus;
import com.psygate.minecraft.spigot.sovereignty.nucleus.managment.NucleusPlugin;
import com.psygate.minecraft.spigot.sovereignty.nucleus.sql.DatabaseInterface;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.notifications.NotificationListener;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.configuration.Configuration;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.data.SanctuaryManager;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.listeners.SanctuaryListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.*;

/**
 * Created by psygate (https://github.com/psygate) on 15.03.2016.
 */
public class Sanctuary extends JavaPlugin implements NucleusPlugin {
    private final static Logger LOG = Logger.getLogger(Sanctuary.class.getName());
    private static Sanctuary instance;
    private Configuration conf;
    private DatabaseInterface dbi;
    private Logger logger;
    private SanctuaryListener listener = new SanctuaryListener();


    static {
        LOG.setUseParentHandlers(false);
        LOG.setLevel(Level.ALL);
        List<Handler> handlers = Arrays.asList(LOG.getHandlers());

        if (handlers.stream().noneMatch(h -> h instanceof FileHandler)) {
            try {
                File logdir = new File("logs/nucleus_logs/sanctuary/");
                if (!logdir.exists()) {
                    logdir.mkdirs();
                }
                FileHandler fh = new FileHandler(
                        "logs/nucleus_logs/sanctuary/sanctuary.%u.%g.log",
                        8 * 1024 * 1024,
                        12,
                        true
                );
                fh.setLevel(Level.ALL);
                fh.setEncoding("UTF-8");
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
                LOG.addHandler(fh);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Logger getLogger(String name) {
        Logger log = Logger.getLogger(name);
        log.setParent(LOG);
        log.setUseParentHandlers(true);
        log.setLevel(Level.ALL);
        return log;
    }


    public static Sanctuary getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Plugin not initialized.");
        }

        return instance;
    }

    public SanctuaryListener getListener() {
        return listener;
    }

    public static DatabaseInterface DBI() {
        return getInstance().dbi;
    }

    public static Configuration getConf() {
        return getInstance().conf;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        conf = new Configuration(getConfig());
        Nucleus.getInstance().register(this);

        if (conf.getInteractions().isDisableBeacons()) {
            Iterator<Recipe> recp = getServer().recipeIterator();
            while (recp.hasNext()) {
                if (recp.next().getResult().getType() == Material.BEACON) {
                    recp.remove();
                }
            }
        }

        Timer.Context ctx = Nucleus.getMetricRegistry().timer("sanctuary-initial-load").time();
        SanctuaryManager.getInstance().loadInitially();

//        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> SanctuaryManager.getInstance().cleanUp(), 20, 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> SanctuaryManager.getInstance().healAll(),
                getConf().getRegeneration().getTicktime(),
                getConf().getRegeneration().getTicktime()
        );
        ctx.stop();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            Bukkit.getOnlinePlayers().stream().filter(Player::isOp).forEach(p -> p.sendMessage(ChatColor.YELLOW + "Sanctuary cache flush initiated."));
            SanctuaryManager.getInstance().flush();
            Bukkit.getOnlinePlayers().stream().filter(Player::isOp).forEach(p -> p.sendMessage(ChatColor.GREEN + "Sanctuary cache flush done."));
        }, 20 * 60 * 30, 20 * 60 * 30);
    }

    @Override
    public void onDisable() {
        SanctuaryManager.getInstance().flush();
        listener.flush();
    }

    @Override
    public int getWantedDBVersion() {
        return 1;
    }

    @Override
    public void fail() {

    }

    @Override
    public List<Listener> getListeners() {
        return Arrays.asList(
                listener,
                NotificationListener.getInstance()
        );

    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Logger getPluginLogger() {
        return logger;
    }

    @Override
    public void setDatabaseInterface(DatabaseInterface databaseInterface) {
        dbi = databaseInterface;
    }
}
