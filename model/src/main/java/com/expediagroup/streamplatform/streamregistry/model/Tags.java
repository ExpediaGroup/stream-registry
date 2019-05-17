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

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

// TODO - Need to generalize Tags to be company agnostic (#110)
@Data
@Builder
public class Tags {
  /**
   * Product Id of the application.
   */
  @NonNull @Default Integer productId = -1;

  /**
   * Portfolio Id of the application.
   */
  @NonNull @Default Integer portfolioId = -1;

  /**
   * Brand name in Expedia Inc.
   */
  @NonNull @Default String brand = null;

  /**
   * Asset Protection Level
   */
  @NonNull @Default String assetProtectionLevel = null;

  /**
   * ComponentId (required)
   */
  @NonNull String componentId;

  /**
   * Hint for Stream Registry to choose a cluster.
   * Example: primary, secondary, logs.
   */
  @NonNull @Default String hint = "primary";
}
