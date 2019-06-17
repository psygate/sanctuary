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

package com.psygate.minecraft.spigot.sovereignty.sanctuary.data;

import com.psygate.collections.Pair;
import com.psygate.minecraft.spigot.sovereignty.nucleus.util.mc.BlockKey;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.Sanctuary;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryAnchorsRecord;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryPayedItemsRecord;
import com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.tables.records.SanctuaryReinforcedRecord;
import com.psygate.spatial.primitives.IntAABB2D;
import org.bukkit.Material;
import org.bukkit.World;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.psygate.minecraft.spigot.sovereignty.sanctuary.db.model.Tables.*;

/**
 * Created by psygate (https://github.com/psygate) on 29.03.2016.
 */
class SanctuaryIO {
    private AtomicBoolean mode = new AtomicBoolean(false);
    private final SanctuaryManager manager;
    private final static Logger LOG = Sanctuary.getLogger(SanctuaryIO.class.getName());

    SanctuaryIO(SanctuaryManager manager) {
        this.manager = manager;
    }

    public void writeReinforcementRecords(SanctuaryReinforcedRecord record) {
        if (record.getSanctuaryId() == null) {
            System.err.println("Required ID on SanctuaryReinforcedRecord is null.");
            new NullPointerException().printStackTrace();
            return;
        }

        execute((conf) -> {
            writeReinforcementRecord(record, DSL.using(conf));
        });
    }

    public Optional<SanctuaryReinforcedRecord> getReinforced(BlockKey key, Long sanctuaryID) {
        return execute((conf) -> {
            return DSL.using(conf).selectFrom(SANCTUARY_REINFORCED)
                    .where(SANCTUARY_REINFORCED.SANCTUARY_ID.eq(sanctuaryID))
                    .and(SANCTUARY_REINFORCED.X.eq(key.getX()))
                    .and(SANCTUARY_REINFORCED.Y.eq(key.getY()))
                    .and(SANCTUARY_REINFORCED.Z.eq(key.getZ()))
                    .and(SANCTUARY_REINFORCED.WORLD_UUID.eq(key.getUuid()))
                    .fetchOptional();
        });
    }

    public void write(List<LSanctuaryField> fields) {
        execute((conf) -> {
            DSLContext ctx = DSL.using(conf);

            for (LSanctuaryField field : fields) {
                processField(field, ctx);
            }
        });
    }


    public void delete(LSanctuaryField sanctuary) {
        if (sanctuary.getId() != null) {
            execute((conf) -> {
                DSL.using(conf).deleteFrom(SANCTUARY_ANCHORS)
                        .where(SANCTUARY_ANCHORS.SANCTUARY_ID.eq(sanctuary.getId()))
                        .execute();
            });
        }
    }

    public void syncWrite(LSanctuaryField field) {
        execute((TransactionalRunnable) (conf -> processField(field, DSL.using(conf))), true);
    }

    public List<LSanctuaryField> loadInBounds(Collection<Pair<UUID, IntAABB2D>> bounds) {
        return execute((conf) -> {
            Condition cond = bounds.stream().map(bound ->
                    SANCTUARY_ANCHORS.WORLD_UUID.eq(bound.getKey())
                            .and(SANCTUARY_ANCHORS.X.ge(bound.getValue().getLower().getX()))
                            .and(SANCTUARY_ANCHORS.Z.ge(bound.getValue().getLower().getY()))
                            .and(SANCTUARY_ANCHORS.X.le(bound.getValue().getUpper().getX()))
                            .and(SANCTUARY_ANCHORS.Z.le(bound.getValue().getUpper().getY()))
            )
                    .reduce(DSL.falseCondition(), (a, b) -> a.or(b));

            DSLContext ctx = DSL.using(conf);
            List<Long> ids = ctx.select(SANCTUARY_ANCHORS.SANCTUARY_ID).from(SANCTUARY_ANCHORS)
                    .where(cond)
                    .fetch(Record1<Long>::value1);


//            System.out.println("Loaded sanctuaries: " + fields);
            return loadByIds(ctx, ids);
        });
    }

