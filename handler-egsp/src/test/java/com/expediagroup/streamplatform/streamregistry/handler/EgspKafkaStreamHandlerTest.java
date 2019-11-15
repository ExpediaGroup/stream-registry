/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.handler;

import static com.expediagroup.streamplatform.streamregistry.handler.EgspType.EGSP_KAFKA;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class EgspKafkaStreamHandlerTest {
  private final ObjectMapper mapper = new ObjectMapper();
  private final EgspKafkaStreamHandler underTest = new EgspKafkaStreamHandler();

  @Test
  public void type() {
    assertThat(underTest.type(), is(EGSP_KAFKA));
  }

  @Test
  public void target() {
    assertThat(underTest.target(), is(equalTo(Stream.class)));
  }

  @Test
  public void insertTypical() {
    underTest.handleInsert(stream(6, 3, "delete", days(7), days(1)));
  }

  @Test(expected = NullPointerException.class)
  public void insertNullPartitions() {
    underTest.handleInsert(stream(null, 3, "delete", days(7), days(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void insertInvalidPartitions() {
    underTest.handleInsert(stream(0, 3, "delete", days(7), days(1)));
  }

  @Test(expected = NullPointerException.class)
  public void insertNullReplicationFactor() {
    underTest.handleInsert(stream(6, null, "delete", days(7), days(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void insertInvalidReplicationFactor() {
    underTest.handleInsert(stream(6, 0, "delete", days(7), days(1)));
  }

  @Test(expected = NullPointerException.class)
  public void insertNullCleanupPolicy() {
    underTest.handleInsert(stream(6, 3, null, days(7), days(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void insertInvalidCleanupPolicy() {
    underTest.handleInsert(stream(6, 3, "foo", days(7), days(1)));
  }

  @Test(expected = NullPointerException.class)
  public void insertNullRetentionMs() {
    underTest.handleInsert(stream(6, 3, "delete", null, days(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void insertInvalidRetentionMs() {
    underTest.handleInsert(stream(6, 3, "delete", 0L, days(1)));
  }

  @Test(expected = NullPointerException.class)
  public void insertNullMinCompactionLagMs() {
    underTest.handleInsert(stream(6, 3, "compact", days(7), null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void insertInvalidMinCompactionLagMs() {
    underTest.handleInsert(stream(6, 3, "compact", days(7), 0L));
  }

  private long days(int days) {
    return Duration.ofDays(days).toMillis();
  }

  private Stream stream(
      Integer partitions,
      Integer replicationFactor,
      String cleanupPolicy,
      Long retentionMs,
      Long minCompactionLagMs) {
    ObjectNode config = mapper.createObjectNode();
    ObjectNode log = config.putObject("log");

    if (partitions != null) {
      log.put("partitions", partitions);
    }
    if (replicationFactor != null) {
      log.put("replicationFactor", replicationFactor);
    }
    if (cleanupPolicy != null) {
      log.put("cleanup.policy", cleanupPolicy);
    }
    if (retentionMs != null) {
      log.put("retention.ms", retentionMs);
    }
    if (minCompactionLagMs != null) {
      log.put("min.compaction.lag.ms", minCompactionLagMs);
    }

    Stream stream = new Stream();
    Specification specification = new Specification();
    stream.setSpecification(specification);
    specification.setConfiguration(config);

    return stream;
  }
}