/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.data;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import io.debezium.schema.SchemaFactory;

/**
 * A semantic type for a JSON string.
 *
 * @author Randall Hauch
 */
public class Json {

    public static final String JSON_SCHEMA_NAME = "io.debezium.data.Json";

    private static final SchemaFactory schemaFactoryObject = SchemaFactory.get();

    /**
     * Returns a {@link SchemaBuilder} for a JSON field. You can use the resulting SchemaBuilder
     * to set additional schema settings such as required/optional, default value, and documentation.
     *
     * @return the schema builder
     */
    public static SchemaBuilder builder() {
        return schemaFactoryObject.datatypeJsonSchema();
    }

    /**
     * Returns a {@link SchemaBuilder} for a JSON field, with all other default Schema settings.
     *
     * @return the schema
     * @see #builder()
     */
    public static Schema schema() {
        return builder().build();
    }
}