    private List<LSanctuaryField> loadByIds(DSLContext ctx, List<Long> ids) {
        LinkedList<LSanctuaryField> fields = new LinkedList<>();
        for (Long id : ids) {
            HashMap<Material, Integer> materials = new HashMap<>();
            ctx.selectFrom(SANCTUARY_PAYED_ITEMS)
                    .where(SANCTUARY_PAYED_ITEMS.SANCTUARY_ID.eq(id))
                    .fetch()
                    .forEach(rec -> materials.put(rec.getMaterialType(), rec.getAmount()));

            SanctuaryAnchorsRecord rec = ctx
                    .selectFrom(SANCTUARY_ANCHORS)
                    .where(SANCTUARY_ANCHORS.SANCTUARY_ID.eq(id))
                    .fetchOne();

            fields.add(new LSanctuaryField(
                    rec.getSanctuaryId(),
                    rec.getCreationTime().getTime(),
                    rec.getCreator(),
                    rec.getGroupId(),
                    new BlockKey(rec.getX(), rec.getY(), rec.getZ(), rec.getWorldUuid()),
                    rec.getHealth(),
                    rec.getMaxHealth(),
                    rec.getSanctuaryName(),
                    materials,
                    manager
            ));
        }

        return fields;
    }

    private void processField(LSanctuaryField field, DSLContext ctx) {
        LOG.info("Field state " + field + " " + field.getState());
        switch (field.getState()) {
            case DELETED:
                ctx.deleteFrom(SANCTUARY_ANCHORS).where(SANCTUARY_ANCHORS.SANCTUARY_ID.eq(field.getId())).execute();
                break;
            case NEW:
                Optional<SanctuaryAnchorsRecord> fieldopt = ctx.selectFrom(SANCTUARY_ANCHORS)
                        .where(SANCTUARY_ANCHORS.X.eq(field.getLocation().getX()))
                        .and(SANCTUARY_ANCHORS.Y.eq(field.getLocation().getY()))
                        .and(SANCTUARY_ANCHORS.Z.eq(field.getLocation().getZ()))
                        .and(SANCTUARY_ANCHORS.WORLD_UUID.eq(field.getLocation().getWorld().getUID()))
                        .fetchOptional();

                if (fieldopt.isPresent()) {
                    LOG.warning(
                            "Duplicate sanctuary found, dropping old one. Here's the data for reference:\n"
                                    + fieldopt.get()
                                    + "\n"
                    );
                    ctx.deleteFrom(SANCTUARY_ANCHORS).where(SANCTUARY_ANCHORS.SANCTUARY_ID.eq(fieldopt.get().getSanctuaryId())).execute();
                }

                Long id = ctx.insertInto(SANCTUARY_ANCHORS)
                        .set(asRecord(field))
                        .returning(SANCTUARY_ANCHORS.SANCTUARY_ID)
                        .fetchOne().getSanctuaryId();
                field.setId(id);
                ctx.batchInsert(asItemRecords(field)).execute();
                break;
            case DIRTY:
                SanctuaryAnchorsRecord rec = asRecord(field);

                HashMap<Material, Integer> items = new HashMap<>(field.getItems());

                ctx.update(SANCTUARY_ANCHORS)
                        .set(rec)
                        .where(SANCTUARY_ANCHORS.SANCTUARY_ID.eq(rec.getSanctuaryId()))
                        .execute();

                ctx.deleteFrom(SANCTUARY_PAYED_ITEMS).where(SANCTUARY_PAYED_ITEMS.SANCTUARY_ID.eq(field.getId())).execute();

                List<SanctuaryPayedItemsRecord> records = items.entrySet().stream()
                        .map(en -> new SanctuaryPayedItemsRecord(null, field.getId(), en.getKey(), 0, en.getValue()))
                        .collect(Collectors.toList());

                for (SanctuaryPayedItemsRecord spi : records) {
                    ctx.insertInto(SANCTUARY_PAYED_ITEMS)
                            .set(spi)
                            .execute();
                }
                break;
        }

        field.setState(SanctuaryState.CLEAN);
    }

