/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.events;

import java.util.Collections;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

@Slf4j
public class NotificationEventUtilsTest {

    @Test
    public void having_a_static_method_in_classpath_verify_that_is_loaded_properly() throws NoSuchMethodException, ClassNotFoundException {
        Function<Stream, AvroEvent> func = NotificationEventUtils.loadToAvroStaticMethod(NotificationEventUtilsTest.class.getName(), "myCustomStreamToAvroEntity", Stream.class);
        Assert.assertNotNull("retrieved conversion method can not be null", func);
    }

    @Test(expected = NoSuchMethodException.class)
    public void having_an_undefined_method_in_classpath_verify_that_exception_is_thrown() throws NoSuchMethodException, ClassNotFoundException {
        NotificationEventUtils.loadToAvroStaticMethod(NotificationEventUtilsTest.class.getName(), "myCustomStreamToAvroEntityNonExistingMethod", Stream.class);
    }

    @Test(expected = ClassNotFoundException.class)
    public void having_an_undefined_class_verify_that_exception_is_thrown() throws NoSuchMethodException, ClassNotFoundException {
        NotificationEventUtils.loadToAvroStaticMethod("NonExistentClass", "myCustomStreamToAvroEntityNonExistingMethod", Stream.class);
    }

    @Test
    public void having_loaded_a_method_verify_it_executes() throws NoSuchMethodException, ClassNotFoundException {
        Stream myArg = new Stream();

        Function<Stream, AvroEvent> func = NotificationEventUtils.loadToAvroStaticMethod(NotificationEventUtilsTest.class.getName(), "myCustomStreamToAvroEntity", Stream.class);

        Assert.assertNotNull("retrieved conversion method can not be null", func);

        AvroEvent response = func.apply(myArg);

        Assert.assertNotNull("response on loaded test method shouldn't be null", response);
    }

    @Test
    public void having_a_complete_schema_verify_that_is_correctly_built() {
        Function<Schema, AvroKey> toKeyRecord = NotificationEventUtils::toAvroKeyRecord;

        Function<Schema, AvroEvent> toValueRecord = NotificationEventUtils::toAvroValueRecord;

        val name = "name";
        val domain = "domain";
        val description = "description";
        val type = "type";
        val configJson = "{}";
        val statusJson = "{foo:bar}";
        val tags = Collections.singletonList(new Tag("tag-name", "tag-value"));

        // Key
        SchemaKey key = new SchemaKey();
        key.setName(name);
        key.setDomain(domain);

        // Spec
        Specification spec = new Specification();
        spec.setDescription(description);
        spec.setType(type);
        spec.setConfigJson(configJson);
        spec.setTags(tags);

        // Status
        Status status = new Status();
        status.setStatusJson(statusJson);

        Schema schema = new Schema();
        schema.setKey(key);
        schema.setSpecification(spec);
        schema.setStatus(status);

        AvroKey avroKey = toKeyRecord.apply(schema);
        log.info("Obtained avro key {}", avroKey);

        Assert.assertNotNull("Avro key shouldn't be null", avroKey);
        Assert.assertNotNull("Key id shouldn't be null", avroKey.getId());
        Assert.assertEquals("Name should be the same as the id", name, avroKey.getId());
        Assert.assertEquals(domain, avroKey.getDomain());

        AvroEvent avroEvent = toValueRecord.apply(schema);
        log.info("Obtained avro event {}", avroEvent);

        Assert.assertNotNull("Avro event shouldn't be null", avroEvent);
        Assert.assertNotNull("Schema entity shouldn't be null", avroEvent.getSchemaEntity());
        Assert.assertEquals(name, avroEvent.getSchemaEntity().getName());
        Assert.assertEquals(domain, avroEvent.getSchemaEntity().getDomain());
        Assert.assertEquals(description, avroEvent.getSchemaEntity().getDescription());
        Assert.assertEquals(type, avroEvent.getSchemaEntity().getType());
        Assert.assertEquals(configJson, avroEvent.getSchemaEntity().getConfigurationString());
        Assert.assertEquals(statusJson, avroEvent.getSchemaEntity().getStatusString());
    }

    // Don't remove, is loaded by reflection for this test...
    public static AvroEvent myCustomStreamToAvroEntity(Stream egStream) {
        log.info("My custom event is being called ☺ {}", egStream);

        return new AvroEvent();
    }
}