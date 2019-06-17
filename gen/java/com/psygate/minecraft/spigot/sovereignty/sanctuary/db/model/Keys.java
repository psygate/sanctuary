/**
 * This class is generated by jOOQ
 */
package com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model;


import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryAnchors;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPayedItems;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPlacementTimes;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPlayerSettings;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryReinforced;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryAnchorsRecord;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryPayedItemsRecord;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryPlacementTimesRecord;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryPlayerSettingsRecord;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryReinforcedRecord;

import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;


/**
 * A class modelling foreign key relationships between tables of the <code>nucleus</code> 
 * schema
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.7.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

	// -------------------------------------------------------------------------
	// IDENTITY definitions
	// -------------------------------------------------------------------------

	public static final Identity<SanctuaryAnchorsRecord, Long> IDENTITY_SANCTUARY_ANCHORS = Identities0.IDENTITY_SANCTUARY_ANCHORS;
	public static final Identity<SanctuaryPayedItemsRecord, Long> IDENTITY_SANCTUARY_PAYED_ITEMS = Identities0.IDENTITY_SANCTUARY_PAYED_ITEMS;

	// -------------------------------------------------------------------------
	// UNIQUE and PRIMARY KEY definitions
	// -------------------------------------------------------------------------

	public static final UniqueKey<SanctuaryAnchorsRecord> KEY_SANCTUARY_ANCHORS_PRIMARY = UniqueKeys0.KEY_SANCTUARY_ANCHORS_PRIMARY;
	public static final UniqueKey<SanctuaryAnchorsRecord> KEY_SANCTUARY_ANCHORS_X = UniqueKeys0.KEY_SANCTUARY_ANCHORS_X;
	public static final UniqueKey<SanctuaryPayedItemsRecord> KEY_SANCTUARY_PAYED_ITEMS_PRIMARY = UniqueKeys0.KEY_SANCTUARY_PAYED_ITEMS_PRIMARY;
	public static final UniqueKey<SanctuaryPlacementTimesRecord> KEY_SANCTUARY_PLACEMENT_TIMES_PRIMARY = UniqueKeys0.KEY_SANCTUARY_PLACEMENT_TIMES_PRIMARY;
	public static final UniqueKey<SanctuaryPlayerSettingsRecord> KEY_SANCTUARY_PLAYER_SETTINGS_PRIMARY = UniqueKeys0.KEY_SANCTUARY_PLAYER_SETTINGS_PRIMARY;
	public static final UniqueKey<SanctuaryReinforcedRecord> KEY_SANCTUARY_REINFORCED_PRIMARY = UniqueKeys0.KEY_SANCTUARY_REINFORCED_PRIMARY;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------

	public static final ForeignKey<SanctuaryPayedItemsRecord, SanctuaryAnchorsRecord> SANCTUARY_PAYED_ITEMS_IBFK_1 = ForeignKeys0.SANCTUARY_PAYED_ITEMS_IBFK_1;
	public static final ForeignKey<SanctuaryReinforcedRecord, SanctuaryAnchorsRecord> SANCTUARY_REINFORCED_IBFK_1 = ForeignKeys0.SANCTUARY_REINFORCED_IBFK_1;

	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class Identities0 extends AbstractKeys {
		public static Identity<SanctuaryAnchorsRecord, Long> IDENTITY_SANCTUARY_ANCHORS = createIdentity(SanctuaryAnchors.SANCTUARY_ANCHORS, SanctuaryAnchors.SANCTUARY_ANCHORS.SANCTUARY_ID);
		public static Identity<SanctuaryPayedItemsRecord, Long> IDENTITY_SANCTUARY_PAYED_ITEMS = createIdentity(SanctuaryPayedItems.SANCTUARY_PAYED_ITEMS, SanctuaryPayedItems.SANCTUARY_PAYED_ITEMS.ITEM_ID);
	}

	private static class UniqueKeys0 extends AbstractKeys {
		public static final UniqueKey<SanctuaryAnchorsRecord> KEY_SANCTUARY_ANCHORS_PRIMARY = createUniqueKey(SanctuaryAnchors.SANCTUARY_ANCHORS, SanctuaryAnchors.SANCTUARY_ANCHORS.SANCTUARY_ID);
		public static final UniqueKey<SanctuaryAnchorsRecord> KEY_SANCTUARY_ANCHORS_X = createUniqueKey(SanctuaryAnchors.SANCTUARY_ANCHORS, SanctuaryAnchors.SANCTUARY_ANCHORS.X, SanctuaryAnchors.SANCTUARY_ANCHORS.Y, SanctuaryAnchors.SANCTUARY_ANCHORS.Z, SanctuaryAnchors.SANCTUARY_ANCHORS.WORLD_UUID);
		public static final UniqueKey<SanctuaryPayedItemsRecord> KEY_SANCTUARY_PAYED_ITEMS_PRIMARY = createUniqueKey(SanctuaryPayedItems.SANCTUARY_PAYED_ITEMS, SanctuaryPayedItems.SANCTUARY_PAYED_ITEMS.ITEM_ID);
		public static final UniqueKey<SanctuaryPlacementTimesRecord> KEY_SANCTUARY_PLACEMENT_TIMES_PRIMARY = createUniqueKey(SanctuaryPlacementTimes.SANCTUARY_PLACEMENT_TIMES, SanctuaryPlacementTimes.SANCTUARY_PLACEMENT_TIMES.CREATOR, SanctuaryPlacementTimes.SANCTUARY_PLACEMENT_TIMES.CREATION);
		public static final UniqueKey<SanctuaryPlayerSettingsRecord> KEY_SANCTUARY_PLAYER_SETTINGS_PRIMARY = createUniqueKey(SanctuaryPlayerSettings.SANCTUARY_PLAYER_SETTINGS, SanctuaryPlayerSettings.SANCTUARY_PLAYER_SETTINGS.PUUID);
		public static final UniqueKey<SanctuaryReinforcedRecord> KEY_SANCTUARY_REINFORCED_PRIMARY = createUniqueKey(SanctuaryReinforced.SANCTUARY_REINFORCED, SanctuaryReinforced.SANCTUARY_REINFORCED.X, SanctuaryReinforced.SANCTUARY_REINFORCED.Y, SanctuaryReinforced.SANCTUARY_REINFORCED.Z, SanctuaryReinforced.SANCTUARY_REINFORCED.WORLD_UUID);
	}

	private static class ForeignKeys0 extends AbstractKeys {
		public static final ForeignKey<SanctuaryPayedItemsRecord, SanctuaryAnchorsRecord> SANCTUARY_PAYED_ITEMS_IBFK_1 = createForeignKey(com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.Keys.KEY_SANCTUARY_ANCHORS_PRIMARY, SanctuaryPayedItems.SANCTUARY_PAYED_ITEMS, SanctuaryPayedItems.SANCTUARY_PAYED_ITEMS.SANCTUARY_ID);
		public static final ForeignKey<SanctuaryReinforcedRecord, SanctuaryAnchorsRecord> SANCTUARY_REINFORCED_IBFK_1 = createForeignKey(com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.Keys.KEY_SANCTUARY_ANCHORS_PRIMARY, SanctuaryReinforced.SANCTUARY_REINFORCED, SanctuaryReinforced.SANCTUARY_REINFORCED.SANCTUARY_ID);
	}
}
