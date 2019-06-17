/**
 * This class is generated by jOOQ
 */
package com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records;


import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.SanctuaryPlacementTimes;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.interfaces.ISanctuaryPlacementTimes;

import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.7.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SanctuaryPlacementTimesRecord extends UpdatableRecordImpl<SanctuaryPlacementTimesRecord> implements Record2<UUID, Timestamp>, ISanctuaryPlacementTimes {

	private static final long serialVersionUID = -572651856;

	/**
	 * Setter for <code>nucleus.sanctuary_placement_times.creator</code>.
	 */
	public void setCreator(UUID value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>nucleus.sanctuary_placement_times.creator</code>.
	 */
	@Override
	public UUID getCreator() {
		return (UUID) getValue(0);
	}

	/**
	 * Setter for <code>nucleus.sanctuary_placement_times.creation</code>.
	 */
	public void setCreation(Timestamp value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>nucleus.sanctuary_placement_times.creation</code>.
	 */
	@Override
	public Timestamp getCreation() {
		return (Timestamp) getValue(1);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record2<UUID, Timestamp> key() {
		return (Record2) super.key();
	}

	// -------------------------------------------------------------------------
	// Record2 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row2<UUID, Timestamp> fieldsRow() {
		return (Row2) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row2<UUID, Timestamp> valuesRow() {
		return (Row2) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<UUID> field1() {
		return SanctuaryPlacementTimes.SANCTUARY_PLACEMENT_TIMES.CREATOR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field2() {
		return SanctuaryPlacementTimes.SANCTUARY_PLACEMENT_TIMES.CREATION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UUID value1() {
		return getCreator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value2() {
		return getCreation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SanctuaryPlacementTimesRecord value1(UUID value) {
		setCreator(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SanctuaryPlacementTimesRecord value2(Timestamp value) {
		setCreation(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SanctuaryPlacementTimesRecord values(UUID value1, Timestamp value2) {
		value1(value1);
		value2(value2);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached SanctuaryPlacementTimesRecord
	 */
	public SanctuaryPlacementTimesRecord() {
		super(SanctuaryPlacementTimes.SANCTUARY_PLACEMENT_TIMES);
	}

	/**
	 * Create a detached, initialised SanctuaryPlacementTimesRecord
	 */
	public SanctuaryPlacementTimesRecord(UUID creator, Timestamp creation) {
		super(SanctuaryPlacementTimes.SANCTUARY_PLACEMENT_TIMES);

		setValue(0, creator);
		setValue(1, creation);
	}
}
