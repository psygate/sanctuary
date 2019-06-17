/**
 * This class is generated by jOOQ
 */
package com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model;


import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryAnchors;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPayedItems;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPlacementTimes;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPlayerSettings;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryReinforced;

import javax.annotation.Generated;


/**
 * Convenience access to all tables in nucleus
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.7.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

	/**
	 * The table nucleus.sanctuary_anchors
	 */
	public static final SanctuaryAnchors SANCTUARY_ANCHORS = com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryAnchors.SANCTUARY_ANCHORS;

	/**
	 * The table nucleus.sanctuary_payed_items
	 */
	public static final SanctuaryPayedItems SANCTUARY_PAYED_ITEMS = com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPayedItems.SANCTUARY_PAYED_ITEMS;

	/**
	 * The table nucleus.sanctuary_placement_times
	 */
	public static final SanctuaryPlacementTimes SANCTUARY_PLACEMENT_TIMES = com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPlacementTimes.SANCTUARY_PLACEMENT_TIMES;

	/**
	 * The table nucleus.sanctuary_player_settings
	 */
	public static final SanctuaryPlayerSettings SANCTUARY_PLAYER_SETTINGS = com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPlayerSettings.SANCTUARY_PLAYER_SETTINGS;

	/**
	 * The table nucleus.sanctuary_reinforced
	 */
	public static final SanctuaryReinforced SANCTUARY_REINFORCED = com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryReinforced.SANCTUARY_REINFORCED;
}