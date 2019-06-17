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

/**
 * Created by psygate (https://github.com/psygate) on 15.03.2016.
 */
public class SanctuaryCost {
    private Material type;
    private double amount;

    public SanctuaryCost(Material type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    public Material getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public int getIntAmount() {
        return (int) Math.ceil(amount);
    }

    @Override
    public String toString() {
        return "SanctuaryCost{" +
                "type=" + type +
                ", amount=" + amount +
                '}';
    }

    public long getLongAmount() {
        return (long) amount;
    }
}
