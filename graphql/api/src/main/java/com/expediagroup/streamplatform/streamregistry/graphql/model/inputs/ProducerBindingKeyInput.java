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
package com.expediagroup.streamplatform.streamregistry.graphql.model.inputs;

import static com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.NameNormaliser.normalise;

import lombok.Builder;
import lombok.Value;

import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;

@Value
@Builder
public class ProducerBindingKeyInput {
  String streamDomain;
  String streamName;
  Integer streamVersion;
  String infrastructureZone;
  String infrastructureName;
  String producerName;

  public ProducerBindingKey asProducerBindingKey() {
    return new ProducerBindingKey(
        normalise(streamDomain),
        normalise(streamName),
        streamVersion,
        normalise(infrastructureZone),
        normalise(infrastructureName),
        normalise(producerName)
    );
  }
}