    private Collection<SanctuaryPayedItemsRecord> asItemRecords(LSanctuaryField field) {
        return field.getItems().entrySet().stream()
                .map(en -> new SanctuaryPayedItemsRecord(null, field.getId(), en.getKey(), 0, en.getValue()))
                .collect(Collectors.toList());
    }

    private SanctuaryAnchorsRecord asRecord(LSanctuaryField field) {
        SanctuaryAnchorsRecord rec = new SanctuaryAnchorsRecord(
                field.getId(),
                new Timestamp(field.getCreationTime()),
                field.getCreator(),
                field.getGroupID(),
                field.getHealth(),
                field.getMaxHealth(),
                field.getLocation().getX(),
                field.getLocation().getY(),
                field.getLocation().getZ(),
                field.getLocation().getUuid(),
                field.getName()
        );
        LOG.info("Record of sanctuary:\n" + rec);
        return rec;
    }

    private void execute(TransactionalRunnable run) {
        if (!mode.get()) {
            Sanctuary.DBI().asyncSubmit(run);
        } else {
            Sanctuary.DBI().submit(run);
        }
    }

    private void execute(TransactionalRunnable run, boolean force) {
        if (!mode.get() && !force) {
            Sanctuary.DBI().asyncSubmit(run);
        } else {
            Sanctuary.DBI().submit(run);
        }
    }

    private <T> T execute(TransactionalCallable<T> run) {
        if (!mode.get()) {
            try {
                return Sanctuary.DBI().asyncSubmit(run).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Sanctuary.DBI().submit(run);
        }
    }

    private <T> T execute(TransactionalCallable<T> run, boolean force) {
        if (!mode.get() && !force) {
            try {
                return Sanctuary.DBI().asyncSubmit(run).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Sanctuary.DBI().submit(run);
        }
    }

    public void forceSynced() {
        mode.set(true);
    }

    public void forceAsync() {
        mode.set(false);
    }

    public void writeReinforcementRecords(List<SanctuaryReinforcedRecord> recs) {

        execute((conf) -> {
            DSLContext ctx = DSL.using(conf);
            for (SanctuaryReinforcedRecord rec : recs) {
                writeReinforcementRecord(rec, ctx);
            }
        });
    }

    private void writeReinforcementRecord(SanctuaryReinforcedRecord rec, DSLContext ctx) {
        try {
            ctx.insertInto(SANCTUARY_REINFORCED)
                    .set(SANCTUARY_REINFORCED.SANCTUARY_ID, rec.getSanctuaryId())
                    .set(SANCTUARY_REINFORCED.X, rec.getX())
                    .set(SANCTUARY_REINFORCED.Y, rec.getY())
                    .set(SANCTUARY_REINFORCED.Z, rec.getZ())
                    .set(SANCTUARY_REINFORCED.WORLD_UUID, rec.getWorldUuid())
                    .onDuplicateKeyIgnore()
                    .execute();
        } catch (Exception e) {
            boolean assertions = false;
            assert assertions = true;
            if (assertions) {
                System.err.println("Silently dropping " + rec.getX() + ", " + rec.getY() + ", " + rec.getZ() + ", " + rec.getWorldUuid() + " -> " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public List<LSanctuaryField> load(UUID wuid) {
        return Sanctuary.DBI().submit((conf) -> {
            DSLContext ctx = DSL.using(conf);
            List<Long> ids = ctx.select(SANCTUARY_ANCHORS.SANCTUARY_ID)
                    .from(SANCTUARY_ANCHORS)
                    .where(SANCTUARY_ANCHORS.WORLD_UUID.eq(wuid))
                    .fetch(Record1<Long>::value1);

            System.out.println("Loading sanctuaries: " + ids.size() + " for " + wuid);
            return loadByIds(ctx, ids);
        });
    }
}