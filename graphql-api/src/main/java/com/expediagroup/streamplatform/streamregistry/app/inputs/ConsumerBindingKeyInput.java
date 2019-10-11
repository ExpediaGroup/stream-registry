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
package com.expediagroup.streamplatform.streamregistry.app.inputs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;

@Value
@Builder
public class ConsumerBindingKeyInput {

  public ConsumerBindingKeyInput() {}

  @Getter
  @Setter
  String streamDomain = null;
  @Getter
  @Setter
  String streamName = null;
  @Getter
  @Setter
  Integer streamVersion = null;
  @Getter
  @Setter
  String infrastructureZone = null;
  @Getter
  @Setter
  String infrastructureName = null;
  @Getter
  @Setter
  String consumerName = null;

  public ConsumerBindingKey asConsumerBindingKey() {
    return new ConsumerBindingKey(
        getStreamDomain(),
        getStreamName(),
        getStreamVersion(),
        getInfrastructureZone(),
        getInfrastructureName(),
        getConsumerName()
    );
  }
}