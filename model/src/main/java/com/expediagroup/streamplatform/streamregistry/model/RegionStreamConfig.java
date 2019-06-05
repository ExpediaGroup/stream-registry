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
package com.expediagroup.streamplatform.streamregistry.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

//TODO @Data -> @Value
@Data
@Builder
public class RegionStreamConfig {
  /**
   * Region where the Producer/Consumer is deployed.
   */
  @NonNull String region;

  /**
   * Name of the cluster that Producer/Consumer is communicating to.
   */
  @NonNull String cluster;

  /**
   * Topic Names
   */
  @NonNull List<String> topics;

  /**
   * Streams properties of the Producer/Consumer
   */
  @NonNull Map<String, String> streamConfiguration;
}
