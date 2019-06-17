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

package com.psygate.minecraft.spigot.sovereignty.sanctuary.configuration;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by psygate on 20.06.2016.
 */
public class IgnoredTypesConfiguration {
    private final Set<Material> types = new HashSet<>();

    public IgnoredTypesConfiguration(List<String> stypes) {
        stypes.stream()
                .map(String::toUpperCase)
                .map(String::trim)
                .map(Material::valueOf)
                .forEach(types::add);
    }

    public boolean isIgnored(Material type) {
        return types.contains(type);
    }
}
