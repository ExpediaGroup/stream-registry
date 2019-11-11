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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Component
public class EgspKafkaStreamHandler implements Handler<Stream> {
  @Override
  public String type() {
    return "egsp.kafka";
  }

  @Override
  public Class<Stream> target() {
    return Stream.class;
  }

  @Override
  public Specification handleInsert(Stream stream) {
    return handle(stream);
  }

  @Override
  public Specification handleUpdate(Stream stream, Stream existing) {
    return handle(stream);
  }

  private Specification handle(Stream stream) {
    Specification specification = stream.getSpecification();
    ObjectNode configuration = specification.getConfiguration();

    ObjectNode log = (ObjectNode) configuration.get("log");
    checkNotNull(log, "log must not be null");

    validateGreaterThanZero(log, "partitions");
    validateGreaterThanZero(log, "replicationFactor");

    Predicate<JsonNode> predicate = value -> List.of("delete", "compact").contains(value.asText());
    validate(log, "cleanup.policy", predicate, "%s must be one of delete or compact");

    if ("delete".equals(log.get("cleanup.policy").asText())) {
      validateGreaterThanZero(log, "retention.ms");
    } else {
      validateGreaterThanZero(log, "min.compaction.lag.ms");
    }

    return specification;
  }

  private void validate(JsonNode node, String key, Predicate<JsonNode> predicate, String errorTemplate) {
    JsonNode value = node.get(key);
    checkNotNull(value, "%s must not be null", key);
    checkArgument(predicate.test(value), errorTemplate, key);
  }

  private void validateGreaterThanZero(JsonNode node, String key) {
    validate(node, key, value -> value.asInt() > 0, "%s must be greater than 0");
  }
}
