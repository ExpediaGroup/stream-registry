/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.data;

import static com.expediagroup.streamplatform.streamregistry.DataToModel.convertToModel;
import static com.expediagroup.streamplatform.streamregistry.ModelToData.convertToData;
import static com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper.deserialise;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Assert;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

public class DataToModelTest {

  @Test
  public void convertConsumer() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.Consumer(
        new ConsumerKey(random(), random(), 1, random(), random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertProducer() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.Producer(
        new ProducerKey(random(), random(), randomInteger(), random(), random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertProducerBinding() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.ProducerBinding(
        new ProducerBindingKey(random(), random(), randomInteger(), random(), random(), random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertConsumerBinding() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding(
        new ConsumerBindingKey(random(), random(), randomInteger(), random(), random(), random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertDomain() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.Domain(
        new DomainKey(random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertInfrastructure() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.Infrastructure(
        new InfrastructureKey(random(), random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertSchema() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.Schema(
        new SchemaKey(random(), random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertStream() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.Stream(
        new StreamKey(random(), random(), randomInteger()),
        new SchemaKey(random(), random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertStreamBinding() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.StreamBinding(
        new StreamBindingKey(random(), random(), randomInteger(), random(), random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  @Test
  public void convertZone() {
    var in = new com.expediagroup.streamplatform.streamregistry.model.Zone(
        new ZoneKey(random()),
        specification(),
        status()
    );
    var out = convertToModel(convertToData(in));
    Assert.assertEquals(in, out);
  }

  private Status status() {
    return new Status(objectNode());
  }

  private Specification specification() {
    return new Specification(
        random(),
        tags(),
        random(),
        objectNode()
    );
  }

  private ObjectNode objectNode() {
    return deserialise("{\"a\":\"b\"}");
  }

  private List<Tag> tags() {
    return Collections.singletonList(new Tag(random(), random()));
  }

  public Integer randomInteger() {
    return new SecureRandom().nextInt();
  }

  public static final char[] symbols = "abcdefghijklmnopqrstuvwxyz".toCharArray();

  public String random() {
    final Random random = new SecureRandom();
    final char[] buf = new char[10];
    for (int idx = 0; idx < buf.length; ++idx) {
      buf[idx] = symbols[random.nextInt(symbols.length)];
    }
    return new String(buf);
  }
}