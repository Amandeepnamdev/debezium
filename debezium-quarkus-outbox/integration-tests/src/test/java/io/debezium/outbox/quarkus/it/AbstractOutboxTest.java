/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.outbox.quarkus.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.junit.jupiter.api.Test;

import io.debezium.outbox.quarkus.internal.OutboxConstants;
import io.quarkus.test.common.QuarkusTestResource;

/**
 * Abstract base class for the Debezium Outbox extension test suite.  Each subclass implementation can
 * specify custom profiles to execute the same tests with differing behavior.
 *
 * @author Chris Cranford
 */
@QuarkusTestResource(DatabaseTestResource.class)
public abstract class AbstractOutboxTest {
    @Inject
    EntityManager entityManager;

    @Inject
    MyService myService;

    @Test
    public void testOutboxEntityMetamodelExists() throws Exception {
        final MetamodelImplementor metadata = entityManager.unwrap(SessionImplementor.class).getFactory().getMetamodel();

        final EntityPersister persister = metadata.entityPersister(OutboxConstants.OUTBOX_ENTITY_FULLNAME);
        assertNotNull(persister);

        // this assumes the default mapping settings, no custom converters or column types
        assertEquals(UUID.class, persister.getIdentifierType().getReturnedClass());
        assertEquals(String.class, persister.getPropertyType("aggregateType").getReturnedClass());
        assertEquals(Long.class, persister.getPropertyType("aggregateId").getReturnedClass());
        assertEquals(String.class, persister.getPropertyType("type").getReturnedClass());
        assertEquals(Instant.class, persister.getPropertyType("timestamp").getReturnedClass());
        assertEquals(String.class, persister.getPropertyType("payload").getReturnedClass());
    }

    @Test
    public void firedEventGetsPersistedInOutboxTable() {
        myService.doSomething();

        Query q = entityManager
                .createNativeQuery("SELECT CAST(id as varchar), aggregateId, aggregateType, type, timestamp, payload FROM OutboxEvent");
        Object[] row = (Object[]) q.getSingleResult();

        assertNotNull(UUID.fromString((String) row[0]));
        assertEquals(BigInteger.valueOf(1L), row[1]);
        assertEquals("MyOutboxEvent", row[2]);
        assertEquals("SomeType", row[3]);
        assertTrue(((Timestamp) row[4]).toInstant().isBefore(Instant.now()));
        assertEquals("Some amazing payload", row[5]);
    }
}
