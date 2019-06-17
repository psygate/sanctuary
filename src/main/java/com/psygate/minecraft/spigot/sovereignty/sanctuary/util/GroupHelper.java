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

package com.psygate.minecraft.spigot.sovereignty.sanctuary.util;

import com.psygate.minecraft.spigot.sovereignty.ivory.groups.Rank;
import com.psygate.minecraft.spigot.sovereignty.ivory.managment.GroupManager;

import java.util.UUID;

/**
 * Created by psygate on 25.05.2016.
 */
public class GroupHelper {

    public static boolean hasMember(UUID uuid, long groupID) {
        return hasMember(uuid, groupID, Rank.GUEST);
    }

    public static boolean hasMember(UUID uuid, long groupID, Rank rank) {
        return GroupManager.getInstance().getGroup(groupID).get().hasMemberWithRankGE(uuid, rank);
    }
}
